package com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components.video

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramException
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.presentation.common.MediaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlin.time.Duration.Companion.seconds


@SuppressLint("OpaqueUnitKey")
@OptIn(ExperimentalFoundationApi::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    media: Media,
    playWhenReady: Boolean,
    videoController: @Composable (ExoPlayer, MutableState<Long>, Long, Int,Boolean, () -> Unit, (Long,Long) -> Bitmap) -> Unit,
    onItemClick: () -> Unit
) {

    var totalDuration by remember { mutableStateOf(0L) }
    val currentTime = rememberSaveable { mutableStateOf(0L) }
    var bufferedPercentage by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    val context = LocalContext.current

    //--
    val viewModel = hiltViewModel<MediaViewModel>()
    var thumbnailCollect by remember{mutableStateOf(false)}
    //--
    val zoomState = rememberZoomState(
        maxScale = 30f
    )

    val videoInfo = remember { mutableStateOf<Triple<Int,Int,String>?>(null) }
    LaunchedEffect(Unit) {
        val item = viewModel.videoLoader(media.id)
        videoInfo.value = Triple(item.id, item.size, item.local.path)
        media.path = item.local.path
        media.uri = item.local.path.toUri()
        /*launch {
            media.thumbnailMsgId?.let { videoPreviewPath = viewModel.photoLoader(it).local.path}
        }*/
    }
    LaunchedEffect(Unit) {
        media.thumbnailMsgId?.let {
            try {
                viewModel.thumbPreparer(it)
                thumbnailCollect = true
            } catch (e: TelegramException){
                e.printStackTrace()
            }
        }
    }

    val temp = videoInfo.value
    if (temp == null){
        Box (
            modifier = Modifier.fillMaxSize()
        )
    } else {
        val exoPlayer = remember(context)  {
            val loadControl: LoadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    5000,
                    10000,
                    5000,
                    5000
                )
                .build()

            ExoPlayer.Builder(context)
                //.setLoadControl(loadControl)
                .build().apply {
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                    repeatMode = Player.REPEAT_MODE_ONE
                    setMediaItem(MediaItem.fromUri("http://localhost:8081/"+temp.first))
                    prepare()
                    setPlayWhenReady(playWhenReady)
                    play()
                }
        }

        DisposableEffect(
            Box {
                AndroidView(
                    modifier = Modifier.zoomable(
                        zoomState
                    ).combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onItemClick,
                    ),
                    factory = {
                        PlayerView(context).apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

                            player = exoPlayer
                            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        }
                    }
                )
                videoController(
                    exoPlayer,
                    currentTime,
                    totalDuration,
                    bufferedPercentage,
                    media.thumbnailMsgId != null,
                    {
                        if (exoPlayer.isPlaying){
                            exoPlayer.pause()
                            isPlaying = false
                        }
                        else{
                            exoPlayer.play()
                            isPlaying = true
                        }
                    },
                    { currentTime, duration ->
                        viewModel.videoThumbnailLoad(thumbnailCollect , currentTime, duration)
                    }
                )
            }
        ) {
            exoPlayer.addListener(
                object : Player.Listener {

                    override fun onEvents(player: Player, events: Player.Events) {
                        totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                    }
                }
            )
            onDispose {
                exoPlayer.release()
            }
        }

        if (isPlaying) {
            LaunchedEffect(Unit) {
                while (true) {
                    currentTime.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                    bufferedPercentage = exoPlayer.bufferedPercentage
                    delay(1.seconds / 30)
                }
            }
        }
    }



}