package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository

class ProvideApiUseCase(private val repository: MediaRepository) {
    operator fun invoke() = repository.provideApi()
}
