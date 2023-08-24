package com.rikkimikki.telegramgallery3.feature_node.data.repository

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rikkimikki.telegramgallery3.FakeActivity
import com.rikkimikki.telegramgallery3.core.Constants
import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.core.contentFlowObserver
import com.rikkimikki.telegramgallery3.feature_node.data.data_source.InternalDatabase
import com.rikkimikki.telegramgallery3.feature_node.data.data_source.Query
import com.rikkimikki.telegramgallery3.feature_node.data.data_types.findMedia
import com.rikkimikki.telegramgallery3.feature_node.data.data_types.getAlbums
import com.rikkimikki.telegramgallery3.feature_node.data.data_types.getMedia
import com.rikkimikki.telegramgallery3.feature_node.data.data_types.getMediaByUri
import com.rikkimikki.telegramgallery3.feature_node.data.data_types.getMediaFavorite
import com.rikkimikki.telegramgallery3.feature_node.data.data_types.getMediaListByUris
import com.rikkimikki.telegramgallery3.feature_node.data.data_types.getMediaTrashed
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.TelegramCredentials
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramException
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramFlow
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.checkAuthenticationCode
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.checkAuthenticationPassword
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.checkDatabaseEncryptionKey
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.createPrivateChat
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.downloadFile
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.editMessageMedia
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.getChat
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.getChatHistory
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.getFile
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.getMe
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.loadChats
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.pinChatMessage
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.searchChatMessages
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.sendMessage
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.setAuthenticationPhoneNumber
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.setLogVerbosityLevel
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines.setTdlibParameters
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.extensions.ChatKtx
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.extensions.UserKtx
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.flows.authorizationStateFlow
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Album
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Index
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Item
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.model.PinnedAlbum
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import com.rikkimikki.telegramgallery3.feature_node.domain.util.AuthState
import com.rikkimikki.telegramgallery3.feature_node.domain.util.MediaOrder
import com.rikkimikki.telegramgallery3.feature_node.domain.util.OrderType
import com.rikkimikki.telegramgallery3.feature_node.presentation.picker.AllowedMedia
import com.rikkimikki.telegramgallery3.feature_node.presentation.picker.AllowedMedia.*
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.getDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import org.drinkless.td.libcore.telegram.TdApi
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStream
import kotlin.math.min

class MediaRepositoryImpl(
    private val context: Context,
    private val contentResolver: ContentResolver,
    private val database: InternalDatabase,
    private val telegramCredentials : TelegramCredentials
) : MediaRepository, UserKtx, ChatKtx {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    override val api: TelegramFlow = TelegramFlow()
    lateinit var index : Index
    private lateinit var indexChat:TdApi.Chat
    lateinit var indexMsg: TdApi.Message
    lateinit var me : TdApi.User

    private val authFlow = api.authorizationStateFlow()
        .onEach {
            checkRequiredParams(it)
        }
        .map {
            if (api.runCatching { api.getMe() }.isSuccess){
                getIndex()
                //AuthState.LoggedIn
                return@map AuthState.LoggedIn
            }

            when (it) {
                is TdApi.AuthorizationStateReady -> {
                    getIndex()
                    AuthState.LoggedIn
                }
                is TdApi.AuthorizationStateWaitCode -> AuthState.EnterCode
                is TdApi.AuthorizationStateWaitPassword -> AuthState.EnterPassword
                is TdApi.AuthorizationStateWaitPhoneNumber -> AuthState.EnterPhone
                else -> AuthState.Waiting
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = AuthState.Initial
        )

    private suspend fun checkRequiredParams(state: TdApi.AuthorizationState?) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                api.setTdlibParameters(telegramCredentials.parameters)
            }
            is TdApi.AuthorizationStateWaitEncryptionKey ->{
                api.checkDatabaseEncryptionKey(ByteArray(0))
            }
        }
    }

    private fun itemToMedia(item: Item): Media{
        var formattedDate = ""
        if (item.date != 0L) {
            formattedDate = item.date.getDate(Constants.EXTENDED_DATE_FORMAT)
        }
        return Media(
            id = item.msgId,
            label = item.label,
            uri = "".toUri(),
            path = "",
            albumID = -99L,
            albumLabel = "",
            timestamp = item.date,
            fullDate = formattedDate,
            mimeType = item.mimeType,
            favorite = if (item.favorite) 1 else 0,
            trashed = if (item.trashed) 1 else 0,
            size = item.size,
            orientation = 0,
            tags = item.tags,
            duration = item.duration,
            thumbnailMsgId = item.thumbnailMsgId

        )
    }

    override fun getMedia(): Flow<Resource<List<Media>>> =
        contentResolver.retrieveMedia {
            (index.photo+index.video).map { item ->
                itemToMedia(item)
            }
        }

    override fun getMediaByType(allowedMedia: AllowedMedia): Flow<Resource<List<Media>>> =
        contentResolver.retrieveMedia {
            val items = when (allowedMedia) {
                PHOTOS -> index.photo
                VIDEOS -> index.video
                BOTH -> index.photo+index.video
            }
            items.map { item ->
                itemToMedia(item)
            }
        }


    override fun getFavorites(mediaOrder: MediaOrder): Flow<Resource<List<Media>>> =
        contentResolver.retrieveMedia {
            (index.photo+index.video).filter { it.favorite } .map { item ->
                itemToMedia(item)
            }
        }

    override fun getTrashed(mediaOrder: MediaOrder): Flow<Resource<List<Media>>> =
        contentResolver.retrieveMedia {
            (index.photo+index.video).filter { it.trashed } .map { item ->
                itemToMedia(item)
            }
        }

    override fun getAlbums(mediaOrder: MediaOrder): Flow<Resource<List<Album>>> =
        contentResolver.retrieveAlbums {
            (index.supportedTags).mapIndexed{ tagIndex, tag ->
                val photoCount = index.photo.count { it.tags.contains(tag) }.toLong()
                val videoCount = index.video.count { it.tags.contains(tag) }.toLong()
                var timeStamp = 0L

                val includes = if (photoCount>0)
                    runBlocking(Dispatchers.IO) {
                        val presenter = index.photo.first{it.tags.contains(tag)}
                        timeStamp = presenter.date
                        loadThumbnail(presenter.msgId)
                    }.local.path
                else if (videoCount>0)
                    runBlocking(Dispatchers.IO) {
                        val presenter = index.video.first{it.tags.contains(tag)}
                        timeStamp = presenter.date
                        loadThumbnail(presenter.msgId)
                    }.local.path
                else
                    ""
                Album(
                    id = tagIndex.toLong(),
                    label = tag,
                    pathToThumbnail = includes,
                    timestamp = timeStamp,
                    count = photoCount+videoCount
                )
            }
        }


    override suspend fun insertPinnedAlbum(pinnedAlbum: PinnedAlbum) =
        database.getPinnedDao().insertPinnedAlbum(pinnedAlbum)

    override suspend fun removePinnedAlbum(pinnedAlbum: PinnedAlbum) =
        database.getPinnedDao().removePinnedAlbum(pinnedAlbum)

    @SuppressLint("Range")
    override suspend fun getMediaById(mediaId: Long): Media? {
        val query = Query.MediaQuery().copy(
            bundle = Bundle().apply {
                putString(
                    ContentResolver.QUERY_ARG_SQL_SELECTION,
                    MediaStore.MediaColumns._ID + "= ?"
                )
                putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    arrayOf(mediaId.toString())
                )
            }
        )
        return contentResolver.findMedia(query)
    }

    override fun getMediaByAlbumId(albumId: Long): Flow<Resource<List<Media>>> =
        contentResolver.retrieveMedia {
            val items = index.photo+index.video
            items
                .filter { it.tags.contains(index.supportedTags[albumId.toInt()]) }
                .map { item ->
                    itemToMedia(item)
                }
        }

    override fun getMediaByAlbumIdWithType(
        albumId: Long,
        allowedMedia: AllowedMedia
    ): Flow<Resource<List<Media>>>  =
        contentResolver.retrieveMedia {
            val items = when (allowedMedia) {
                PHOTOS -> index.photo
                VIDEOS -> index.video
                BOTH -> index.photo+index.video
            }
            items
                .filter { it.tags.contains(index.supportedTags[albumId.toInt()]) }
                .map { item ->
                    itemToMedia(item)
                }
        }

    override fun getAlbumsWithType(allowedMedia: AllowedMedia): Flow<Resource<List<Album>>> =
        contentResolver.retrieveAlbums {
            val query = Query.AlbumQuery().copy(
                bundle = Bundle().apply {
                    val mimeType = when (allowedMedia) {
                        PHOTOS -> "image%"
                        VIDEOS -> "video%"
                        BOTH -> "%/%"
                    }
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SELECTION,
                        MediaStore.MediaColumns.MIME_TYPE + " like ?"
                    )
                    putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        arrayOf(mimeType)
                    )
                }
            )
            it.getAlbums(query, mediaOrder = MediaOrder.Label(OrderType.Ascending))
        }

    override fun getMediaByUri(
        uriAsString: String,
        isSecure: Boolean
    ): Flow<Resource<List<Media>>> =
        contentResolver.retrieveMediaAsResource {
            val media = it.getMediaByUri(Uri.parse(uriAsString))
            /** return@retrieveMediaAsResource */
            if (media == null) {
                Resource.Error(message = "Media could not be opened")
            } else {
                val query = Query.MediaQuery().copy(
                    bundle = Bundle().apply {
                        putString(
                            ContentResolver.QUERY_ARG_SQL_SELECTION,
                            MediaStore.MediaColumns.BUCKET_ID + "= ?"
                        )
                        putStringArray(
                            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                            arrayOf(media.albumID.toString())
                        )
                    }
                )
                Resource.Success(
                    data = if (isSecure) listOf(media) else it.getMedia(query)
                        .ifEmpty { listOf(media) })
            }
        }

    override fun getMediaListByUris(listOfUris: List<Uri>): Flow<Resource<List<Media>>> =
        contentResolver.retrieveMediaAsResource {
            val mediaList = it.getMediaListByUris(listOfUris)
            if (mediaList.isEmpty()) {
                Resource.Error(message = "Media could not be opened")
            } else {
                Resource.Success(data = mediaList)
            }
        }

    private fun getFakeSender(): IntentSenderRequest{
        val intentSender = PendingIntent.getActivity(context, 1, Intent(context,FakeActivity::class.java),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE).intentSender
        return IntentSenderRequest.Builder(intentSender)
            .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
            .build()
    }

    override suspend fun toggleFavorite(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        favorite: Boolean
    ) {
        val idList = mediaList.map { it.id }

        index.video.filter { idList.contains(it.msgId) }.forEach { it.favorite = favorite }
        index.photo.filter { idList.contains(it.msgId) }.forEach { it.favorite = favorite }
        uploadIndex()

        result.launch(getFakeSender())
    }

    override suspend fun trashMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        trash: Boolean
    ) {
        val idList = mediaList.map { it.id }
        index.video.filter { idList.contains(it.msgId) }.forEach { it.trashed = trash }
        index.photo.filter { idList.contains(it.msgId) }.forEach { it.trashed = trash }
        uploadIndex()
        result.launch(getFakeSender())
    }

    override suspend fun deleteMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>
    ) {
        val idList = mediaList.map { it.id }
        index.video.removeIf{ idList.contains(it.msgId) }
        index.photo.removeIf{ idList.contains(it.msgId) }
        uploadIndex()
        result.launch(getFakeSender())
    }

    override fun startTelegram() {
        api.attachClient()
    }
    override fun checkAuthState(): Flow<AuthState> {
        return authFlow
    }

    override suspend fun authSendPhone(phone: String) {
        api.setAuthenticationPhoneNumber(phone, null)
    }

    override suspend fun authSendCode(code: String) {
        api.checkAuthenticationCode(code)
    }

    override suspend fun authSendPassword(password: String) {
        api.checkAuthenticationPassword(password)
    }


    suspend fun newIndex():String{

        val tempFile = File.createTempFile("index2", ".tg")
        val newIndexText = """{"supportedTags" : [], "photo" : [], "video" : []"""

        tempFile.writeText(newIndexText)

        val inputFileLocal = TdApi.InputFileLocal(tempFile.path)
        val formattedText = TdApi.FormattedText("index2.tg", arrayOf())
        val doc = TdApi.InputMessageDocument(inputFileLocal,TdApi.InputThumbnail(),false, formattedText)

        val options = TdApi.MessageSendOptions(true,false,null)

        indexMsg = api.sendMessage(me.id,0, options,null, doc)

        tempFile.delete()
        return newIndexText
    }
    override suspend fun uploadIndex(){
        val objectMapper: Gson = GsonBuilder()
            .setLenient()
            .create()

        val miniIndex = objectMapper.toJson(index)

        val tempFile = File.createTempFile("index2", ".tg")

        tempFile.writeText(miniIndex)


        val inputFileLocal = TdApi.InputFileLocal(tempFile.path)
        val formattedText = TdApi.FormattedText("index2.tg", arrayOf())
        val doc = TdApi.InputMessageDocument(inputFileLocal,TdApi.InputThumbnail(),false, formattedText)
        val newIndex = api.editMessageMedia(me.id, indexMsg.id,null,doc)

        tempFile.delete()
    }
    override suspend fun getIndex() : Index{
        me = api.getMe()
        findIndexChat()

        val objectMapper: Gson = GsonBuilder()
            .setLenient()
            .create()

        val a = api.searchChatMessages(indexChat.id,"index2.tg",0,0,0,100,TdApi.SearchMessagesFilterDocument()).messages
            .filter { item-> item.content is TdApi.MessageDocument && (item.content as TdApi.MessageDocument).caption.text == "index2.tg" }.toMutableList()
        if (a.isEmpty()){

            newIndex()
            delay(1000L)
            return getIndex()

        } else {
            indexMsg = a[0]
        }

        if (!indexMsg.isPinned)
            api.pinChatMessage(me.id,indexMsg.id,true)

        val doc = indexMsg.content as TdApi.MessageDocument

        val filePath = api.downloadFile(doc.document.document.id,31,0,0,true).local.path

        val stringBuilder = StringBuilder()
        try {
            val file = File(filePath)
            val reader = BufferedReader(FileReader(file))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val str = stringBuilder.toString()
        index = objectMapper.fromJson(str, Index::class.java)

        return index
    }

    private suspend fun findIndexChat() {
        api.setLogVerbosityLevel(2)
        val chatsPart = api.loadChats(TdApi.ChatListMain() ,100)
        if (chatsPart is TdApi.Error){
            indexChat = api.createPrivateChat(me.id,false)
            return
        } else {
            try {
                indexChat = api.getChat(me.id)
                return
            } catch (e: TelegramException){
                findIndexChat()
            }
        }
        return
    }

    private suspend fun messageIdToFileId(messageId: Long, getThumb: Boolean = false):Int{
        val messages = api.getChatHistory(me.id, (messageId*1024*1024),-1,1,false).messages
        val message = if (messages.isEmpty()) TdApi.Message() else messages[0]
        println(messageId)
        return when(message.content.constructor) {
            TdApi.MessageDocument.CONSTRUCTOR -> {
                val doc = message.content as TdApi.MessageDocument
                if (getThumb){
                    doc.document.thumbnail!!.file.id //doc.document.thumbnail?.file.id :?doc.document.document.id
                } else {
                    doc.document.document.id
                }
                //doc.document.document.id
            }
            TdApi.MessageVideo.CONSTRUCTOR -> {
                val video = message.content as TdApi.MessageVideo
                if (getThumb){
                    video.video.thumbnail!!.file.id
                } else {
                    video.video.video.id
                }
            }
            TdApi.MessageAudio.CONSTRUCTOR -> {
                val audio = message.content as TdApi.MessageAudio
                audio.audio.audio.id
            }
            TdApi.MessagePhoto.CONSTRUCTOR -> {
                val photo = message.content as TdApi.MessagePhoto
                if (getThumb){
                    photo.photo.sizes[0].photo.id
                } else {
                    photo.photo.sizes[photo.photo.sizes.size-1].photo.id
                }
            }
            TdApi.MessageAnimation.CONSTRUCTOR -> {
                val anime = message.content as TdApi.MessageAnimation
                anime.animation.animation.id
            }
            else -> {
                print(message)
                throw Exception("-a-")
            }
        }
    }

    override suspend fun loadThumbnail(messageId:Long): TdApi.File {
        val fileId = messageIdToFileId(messageId,true)
        return api.downloadFile(fileId,31,0,0,true)
    }

    override suspend fun loadPhoto(messageId:Long): TdApi.File {
        val fileId = messageIdToFileId(messageId,false)
        return api.downloadFile(fileId,30,0,0,true)
    }

    override suspend fun loadVideo(messageId:Long): TdApi.File {
        val fileId = messageIdToFileId(messageId,false)
        return api.downloadFile(fileId,29,0,1,true)
    }

    override fun getTags(): List<String> = index.supportedTags

    private lateinit var compositeBitmap:Bitmap
    private var numFrames:Int = -1
    override suspend fun prepareVideoThumbnail(messageId:Long) {
        val fileId = messageIdToFileId(messageId,false)
        val file = api.downloadFile(fileId,32,0,0,true)

        val inputStream: InputStream = FileInputStream(file.local.path)
        compositeBitmap = BitmapFactory.decodeStream(inputStream)

        numFrames = compositeBitmap.width / PREWIEW_FRAME_WIDTH
    }


    override fun cleaner() {
        val files1 = File(context.filesDir.absolutePath + "/td/documents").listFiles()!!
        //val files2 = File(context.filesDir.absolutePath + "/td/thumbnails").listFiles()!!
        //val files3 = File(context.filesDir.absolutePath + "/td/temp").listFiles()!!
        //for (tempFile in files1+files2+files3) {
        for (tempFile in files1) {
            tempFile.delete()
        }
    }

    override fun provideApi(): TelegramFlow {
        return api
    }

    override fun getVideoThumbnail(seconds: Long, totalSeconds: Long): Bitmap {

        val frameIndex = min( (numFrames * (seconds / totalSeconds.toFloat()) ).toInt(), numFrames-1)

        return Bitmap.createBitmap(
            compositeBitmap,
            frameIndex * PREWIEW_FRAME_WIDTH,
            0,
            PREWIEW_FRAME_WIDTH,
            PREWIEW_FRAME_HEIGHT
        )
    }

    companion object {
        private val PREWIEW_FRAME_WIDTH = 160
        private val PREWIEW_FRAME_HEIGHT = 90
        private val DEFAULT_ORDER = MediaOrder.Date(OrderType.Descending)
        private val URIs = arrayOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )

        private fun ContentResolver.retrieveMediaAsResource(dataBody: suspend (ContentResolver) -> Resource<List<Media>>) =
            contentFlowObserver(URIs).map {
                try {
                    dataBody.invoke(this)
                } catch (e: Exception) {
                    Resource.Error(message = e.localizedMessage ?: "An error occurred")
                }
            }

        private fun ContentResolver.retrieveMedia(dataBody: suspend (ContentResolver) -> List<Media>) =
            contentFlowObserver(URIs).map {
                try {
                    Resource.Success(data = dataBody.invoke(this))
                } catch (e: Exception) {
                    Resource.Error(message = e.localizedMessage ?: "An error occurred")
                }
            }

        private fun ContentResolver.retrieveAlbums(dataBody: suspend (ContentResolver) -> List<Album>) =
            contentFlowObserver(URIs).map {
                try {
                    Resource.Success(data = dataBody.invoke(this))
                } catch (e: Exception) {
                    Resource.Error(message = e.localizedMessage ?: "An error occurred")
                }
            }
    }
}