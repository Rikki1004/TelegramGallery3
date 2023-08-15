package com.rikkimikki.telegramgallery3.feature_node.domain.use_case

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.rikkimikki.telegramgallery3.core.Settings.Misc.getTrashEnabled
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MediaHandleUseCase(
    private val repository: MediaRepository,
    private val context: Context
) {

    suspend fun toggleFavorite(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        favorite: Boolean
    ) = repository.toggleFavorite(result, mediaList, favorite)

    suspend fun toggleFavorite(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>
    ) {
        val turnToFavorite = mediaList.filter { it.favorite == 0 }
        val turnToNotFavorite = mediaList.filter { it.favorite == 1 }
        if (turnToFavorite.isNotEmpty()) {
            repository.toggleFavorite(result, turnToFavorite, true)
        }
        if (turnToNotFavorite.isNotEmpty()) {
            repository.toggleFavorite(result, turnToNotFavorite, false)
        }
    }

    suspend fun trashMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        trash: Boolean = true
    ) = withContext(Dispatchers.Default) {
        val isTrashEnabled = getTrashEnabled(context).first()
        /**
         * Trash media only if user enabled the Trash Can
         * Or if user wants to remove existing items from the trash
         * */
        return@withContext if (isTrashEnabled || !trash)
            repository.trashMedia(result, mediaList, trash)
        else {
            repository.deleteMedia(result, mediaList)
        }
    }

    suspend fun deleteMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>
    ) = repository.deleteMedia(result, mediaList)
}