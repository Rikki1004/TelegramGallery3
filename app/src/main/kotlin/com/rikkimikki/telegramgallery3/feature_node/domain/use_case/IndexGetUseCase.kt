package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository


class IndexGetUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke() = repository.getIndex()
}

