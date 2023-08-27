package com.rikkimikki.telegramgallery3.feature_node.data.repository

import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramException
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramFlow
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.downloadFile
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.readFilePart
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.util.regex.Pattern
import kotlin.math.min

class LocalServer(port: Int, private val telegramFlow: TelegramFlow) : NanoHTTPD("localhost",port) {
    private val downloadedBlockOffsets = mutableMapOf<Int,MutableSet<Long>>() // [<mediaId>: [0, 6mb, 12bm,...],...]
    private var superSize = mutableMapOf<Int,Int>() // [<mediaId>: <size>]
    fun clear() {
        downloadedBlockOffsets.clear()
        superSize.clear()
    }

    private fun getRest(fileId: Int,superStart:Long) =
        superSize[fileId]?.let { min(MAX_CHUNK_SIZE, it - superStart.toInt()) } ?: MAX_CHUNK_SIZE

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val fileId = extractFileId(uri)
        val range = session.headers["range"]?.let { parseRange(it) }
        val superStart = range?.first ?: 0

        val startOffset = (superStart.div(MAX_CHUNK_SIZE)) * MAX_CHUNK_SIZE
        val endOffset = startOffset + MAX_CHUNK_SIZE

        superSize[fileId]?.takeIf { superStart > it }?.let {
            println("RANGE_NOT_SATISFIABLE")
            return newFixedLengthResponse(
                Response.Status.RANGE_NOT_SATISFIABLE,
                MIME_PLAINTEXT,
                "err"
            )
        }


        for (offset in startOffset .. endOffset step MAX_CHUNK_SIZE.toLong()) {
            if (!downloadedBlockOffsets.getOrPut(fileId) { mutableSetOf() }.contains(offset)) {
                try {
                    runBlocking(Dispatchers.IO) {
                        val file = telegramFlow.downloadFile(
                            fileId,
                            30,
                            offset.toInt(),
                            getRest(fileId,offset),
                            true
                        )
                        if (superSize.getOrPut(fileId) { 0 } == 0)
                            superSize[fileId] = file.size
                    }
                } catch (e: TelegramException){
                    e.printStackTrace()
                    //extra part beyond the file size
                }
                downloadedBlockOffsets.getValue(fileId).add(offset)
            }
        }
        return runBlocking(Dispatchers.IO) {
            try {
                println("rfp")
                val filePart = telegramFlow.readFilePart(
                    fileId,
                    superStart.toInt(),
                    getRest(fileId,superStart)
                )
                val inputStream = ByteArrayInputStream(filePart.data)
                val response = newFixedLengthResponse(
                    Response.Status.PARTIAL_CONTENT,
                    "video/mp4",
                    inputStream,
                    filePart.data.size.toLong()
                )
                response.addHeader("Content-Length", filePart.data.size.toString())
                response.addHeader(
                    "Content-Range",
                    "bytes ${superStart}-${superStart + filePart.data.size - 1}/${superSize[fileId]?:"*"}"
                )
                return@runBlocking response
            } catch (e: TelegramException) {
                e.printStackTrace()
                clear()
                return@runBlocking newFixedLengthResponse(
                    Response.Status.REDIRECT,
                    MIME_HTML,
                    ""
                ).apply { addHeader("Location", session.uri) } //clear and retry request

            } catch (e: Exception) {
                e.printStackTrace()
                return@runBlocking newFixedLengthResponse(
                    Response.Status.RANGE_NOT_SATISFIABLE,
                    MIME_PLAINTEXT,
                    "err"
                )
            }
        }
    }

    private fun extractFileId(uri: String): Int {
        val pattern = Pattern.compile("/(\\d+)")
        val matcher = pattern.matcher(uri)
        return if (matcher.find()) matcher.group(1)!!.toInt() else -1
    }

    private fun parseRange(rangeHeader: String): Pair<Long, Long>? {
        val pattern = Pattern.compile("bytes=(\\d+)-(\\d*)")
        val matcher = pattern.matcher(rangeHeader)

        if (matcher.matches()) {
            val start = matcher.group(1)!!.toLong()
            val end =
                if (matcher.group(2)!!.isEmpty()) Long.MAX_VALUE else matcher.group(2)!!.toLong()
            if (start in 0..end) {
                return Pair(start, end)
            }
        }
        return null
    }

    companion object {
        private const val MAX_CHUNK_SIZE = 5 * 1024 * 1024
    }
}