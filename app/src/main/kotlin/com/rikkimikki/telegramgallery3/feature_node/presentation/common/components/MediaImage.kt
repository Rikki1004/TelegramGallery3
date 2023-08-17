package com.rikkimikki.telegramgallery3.feature_node.presentation.common.components

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DecodeFormat
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.core.Constants.Animation
import com.rikkimikki.telegramgallery3.core.MediaKey
import com.rikkimikki.telegramgallery3.core.presentation.components.CheckBox
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.presentation.common.MediaViewModel
import com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components.video.VideoDurationHeader
import com.rikkimikki.telegramgallery3.ui.theme.Dimens

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MediaImage(
    media: Media,
    preloadRequestBuilder: RequestBuilder<Drawable>,
    selectionState: MutableState<Boolean>,
    selectedMedia: SnapshotStateList<Media>,
    isSelected: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    //--
    val viewModel = hiltViewModel<MediaViewModel>()
    //--
    if (!selectionState.value) {
        isSelected.value = false
    } else {
        isSelected.value = selectedMedia.find { it.id == media.id } != null
    }
    val selectedSize by animateDpAsState(
        if (isSelected.value) 12.dp else 0.dp, label = "selectedSize"
    )
    val scale by animateFloatAsState(
        if (isSelected.value) 0.5f else 1f, label = "scale"
    )
    val selectedShapeSize by animateDpAsState(
        if (isSelected.value) 16.dp else 0.dp, label = "selectedShapeSize"
    )
    val strokeSize by animateDpAsState(
        targetValue = if (isSelected.value) 2.dp else 0.dp, label = "strokeSize"
    )
    val strokeColor by animateColorAsState(
        targetValue = if (isSelected.value) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "strokeColor"
    )
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .size(Dimens.Photo())
    ) {

        val mediaUri = remember { mutableStateOf<String?>(null) }
        LaunchedEffect(Unit) {
            val item = viewModel.thumbLoader(media.id)
            mediaUri.value = item.local.path
            //media.path = item.local.path
            //media.uri = item.local.path.toUri()
        }

        GlideImage(
            modifier = Modifier
                .fillMaxSize()
                .padding(selectedSize)
                .clip(RoundedCornerShape(selectedShapeSize))
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(selectedShapeSize)
                )
                .border(
                    width = strokeSize,
                    shape = RoundedCornerShape(selectedShapeSize),
                    color = strokeColor
                ),
            //model = media.uri,
            model = mediaUri.value?: R.drawable.malevich,
            contentDescription = media.label,
            contentScale = ContentScale.Crop,
        ) /*{
            it.thumbnail(preloadRequestBuilder)
                .signature(MediaKey(media.id, media.timestamp, media.mimeType, media.orientation))
                .format(DecodeFormat.PREFER_RGB_565)
                .override(270)
        }*/

        AnimatedVisibility(
            visible = media.duration != null,
            enter = Animation.enterAnimation,
            exit = Animation.exitAnimation,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            VideoDurationHeader(
                modifier = Modifier
                    .padding(selectedSize / 2)
                    .scale(scale),
                media = media
            )
        }

        AnimatedVisibility(
            visible = media.favorite == 1,
            enter = Animation.enterAnimation,
            exit = Animation.exitAnimation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            Image(
                modifier = Modifier
                    .padding(selectedSize / 2)
                    .scale(scale)
                    .padding(8.dp)
                    .size(16.dp),
                imageVector = Icons.Filled.Favorite,
                colorFilter = ColorFilter.tint(Color.Red),
                contentDescription = null
            )
        }

        AnimatedVisibility(
            visible = selectionState.value,
            enter = Animation.enterAnimation,
            exit = Animation.exitAnimation
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                CheckBox(isChecked = isSelected.value)
            }
        }
    }
}
