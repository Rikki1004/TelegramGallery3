package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class AuthSendPasswordUseCase(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(
        password: String
    ) {
        return repository.authSendPassword(password)
    }

}

