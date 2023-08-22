package com.rikkimikki.telegramgallery3.feature_node.presentation.util

sealed class Screen(val route: String) {
    object TimelinePhotoScreen : Screen("timeline_screen1")
    object TimelineVideoScreen : Screen("timeline_screen2")
    object AlbumsScreen : Screen("albums_screen")

    object AlbumViewScreen : Screen("album_view_screen")
    object MediaViewScreen : Screen("media_screen")

    object TrashedScreen : Screen("trashed_screen")
    object FavoriteScreen : Screen("favorite_screen")

    object SettingsScreen : Screen("settings_screen")
    object AlbumSizeScreen: Screen("album_size_screen")

    operator fun invoke() = route
}
