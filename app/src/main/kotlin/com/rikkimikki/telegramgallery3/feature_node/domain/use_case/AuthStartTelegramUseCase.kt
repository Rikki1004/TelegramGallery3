package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import com.rikkimikki.telegramgallery3.feature_node.domain.util.AuthState
import kotlinx.coroutines.flow.Flow

class AuthStartTelegramUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(){
        return repository.startTelegram()
    }
}