package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetMediaUseCase(
    private val repository: MediaRepository
) {

    operator fun invoke(): Flow<Resource<List<Media>>> = repository.getMedia()

}