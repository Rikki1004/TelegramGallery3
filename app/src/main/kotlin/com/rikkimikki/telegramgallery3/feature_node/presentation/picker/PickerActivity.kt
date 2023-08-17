package com.rikkimikki.telegramgallery3.feature_node.presentation.picker

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.core.Constants
import com.rikkimikki.telegramgallery3.feature_node.presentation.picker.components.PickerScreen
import com.rikkimikki.telegramgallery3.ui.theme.GalleryTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val type = intent.type
        val allowMultiple = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

        var title = getString(R.string.select)
        title += " " + if (allowMultiple) {
            if (type.pickAny) getString(R.string.photos_and_videos)
            else if (type.pickImage)getString(R.string.photos)
            else getString(R.string.videos)
        } else {
            if (type.pickImage) getString(R.string.photo)
            else if (type.pickVideo)getString(R.string.video)
            else getString(R.string.photos_and_videos)
        }
        setContent {
            GalleryTheme {
                PickerRootScreen(title, type.allowedMedia, allowMultiple)
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun PickerRootScreen(title: String, allowedMedia: AllowedMedia, allowMultiple: Boolean) {
        val mediaPermissions =
            rememberMultiplePermissionsState(Constants.PERMISSIONS)
        if (!mediaPermissions.allPermissionsGranted) {
            LaunchedEffect(Unit) {
                mediaPermissions.launchMultiplePermissionRequest()
            }
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title) },
                    navigationIcon = {
                        IconButton(onClick = ::finish) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = getString(R.string.close)
                            )
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(top = it.calculateTopPadding()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PickerScreen(
                    allowedMedia = allowedMedia,
                    allowSelection = allowMultiple,
                    sendMediaAsResult = ::sendMediaAsResult
                )
            }
        }
    }

    private fun sendMediaAsResult(selectedMedia: List<Uri>) {
        val newIntent = Intent().apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            data = selectedMedia[0]
        }
        if (selectedMedia.size == 1)
            setResult(RESULT_OK, newIntent)
        else {
            val newClipData = ClipData.newUri(contentResolver, null, selectedMedia[0])
            for (nextUri in selectedMedia.stream().skip(1)) {
                newClipData.addItem(contentResolver, ClipData.Item(nextUri))
            }
            newIntent.clipData = newClipData
            setResult(RESULT_OK, newIntent)
        }
        finish()
    }

    private val String?.pickImage: Boolean get() = this?.startsWith("image") ?: false
    private val String?.pickVideo: Boolean get() = this?.startsWith("video") ?: false
    private val String?.pickAny: Boolean get() = this == "*/*"
    private val String?.allowedMedia: AllowedMedia
        get() = if (pickImage) AllowedMedia.PHOTOS
        else if (pickVideo) AllowedMedia.VIDEOS
        else AllowedMedia.BOTH
}