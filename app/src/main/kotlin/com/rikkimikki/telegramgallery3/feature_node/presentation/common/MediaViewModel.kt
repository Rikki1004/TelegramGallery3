package com.rikkimikki.telegramgallery3.feature_node.presentation.common

import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rikkimikki.telegramgallery3.core.MediaState
import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.data.repository.LocalServer
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.use_case.MediaUseCases
import com.rikkimikki.telegramgallery3.feature_node.presentation.picker.AllowedMedia
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.collectMedia
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.mediaFlow
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.mediaFlowWithType
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.update
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class MediaViewModel @Inject constructor(
    private val mediaUseCases: MediaUseCases
) : ViewModel() {

    val multiSelectState = mutableStateOf(false)
    private val _mediaState = MutableStateFlow(MediaState())
    val mediaState = _mediaState.asStateFlow()
    val selectedPhotoState = mutableStateListOf<Media>()
    val handler = mediaUseCases.mediaHandleUseCase

    //-
    val thumbLoader = mediaUseCases.loadThumbnailUseCase
    val photoLoader = mediaUseCases.loadPhotoUseCase
    val videoLoader = mediaUseCases.loadVideoUseCase
    val thumbPreparer = mediaUseCases.prepareVideoThumbnailUseCase
    val api = mediaUseCases.provideApiUseCase()
    private val server = LocalServer(8081,api)
    //-

    var albumId: Long = -1L
        set(value) {
            getMedia(albumId = value)
            field = value
        }
    var target: String? = null
        set(value) {
            getMedia(target = value)
            field = value
        }

    var groupByMonth: Boolean = false

    /**
     * Used in PhotosScreen to retrieve all media
     */
    fun launchInPhotosScreen() {
        getMediaWithType(-1, AllowedMedia.PHOTOS)
    }
    fun launchInVideoScreen() {
        getMediaWithType(-1, AllowedMedia.VIDEOS)
    }
    fun onServer(){
        if(!server.wasStarted())
            server.start()
        //server.clear()
    }
    fun offServer(){
        if(server.wasStarted()){
            server.closeAllConnections()
            server.stop()
        }
        server.clear()

    }
    fun cleaner(){
        mediaUseCases.cleanOldFilesUseCase()
        //server.clear()
    }


    fun toggleFavorite(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        favorite: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            handler.toggleFavorite(result, mediaList, favorite)
        }
    }

    fun toggleSelection(index: Int) {
        viewModelScope.launch {
            val item = mediaState.value.media[index]
            val selectedPhoto = selectedPhotoState.find { it.id == item.id }
            if (selectedPhoto != null) {
                selectedPhotoState.remove(selectedPhoto)
            } else {
                selectedPhotoState.add(item)
            }
            multiSelectState.update(selectedPhotoState.isNotEmpty())
        }
    }

    private fun getMediaWithType(albumId: Long = -1L, allowedMedia: AllowedMedia) {
        viewModelScope.launch {
            mediaUseCases.mediaFlowWithType(albumId, allowedMedia).flowOn(Dispatchers.IO).collectLatest { result ->
                val data = result.data ?: emptyList()
                if (data == mediaState.value.media) return@collectLatest
                val error = if (result is Resource.Error) result.message
                    ?: "An error occurred" else ""
                if (data.isEmpty()) {
                    return@collectLatest _mediaState.emit(MediaState())
                }
                return@collectLatest _mediaState.collectMedia(
                    data = data,
                    error = error,
                    albumId = albumId,
                    groupByMonth = groupByMonth
                )
            }
        }
    }
    private fun getMedia(albumId: Long = -1L, target: String? = null) {
        viewModelScope.launch {
            mediaUseCases.mediaFlow(albumId, target).flowOn(Dispatchers.IO).collectLatest { result ->
                val data = result.data ?: emptyList()
                if (data == mediaState.value.media) return@collectLatest
                val error = if (result is Resource.Error) result.message
                    ?: "An error occurred" else ""
                if (data.isEmpty()) {
                    return@collectLatest _mediaState.emit(MediaState())
                }
                return@collectLatest _mediaState.collectMedia(
                    data = data,
                    error = error,
                    albumId = albumId,
                    groupByMonth = groupByMonth
                )
            }
        }
    }

    fun videoThumbnailLoad(thumbnailCollect: Boolean, seconds: Long,totalSeconds: Long): Bitmap {
        return if (!thumbnailCollect)
            Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
        else
            mediaUseCases.loadVideoThumbnailUseCase(seconds,totalSeconds)
    }

}