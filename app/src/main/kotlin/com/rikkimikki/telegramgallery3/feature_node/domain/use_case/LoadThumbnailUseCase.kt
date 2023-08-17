package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository

class LoadThumbnailUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(messageId:Long) = repository.loadThumbnail(messageId)
}
