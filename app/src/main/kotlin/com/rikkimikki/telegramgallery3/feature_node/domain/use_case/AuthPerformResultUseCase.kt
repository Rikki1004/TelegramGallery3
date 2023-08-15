package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import com.rikkimikki.telegramgallery3.feature_node.domain.util.AuthState
import kotlinx.coroutines.flow.Flow

class AuthPerformResultUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<AuthState>{
        return repository.checkAuthState()
    }
}