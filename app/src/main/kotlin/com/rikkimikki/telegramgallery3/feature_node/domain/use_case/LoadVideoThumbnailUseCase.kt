package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import android.graphics.Bitmap
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository

class LoadVideoThumbnailUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(seconds: Long,totalSeconds: Long): Bitmap {
        return repository.getVideoThumbnail(seconds,totalSeconds)
    }
}