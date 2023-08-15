package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import android.net.Uri
import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetMediaListByUrisUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(
        listOfUris: List<Uri>
    ): Flow<Resource<List<Media>>> {
        return repository.getMediaListByUris(listOfUris)
    }

}

