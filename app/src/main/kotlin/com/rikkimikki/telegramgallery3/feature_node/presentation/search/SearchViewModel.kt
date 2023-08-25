package com.rikkimikki.telegramgallery3.feature_node.presentation.search

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rikkimikki.telegramgallery3.core.MediaState
import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.model.MediaItem
import com.rikkimikki.telegramgallery3.feature_node.domain.use_case.MediaUseCases
import com.rikkimikki.telegramgallery3.feature_node.presentation.picker.AllowedMedia
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.getDate
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.getMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val mediaUseCases: MediaUseCases
) : ViewModel() {

    var lastQuery = mutableStateOf("")
        private set

    private val _mediaState = MutableStateFlow(MediaState())
    val mediaState = _mediaState.asStateFlow()

    val selectionState = mutableStateOf(false)
    val selectedMedia = mutableStateListOf<Media>()

    init {
        queryMedia()
    }

    /*private suspend fun List<Media>.parseQuery(query: String): List<Media> {
        return withContext(Dispatchers.IO) {
            if (query.isEmpty())
                return@withContext emptyList()
            val queryTags = query.split(",").map { it.trim() }
            val matches = FuzzySearch.extractSorted(query, this@parseQuery, { it.toString() }, 60)
            return@withContext matches.map { it.referent }.ifEmpty { emptyList() }
        }
    }*/
    private fun List<Media>.parseQuery(query: String): List<Media> {
        if (query.isBlank()) {
            return emptyList()
        }

        val queryTags = query.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val positiveTags = mutableListOf<String>()
        val negativeTags = mutableListOf<String>()

        for (tag in queryTags) {
            if (tag.startsWith("-")) {
                negativeTags.add(tag.substring(1))
            } else {
                positiveTags.add(tag)
            }
        }

        return this.filter { media ->
            val hasAllPositiveTags = positiveTags.all { media.tags.contains(it) }
            val hasNoNegativeTags = negativeTags.none { media.tags.contains(it) }
            hasAllPositiveTags && hasNoNegativeTags
        }
    }


    fun clearQuery() = queryMedia("")
    fun completeTags(q: String): List<String>{
        val allTags = mediaUseCases.getTagsUseCase()
        if (q == "" || q == "-")
            return listOf()
        return if (q.startsWith("-"))
            allTags.filter { it.startsWith(q.removePrefix("-")) }.map { "-$it" }
        else
            allTags.filter { it.startsWith(q) }
    }

    /*fun queryMedia(query: String = "") {
        viewModelScope.launch {
            lastQuery.value = query
            mediaUseCases.getMediaFilteredUseCase(query).flowOn(Dispatchers.IO).collectLatest { result ->
                val mappedData = ArrayList<MediaItem>()
                val monthHeaderList = ArrayList<String>()
                val data = result.data ?: emptyList()
                if (data == mediaState.value.media) return@collectLatest
                val error = if (result is Resource.Error) result.message
                    ?: "An error occurred" else ""
                if (data.isEmpty()) {
                    return@collectLatest _mediaState.emit(MediaState())
                }
                _mediaState.value = MediaState(isLoading = true)
                val parsedData = data//.parseQuery(query)
                parsedData.groupBy {
                    it.timestamp.getDate(
                        stringToday = "Today"
                        ,
                        stringYesterday = "Yesterday"
                    )
                }.forEach { (date, data) ->
                    val month = getMonth(date)
                    if (month.isNotEmpty() && !monthHeaderList.contains(month)) {
                        monthHeaderList.add(month)
                    }
                    mappedData.add(MediaItem.Header("header_$date", date, data))
                    mappedData.addAll(data.map {
                        MediaItem.MediaViewItem.Loaded(
                            "media_${it.id}_${it.label}",
                            it
                        )
                    })
                }
                _mediaState.value =
                    MediaState(
                        error = error,
                        media = parsedData,
                        mappedMedia = mappedData
                    )
            }
        }
    }*/

    fun queryMedia(query: String = "", isPhoto:Boolean = true) {
        viewModelScope.launch {
            lastQuery.value = query
            mediaUseCases.getMediaByTypeUseCase(if (isPhoto) AllowedMedia.PHOTOS else AllowedMedia.VIDEOS).flowOn(Dispatchers.IO).collectLatest { result ->
                val mappedData = ArrayList<MediaItem>()
                val monthHeaderList = ArrayList<String>()
                val data = result.data ?: emptyList()
                if (data == mediaState.value.media) return@collectLatest
                val error = if (result is Resource.Error) result.message
                    ?: "An error occurred" else ""
                if (data.isEmpty()) {
                    return@collectLatest _mediaState.emit(MediaState())
                }
                _mediaState.value = MediaState(isLoading = true)
                val parsedData = data.parseQuery(query)
                parsedData.groupBy {
                    it.timestamp.getDate(
                        stringToday = "Today",
                        stringYesterday = "Yesterday"
                    )
                }.forEach { (date, data) ->
                    val month = getMonth(date)
                    if (month.isNotEmpty() && !monthHeaderList.contains(month)) {
                        monthHeaderList.add(month)
                    }
                    mappedData.add(MediaItem.Header("header_$date", date, data))
                    mappedData.addAll(data.map {
                        MediaItem.MediaViewItem.Loaded(
                            "media_${it.id}_${it.label}",
                            it
                        )
                    })
                }
                _mediaState.value =
                    MediaState(
                        error = error,
                        media = parsedData,
                        mappedMedia = mappedData
                    )
            }
        }
    }

}