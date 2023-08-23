package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import android.graphics.Bitmap
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository

class PrepareVideoThumbnailUseCase(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(messageId:Long) {
        return repository.prepareVideoThumbnail(messageId)
    }
}