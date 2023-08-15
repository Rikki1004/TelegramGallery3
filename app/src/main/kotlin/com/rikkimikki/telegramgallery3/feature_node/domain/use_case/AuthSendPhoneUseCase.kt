package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository

class AuthSendPhoneUseCase(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(
        phone: String
    ) {
        return repository.authSendPhone(phone)
    }

}