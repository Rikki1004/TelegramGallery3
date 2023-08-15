package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import com.rikkimikki.telegramgallery3.feature_node.presentation.picker.AllowedMedia
import kotlinx.coroutines.flow.Flow

class GetMediaByTypeUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(type: AllowedMedia): Flow<Resource<List<Media>>> = repository.getMediaByType(type)

}