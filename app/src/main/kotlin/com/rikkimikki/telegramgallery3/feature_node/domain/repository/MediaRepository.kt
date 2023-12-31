package com.rikkimikki.telegramgallery3.feature_node.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramFlow
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Album
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Index
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.model.PinnedAlbum
import com.rikkimikki.telegramgallery3.feature_node.domain.util.AuthState
import com.rikkimikki.telegramgallery3.feature_node.domain.util.MediaOrder
import com.rikkimikki.telegramgallery3.feature_node.presentation.picker.AllowedMedia
import kotlinx.coroutines.flow.Flow
import org.drinkless.td.libcore.telegram.TdApi

interface MediaRepository {

    fun getMedia(): Flow<Resource<List<Media>>>
    fun getMediaFiltered(q: String): Flow<Resource<List<Media>>>

    fun getMediaByType(allowedMedia: AllowedMedia): Flow<Resource<List<Media>>>

    fun getFavorites(mediaOrder: MediaOrder): Flow<Resource<List<Media>>>

    fun getTrashed(mediaOrder: MediaOrder): Flow<Resource<List<Media>>>

    fun getAlbums(mediaOrder: MediaOrder): Flow<Resource<List<Album>>>

    suspend fun insertPinnedAlbum(pinnedAlbum: PinnedAlbum)

    suspend fun removePinnedAlbum(pinnedAlbum: PinnedAlbum)

    suspend fun getMediaById(mediaId: Long): Media?

    fun getMediaByAlbumId(albumId: Long): Flow<Resource<List<Media>>>

    fun getMediaByAlbumIdWithType(albumId: Long, allowedMedia: AllowedMedia): Flow<Resource<List<Media>>>

    fun getAlbumsWithType(allowedMedia: AllowedMedia): Flow<Resource<List<Album>>>

    fun getMediaByUri(uriAsString: String, isSecure: Boolean): Flow<Resource<List<Media>>>

    fun getMediaListByUris(listOfUris: List<Uri>): Flow<Resource<List<Media>>>

    suspend fun toggleFavorite(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        favorite: Boolean
    )

    suspend fun trashMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        trash: Boolean
    )

    suspend fun deleteMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>
    )


    fun checkAuthState() : Flow<AuthState>
    fun startTelegram()
    suspend fun authSendPhone(phone: String)
    suspend fun authSendCode(code: String)
    suspend fun authSendPassword(password: String)
    suspend fun uploadIndex()
    suspend fun getIndex(): Index
    suspend fun loadThumbnail(messageId: Long): TdApi.File
    suspend fun loadPhoto(messageId: Long): TdApi.File
    suspend fun loadVideo(messageId: Long): TdApi.File
    fun provideApi(): TelegramFlow
    fun getVideoThumbnail(seconds: Long, totalSeconds: Long): Bitmap
    suspend fun prepareVideoThumbnail(messageId:Long)
    fun getTags(): List<String>
    fun cleaner()

}