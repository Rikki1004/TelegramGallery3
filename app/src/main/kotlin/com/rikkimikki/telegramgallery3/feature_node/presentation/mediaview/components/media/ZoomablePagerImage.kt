package com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components.media

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.presentation.common.MediaViewModel
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomablePagerImage(
    modifier: Modifier = Modifier,
    media: Media,
    scrollEnabled: MutableState<Boolean>,
    maxScale: Float = 35f,
    maxImageSize: Int,
    onItemClick: () -> Unit
) {
    //--
    val viewModel = hiltViewModel<MediaViewModel>()
    //--
    val zoomState = rememberZoomState(
        maxScale = maxScale
    )


    val mediaUri = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val item = viewModel.photoLoader(media.id)
        mediaUri.value = item.local.path
        media.path = item.local.path
        media.uri = item.local.path.toUri()
    }


    LaunchedEffect(zoomState.scale) {
        scrollEnabled.value = zoomState.scale == 1f
    }

    Image(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onDoubleClick = {},
                onClick = onItemClick
            )
            .zoomable(
                zoomState = zoomState,
            ),
        painter =
            if (mediaUri.value == null)
                painterResource(id = R.drawable.malevich)
            else rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mediaUri.value)
                    .size(maxImageSize)
                    .build(),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                onSuccess = {
                    zoomState.setContentSize(it.painter.intrinsicSize)
                }
            ),
        contentScale = ContentScale.Fit,
        contentDescription = media.label
    )
}
