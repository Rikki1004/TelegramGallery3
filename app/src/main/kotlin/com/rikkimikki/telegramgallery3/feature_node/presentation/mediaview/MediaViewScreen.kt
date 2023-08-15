package com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import com.rikkimikki.telegramgallery3.core.Constants
import com.rikkimikki.telegramgallery3.core.Constants.Animation.enterAnimation
import com.rikkimikki.telegramgallery3.core.Constants.Animation.exitAnimation
import com.rikkimikki.telegramgallery3.core.Constants.DEFAULT_LOW_VELOCITY_SWIPE_DURATION
import com.rikkimikki.telegramgallery3.core.Constants.HEADER_DATE_FORMAT
import com.rikkimikki.telegramgallery3.core.Constants.Target.TARGET_TRASH
import com.rikkimikki.telegramgallery3.core.MediaState
import com.rikkimikki.telegramgallery3.core.Settings.Glide.rememberMaxImageSize
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.use_case.MediaHandleUseCase
import com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components.MediaViewAppBar
import com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components.MediaViewBottomBar
import com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components.media.MediaPreviewComponent
import com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components.video.VideoPlayerController
import com.rikkimikki.telegramgallery3.feature_node.presentation.trashed.components.TrashedViewBottomBar
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.getDate
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.rememberAppBottomSheetState
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.rememberWindowInsetsController
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.toggleSystemBars
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewScreen(
    navigateUp: () -> Unit,
    toggleRotate: () -> Unit,
    paddingValues: PaddingValues,
    isStandalone: Boolean = false,
    mediaId: Long,
    target: String? = null,
    mediaState: StateFlow<MediaState>,
    handler: MediaHandleUseCase
) {
    var runtimeMediaId by rememberSaveable(mediaId) { mutableStateOf(mediaId) }
    val state by mediaState.collectAsStateWithLifecycle()
    val initialPage = rememberSaveable(runtimeMediaId) {
        state.media.indexOfFirst { it.id == runtimeMediaId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        initialPageOffsetFraction = 0f,
        pageCount = state.media::size
    )
    val scrollEnabled = rememberSaveable { mutableStateOf(true) }
    val bottomSheetState = rememberAppBottomSheetState()

    val currentDate = rememberSaveable { mutableStateOf("") }
    val currentMedia = rememberSaveable { mutableStateOf<Media?>(null) }

    val showUI = rememberSaveable { mutableStateOf(true) }
    val maxImageSize by rememberMaxImageSize()
    val windowInsetsController = rememberWindowInsetsController()

    val result = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {}
    )
    val lastIndex = remember { mutableStateOf(-1) }
    val updateContent: (Int) -> Unit = { page ->
        if (state.media.isNotEmpty()) {
            val index = if (page == -1) 0 else page
            if (lastIndex.value != -1)
                runtimeMediaId = state.media[lastIndex.value.coerceAtMost(state.media.size - 1)].id
            currentDate.value = state.media[index].timestamp.getDate(HEADER_DATE_FORMAT)
            currentMedia.value = state.media[index]
        } else if (!isStandalone) navigateUp()
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            updateContent(page)
        }
    }

    LaunchedEffect(state.media) {
        updateContent(pagerState.currentPage)
    }

    BackHandler(!showUI.value) {
        windowInsetsController.toggleSystemBars(show = true)
        navigateUp()
    }

    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = scrollEnabled.value,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                lowVelocityAnimationSpec = tween(
                    easing = LinearEasing,
                    durationMillis = DEFAULT_LOW_VELOCITY_SWIPE_DURATION
                )
            ),
            key = { index -> state.media[index.coerceAtMost(state.media.size - 1)].toString() },
            pageSpacing = 16.dp,
        ) { index ->
            MediaPreviewComponent(
                media = state.media[index],
                scrollEnabled = scrollEnabled,
                maxImageSize = maxImageSize,
                playWhenReady = index == pagerState.currentPage,
                onItemClick = {
                    showUI.value = !showUI.value
                    windowInsetsController.toggleSystemBars(showUI.value)
                }
            ) { player: ExoPlayer, currentTime: MutableState<Long>, totalTime: Long, buffer: Int, playToggle: () -> Unit ->
                AnimatedVisibility(
                    visible = showUI.value,
                    enter = enterAnimation(Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION),
                    exit = exitAnimation(Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION),
                    modifier = Modifier.fillMaxSize()
                ) {
                    VideoPlayerController(
                        paddingValues = paddingValues,
                        player = player,
                        currentTime = currentTime,
                        totalTime = totalTime,
                        buffer = buffer,
                        playToggle = playToggle,
                        toggleRotate = toggleRotate
                    )
                }
            }
        }
        MediaViewAppBar(
            showUI = showUI.value,
            showInfo = currentMedia.value?.trashed == 0 && !(currentMedia.value?.readUriOnly() ?: false),
            showDate = currentMedia.value?.timestamp != 0L,
            currentDate = currentDate.value,
            bottomSheetState = bottomSheetState,
            paddingValues = paddingValues,
            onGoBack = navigateUp
        )
        if (target == TARGET_TRASH) {
            TrashedViewBottomBar(
                handler = handler,
                showUI = showUI.value,
                paddingValues = paddingValues,
                currentMedia = currentMedia.value,
                currentIndex = pagerState.currentPage,
                result = result,
            ) {
                lastIndex.value = it
            }
        } else {
            MediaViewBottomBar(
                showDeleteButton = !isStandalone,
                bottomSheetState = bottomSheetState,
                handler = handler,
                showUI = showUI.value,
                paddingValues = paddingValues,
                currentMedia = currentMedia.value,
                currentIndex = pagerState.currentPage,
                result = result,
            ) {
                lastIndex.value = it
            }
        }
    }

}