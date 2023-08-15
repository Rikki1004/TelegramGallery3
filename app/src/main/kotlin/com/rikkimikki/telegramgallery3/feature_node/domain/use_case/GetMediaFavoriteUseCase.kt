package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.core.Resource
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import com.rikkimikki.telegramgallery3.feature_node.domain.util.MediaOrder
import com.rikkimikki.telegramgallery3.feature_node.domain.util.OrderType
import kotlinx.coroutines.flow.Flow

class GetMediaFavoriteUseCase(
    private val repository: MediaRepository
) {

    operator fun invoke(
        mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending)
    ): Flow<Resource<List<Media>>> = repository.getFavorites(mediaOrder)

}