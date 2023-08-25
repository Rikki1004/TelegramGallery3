package com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components.video

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.formatMinSec
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerController(
    paddingValues: PaddingValues,
    player: ExoPlayer,
    currentTime: MutableState<Long>,
    totalTime: Long,
    buffer: Int,
    hasPreview: Boolean,
    playToggle: () -> Unit,
    toggleRotate: () -> Unit,
    onLoadPreview: (Long,Long) -> Bitmap
) {
    val context = LocalContext.current
    var currentValue by rememberSaveable(currentTime.value) { mutableStateOf(currentTime.value) }
    var isImageVisible by remember { mutableStateOf(false) }

    var currentPreview by remember{ mutableStateOf(BitmapFactory.decodeResource (context.resources,R.drawable.malevich)) }


    var job: Job? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp)
                //.padding(bottom = paddingValues.calculateBottomPadding() + 72.dp)
                .padding(bottom = paddingValues.calculateBottomPadding())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            if (hasPreview && isImageVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        //painter = painterResource(id = R.drawable.image_sample_1),
                        //painter = currentPreview,
                        painter = rememberAsyncImagePainter(currentPreview),
                        contentDescription = "preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp, 70.dp)
                    )
                }
            }
            IconButton(
                onClick = { toggleRotate() },
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ScreenRotation,
                    tint = Color.White,
                    contentDescription = stringResource(
                        R.string.rotate_screen_cd
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    modifier = Modifier.width(52.dp),
                    text = currentValue.formatMinSec(),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Box(Modifier.weight(1f)) {
                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = buffer.toFloat(),
                        enabled = false,
                        onValueChange = {},
                        valueRange = 0f..100f,
                        colors =
                        SliderDefaults.colors(
                            disabledThumbColor = Color.Transparent,
                            disabledInactiveTrackColor = Color.DarkGray.copy(alpha = 0.4f),
                            disabledActiveTrackColor = Color.Gray
                        )
                    )

                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = currentValue.toFloat(),
                        onValueChange = {
                            job?.cancel()
                            currentValue = it.toLong()

                            job = coroutineScope.launch {
                                delay(150)
                                onLoadPreview(currentValue,totalTime).let {arr ->
                                    if (arr.byteCount > 1)
                                        currentPreview = arr
                                }
                            }

                            isImageVisible = true
                            if (player.isPlaying)
                                playToggle()
                        },
                        onValueChangeFinished = {
                            player.seekTo(currentValue)
                            isImageVisible = false
                            if (!player.isPlaying)
                                playToggle()
                        },
                        valueRange = 0f..totalTime.toFloat(),
                        colors =
                        SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            activeTickColor = Color.White,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }
                Text(
                    modifier = Modifier.width(52.dp),
                    text = totalTime.formatMinSec(),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (!isImageVisible){
            IconButton(
                onClick = { playToggle.invoke() },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
            ) {
                if (player.isPlaying) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = Icons.Filled.PauseCircleFilled,
                        contentDescription = stringResource(R.string.pause_video),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                } else {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = Icons.Filled.PlayCircleFilled,
                        contentDescription = stringResource(R.string.play_video),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }
    }
}