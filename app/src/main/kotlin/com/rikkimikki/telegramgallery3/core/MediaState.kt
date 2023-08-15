package com.rikkimikki.telegramgallery3.core

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Album
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.model.MediaItem
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class MediaState(
    val media: List<Media> = emptyList(),
    val mappedMedia: List<MediaItem> = emptyList(),
    val mappedMediaWithMonthly: List<MediaItem> = emptyList(),
    val dateHeader: String = "",
    val error: String = "",
    val isLoading: Boolean = false
) : Parcelable


@Immutable
@Parcelize
data class AlbumState(
    val albums: List<Album> = emptyList(),
    val error: String = ""
) : Parcelable