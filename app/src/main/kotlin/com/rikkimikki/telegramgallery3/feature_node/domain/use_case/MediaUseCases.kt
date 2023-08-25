package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import android.content.Context
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository

data class MediaUseCases(
    private val context: Context,
    private val repository: MediaRepository
) {
    val getAlbumsUseCase = GetAlbumsUseCase(repository)
    val getAlbumsWithTypeUseCase = GetAlbumsWithTypeUseCase(repository)
    val getMediaUseCase = GetMediaUseCase(repository)
    val getMediaByAlbumUseCase = GetMediaByAlbumUseCase(repository)
    val getMediaByAlbumWithTypeUseCase = GetMediaByAlbumWithTypeUseCase(repository)
    val getMediaFavoriteUseCase = GetMediaFavoriteUseCase(repository)
    val getMediaTrashedUseCase = GetMediaTrashedUseCase(repository)
    val getMediaByTypeUseCase = GetMediaByTypeUseCase(repository)
    val getMediaByUriUseCase = GetMediaByUriUseCase(repository)
    val getMediaListByUrisUseCase = GetMediaListByUrisUseCase(repository)
    val mediaHandleUseCase = MediaHandleUseCase(repository, context)
    val insertPinnedAlbumUseCase = InsertPinnedAlbumUseCase(repository)
    val deletePinnedAlbumUseCase = DeletePinnedAlbumUseCase(repository)

    val authPerformResultUseCase = AuthPerformResultUseCase(repository)
    val authSendPhoneUseCase = AuthSendPhoneUseCase(repository)
    val authSendCodeUseCase = AuthSendCodeUseCase(repository)
    val authSendPasswordUseCase = AuthSendPasswordUseCase(repository)
    val authStartTelegramUseCase = AuthStartTelegramUseCase(repository)

    val indexUploadUseCase = IndexUploadUseCase(repository)
    val indexGetUseCase = IndexGetUseCase(repository)
    val loadThumbnailUseCase = LoadThumbnailUseCase(repository)
    val loadPhotoUseCase = LoadPhotoUseCase(repository)
    val loadVideoUseCase = LoadVideoUseCase(repository)
    val provideApiUseCase = ProvideApiUseCase(repository)
    val cleanOldFilesUseCase = CleanOldFilesUseCase(repository)
    val loadVideoThumbnailUseCase = LoadVideoThumbnailUseCase(repository)
    val prepareVideoThumbnailUseCase = PrepareVideoThumbnailUseCase(repository)
    val getMediaFilteredUseCase = GetMediaFilteredUseCase(repository)
    val getTagsUseCase = GetTagsUseCase(repository)
}