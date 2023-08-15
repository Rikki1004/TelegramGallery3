package com.rikkimikki.telegramgallery3.feature_node.presentation.trashed.components

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.core.Constants
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.use_case.MediaHandleUseCase
import com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components.BottomBarColumn
import com.rikkimikki.telegramgallery3.ui.theme.Black40P
import kotlinx.coroutines.launch

@Composable
fun BoxScope.TrashedViewBottomBar(
    handler: MediaHandleUseCase,
    showUI: Boolean,
    paddingValues: PaddingValues,
    currentMedia: Media?,
    currentIndex: Int,
    result: ActivityResultLauncher<IntentSenderRequest>,
    onDeleteMedia: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = showUI,
        enter = Constants.Animation.enterAnimation(Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION),
        exit = Constants.Animation.exitAnimation(Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION),
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Black40P)
                    )
                )
                .padding(
                    top = 24.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Favourite Component
            BottomBarColumn(
                currentMedia = currentMedia,
                imageVector = Icons.Outlined.RestoreFromTrash,
                title = stringResource(id = R.string.trash_restore)
            ) {
                scope.launch {
                    onDeleteMedia.invoke(currentIndex)
                    handler.trashMedia(result = result, arrayListOf(it), trash = false)
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            // Delete Component
            BottomBarColumn(
                currentMedia = currentMedia,
                imageVector = Icons.Outlined.DeleteOutline,
                title = stringResource(id = R.string.trash_delete)
            ) {
                scope.launch {
                    onDeleteMedia.invoke(currentIndex)
                    handler.deleteMedia(result = result, arrayListOf(it))
                }
            }
        }
    }
}