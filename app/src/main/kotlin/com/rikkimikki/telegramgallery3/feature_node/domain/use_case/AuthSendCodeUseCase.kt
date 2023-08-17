package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository

class AuthSendCodeUseCase(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(
        code: String
    ) {
        return repository.authSendCode(code)
    }

}