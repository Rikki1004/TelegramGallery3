package com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLocation
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.rikkimikki.telegramgallery3.BuildConfig
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.core.Constants.TAG
import com.rikkimikki.telegramgallery3.feature_node.domain.model.Media
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.ConnectionState
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.ExifMetadata
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.MapBoxURL
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.connectivityState
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.formatMinSec
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.formattedFileSize
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.launchMap
import com.rikkimikki.telegramgallery3.ui.theme.Shapes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.io.IOException

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaInfoRow(
    modifier: Modifier = Modifier,
    label: String,
    content: String,
    icon: ImageVector,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.medium)
            .combinedClickable(
                onClick = { onClick?.let { it() } },
                onLongClick = {
                    if (onLongClick != null) onLongClick()
                    else {
                        clipboardManager.setText(AnnotatedString(content))
                    }
                }
            ),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        headlineContent = {
            Text(
                text = label,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(text = content)
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    )
}

@OptIn(
    ExperimentalCoroutinesApi::class,
    ExperimentalGlideComposeApi::class
)
@Composable
fun LocationInfo(exifMetadata: ExifMetadata) {
    if (exifMetadata.gpsLatLong != null) {
        val context = LocalContext.current
        val lat = exifMetadata.gpsLatLong[0]
        val long = exifMetadata.gpsLatLong[1]
        val connection by connectivityState()
        val isConnected = connection == ConnectionState.Available
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shapes.large)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (isConnected && BuildConfig.MAPS_TOKEN != "DEBUG") {
                GlideImage(
                    model = MapBoxURL(
                        latitude = lat,
                        longitude = long,
                        darkTheme = isSystemInDarkTheme()
                    ),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = stringResource(R.string.location_map_cd),
                    modifier = Modifier
                        .clip(Shapes.large)
                        .fillMaxWidth()
                        .height(196.dp)
                        .clickable { context.launchMap(lat, long) }
                )
            }
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.location),
                        fontWeight = FontWeight.Medium
                    )
                },
                supportingContent = {
                    Text(
                        text = String.format(
                            "%.3f, %.3f",
                            exifMetadata.gpsLatLong[0], exifMetadata.gpsLatLong[1]
                        ),
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.edit_location_cd)
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )
        }
    } else {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(R.string.add_location_cd),
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.AddLocation,
                    contentDescription = stringResource(R.string.add_location_cd)
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(100)),
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

data class InfoRow(
    val label: String,
    val content: String,
    val icon: ImageVector,
    val contentDescription: String? = null,
    val onClick: (() -> Unit)? = null,
    val onLongClick: (() -> Unit)? = null,
)

fun Media.retrieveMetadata(context: Context): List<InfoRow> {
    val infoList = ArrayList<InfoRow>()
    if (trashed == 1) {
        infoList.apply {
            add(
                InfoRow(
                    icon = Icons.Outlined.Photo,
                    label = context.getString(R.string.label),
                    content = label
                )
            )
            add(
                InfoRow(
                    icon = Icons.Outlined.Info,
                    label = context.getString(R.string.path),
                    content = path
                )
            )
        }
        return infoList
    }
    try {
        infoList.apply {
            /*add(
                InfoRow(
                    icon = Icons.Outlined.Camera,
                    label = this@retrieveMetadata.duration,
                    content = stringBuilder.toString()
                )
            )*/
            add(
                InfoRow(
                    icon = Icons.Outlined.Photo,
                    label = context.getString(R.string.label),
                    content = label
                )
            )
            val formattedFileSize = this@retrieveMetadata.size.formattedFileSize(context)

            val contentString = StringBuilder()
            contentString.append(formattedFileSize)
            if (mimeType.contains("video"))
                contentString.append(" • ${duration.formatMinSec()}")
            add(
                InfoRow(
                    icon = Icons.Outlined.VideoFile,
                    label = context.getString(R.string.metadata),
                    content = contentString.toString()
                )
            )

            add(
                InfoRow(
                    icon = Icons.Outlined.Info,
                    label = context.getString(R.string.path),
                    content = path.substringBeforeLast("/")
                )
            )
        }
    } catch (e: IOException) {
        Log.e(TAG, "ExifInterface ERROR\n" + e.printStackTrace())
    }

    return infoList
}