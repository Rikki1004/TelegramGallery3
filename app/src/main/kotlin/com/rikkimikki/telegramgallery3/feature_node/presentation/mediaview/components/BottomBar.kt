package com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components

import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.core.Constants
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.domain.use_case.MediaHandleUseCase
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.AppBottomSheetState
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.ExifMetadata
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.getDate
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.getExifInterface
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.launchEditIntent
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.launchOpenWithIntent
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.launchUseAsIntent
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.shareMedia
import com.rikkimikki.telegramgallery3.ui.theme.Black40P
import com.rikkimikki.telegramgallery3.ui.theme.Shapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.MediaViewBottomBar(
    showDeleteButton: Boolean = true,
    bottomSheetState: AppBottomSheetState,
    handler: MediaHandleUseCase,
    showUI: Boolean,
    paddingValues: PaddingValues,
    currentMedia: Media?,
    currentIndex: Int = 0,
    result: ActivityResultLauncher<IntentSenderRequest>? = null,
    onDeleteMedia: ((Int) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    currentMedia?.let {

            val metadataList = remember(currentMedia) {
                currentMedia.retrieveMetadata(context)
            }
            if (bottomSheetState.isVisible) {
                ModalBottomSheet(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    onDismissRequest = {
                        scope.launch {
                            bottomSheetState.hide()
                        }
                    },
                    shape = Shapes.extraSmall,
                    sheetState = bottomSheetState.sheetState,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    dragHandle = null
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        MediaViewDateContainer(
                            currentMedia = currentMedia,
                        ) {
                            for (metadata in metadataList) {
                                MediaInfoRow(
                                    label = metadata.label,
                                    content = metadata.content,
                                    icon = metadata.icon
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        //LocationInfo(exifMetadata = exifMetadata)
                        //Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

        BackHandler(bottomSheetState.isVisible) {
            scope.launch {
                bottomSheetState.hide()
            }
        }
    }
}

@Composable
private fun MediaViewDateContainer(
    currentMedia: Media,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = currentMedia.timestamp.getDate(Constants.HEADER_DATE_FORMAT),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Image(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.edit_cd),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
        content()
    }
}

@Composable
fun BottomBarColumn(
    currentMedia: Media?,
    imageVector: ImageVector,
    title: String,
    onItemClick: (Media) -> Unit
) {
    val tintColor = Color.White
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .defaultMinSize(
                minWidth = 90.dp,
                minHeight = 80.dp
            )
            .clickable {
                currentMedia?.let {
                    onItemClick.invoke(it)
                }
            }
            .padding(top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = imageVector,
            colorFilter = ColorFilter.tint(tintColor),
            contentDescription = title,
            modifier = Modifier
                .height(32.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = title,
            modifier = Modifier,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = tintColor,
            textAlign = TextAlign.Center
        )
    }
}