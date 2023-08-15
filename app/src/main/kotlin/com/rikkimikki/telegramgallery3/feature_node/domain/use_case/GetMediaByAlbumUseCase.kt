package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import com.rikkimikki.telegramgallery3.feature_node.domain.util.MediaOrder
import com.rikkimikki.telegramgallery3.feature_node.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetMediaByAlbumUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(
        albumId: Long,
        mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending)
    ): Flow<Resource<List<Media>>> {
        return repository.getMediaByAlbumId(albumId).map {
            it.apply {
                data = data?.let { it1 -> mediaOrder.sortMedia(it1) }
            }
        }
    }

}

