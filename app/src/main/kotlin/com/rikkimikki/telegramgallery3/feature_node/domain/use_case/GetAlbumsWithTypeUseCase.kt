package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Album
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import com.rikkimikki.telegramgallery3.feature_node.presentation.picker.AllowedMedia
import kotlinx.coroutines.flow.Flow

class GetAlbumsWithTypeUseCase(
    private val repository: MediaRepository
) {

    operator fun invoke(
        allowedMedia: AllowedMedia
    ): Flow<Resource<List<Album>>> = repository.getAlbumsWithType(allowedMedia)
}