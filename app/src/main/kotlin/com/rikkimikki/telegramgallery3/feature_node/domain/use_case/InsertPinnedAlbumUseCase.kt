package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import com.rikkimikki.telegramgallery3.feature_node.domain.model.Album
import com.rikkimikki.telegramgallery3.feature_node.domain.model.PinnedAlbum
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository

class InsertPinnedAlbumUseCase(
    private val repository: MediaRepository
) {

    suspend operator fun invoke(
        album: Album
    ) = repository.insertPinnedAlbum(PinnedAlbum(album.id))
}