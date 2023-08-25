package com.rikkimikki.telegramgallery3.core.presentation.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.core.Constants.Animation.navigateInAnimation
import com.rikkimikki.telegramgallery3.core.Constants.Animation.navigateUpAnimation
import com.rikkimikki.telegramgallery3.core.Constants.Target.TARGET_FAVORITES
import com.rikkimikki.telegramgallery3.core.Constants.Target.TARGET_TRASH
import com.rikkimikki.telegramgallery3.core.Settings.Misc.rememberTimelineGroupByMonth
import com.rikkimikki.telegramgallery3.feature_node.presentation.albums.AlbumsScreen
import com.rikkimikki.telegramgallery3.feature_node.presentation.albums.AlbumsViewModel
import com.rikkimikki.telegramgallery3.feature_node.presentation.common.ChanneledViewModel
import com.rikkimikki.telegramgallery3.feature_node.presentation.common.MediaViewModel
import com.rikkimikki.telegramgallery3.feature_node.presentation.favorites.FavoriteScreen
import com.rikkimikki.telegramgallery3.feature_node.presentation.mediaview.MediaViewScreen
import com.rikkimikki.telegramgallery3.feature_node.presentation.settings.SettingsScreen
import com.rikkimikki.telegramgallery3.feature_node.presentation.settings.SettingsViewModel
import com.rikkimikki.telegramgallery3.feature_node.presentation.settings.customization.albumsize.AlbumSizeScreen
import com.rikkimikki.telegramgallery3.feature_node.presentation.timeline.TimelineScreen
import com.rikkimikki.telegramgallery3.feature_node.presentation.trashed.TrashedGridScreen
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.Screen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationComp(
    navController: NavHostController,
    paddingValues: PaddingValues,
    bottomBarState: MutableState<Boolean>,
    systemBarFollowThemeState: MutableState<Boolean>,
    windowSizeClass: WindowSizeClass,
    toggleRotate: () -> Unit,
    isScrolling: MutableState<Boolean>
) {
    val useNavRail = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
    val bottomNavEntries = rememberNavigationItems()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    navBackStackEntry?.destination?.route?.let {
        val shouldDisplayBottomBar = bottomNavEntries.find { item -> item.route == it } != null
        bottomBarState.value = shouldDisplayBottomBar
        systemBarFollowThemeState.value = !it.contains(Screen.MediaViewScreen.route)
    }
    val navPipe = hiltViewModel<ChanneledViewModel>()
    navPipe
        .initWithNav(navController, bottomBarState)
        .collectAsStateWithLifecycle(LocalLifecycleOwner.current)

    val groupTimelineByMonth by rememberTimelineGroupByMonth()

    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.TimelinePhotoScreen.route
    ) {
        composable(
            route = Screen.TimelinePhotoScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            val viewModel =
                hiltViewModel<MediaViewModel>()
                    .apply(MediaViewModel::launchInPhotosScreen)
                    .apply { groupByMonth = groupTimelineByMonth }

            TimelineScreen(
                isPhoto = true,
                paddingValues = paddingValues,
                retrieveMedia = viewModel::launchInPhotosScreen,
                handler = viewModel.handler,
                mediaState = viewModel.mediaState,
                selectionState = viewModel.multiSelectState,
                selectedMedia = viewModel.selectedPhotoState,
                toggleSelection = viewModel::toggleSelection,
                allowNavBar = !useNavRail,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar,
                isScrolling = isScrolling,
            )
        }
        composable(
            route = Screen.TimelineVideoScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            val viewModel =
                hiltViewModel<MediaViewModel>()
                    .apply(MediaViewModel::launchInVideoScreen)
                    .apply { groupByMonth = groupTimelineByMonth }

            TimelineScreen(
                isPhoto = false,
                paddingValues = paddingValues,
                retrieveMedia = viewModel::launchInVideoScreen,
                handler = viewModel.handler,
                mediaState = viewModel.mediaState,
                selectionState = viewModel.multiSelectState,
                selectedMedia = viewModel.selectedPhotoState,
                toggleSelection = viewModel::toggleSelection,
                allowNavBar = !useNavRail,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar,
                isScrolling = isScrolling,
            )
        }


        composable(
            route = Screen.TrashedScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            val viewModel = hiltViewModel<MediaViewModel>()
                .apply { target = TARGET_TRASH }
                .apply { groupByMonth = groupTimelineByMonth }
            TrashedGridScreen(
                paddingValues = paddingValues,
                mediaState = viewModel.mediaState,
                selectionState = viewModel.multiSelectState,
                selectedMedia = viewModel.selectedPhotoState,
                handler = viewModel.handler,
                toggleSelection = viewModel::toggleSelection,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar
            )
        }
        composable(
            route = Screen.FavoriteScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            val viewModel = hiltViewModel<MediaViewModel>()
                .apply { target = TARGET_FAVORITES }
                .apply { groupByMonth = groupTimelineByMonth }
            FavoriteScreen(
                paddingValues = paddingValues,
                mediaState = viewModel.mediaState,
                handler = viewModel.handler,
                selectionState = viewModel.multiSelectState,
                selectedMedia = viewModel.selectedPhotoState,
                toggleFavorite = viewModel::toggleFavorite,
                toggleSelection = viewModel::toggleSelection,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar
            )
        }
        composable(
            route = Screen.AlbumsScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            val viewModel = hiltViewModel<AlbumsViewModel>()
            AlbumsScreen(
                navigate = navPipe::navigate,
                toggleNavbar = navPipe::toggleNavbar,
                paddingValues = paddingValues,
                viewModel = viewModel,
                isScrolling = isScrolling,
            )
        }
        composable(
            route = Screen.AlbumViewScreen.route +
                    "?albumId={albumId}&albumName={albumName}",
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation },
            arguments = listOf(
                navArgument(name = "albumId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "albumName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val argumentAlbumName = backStackEntry.arguments?.getString("albumName")
                ?: stringResource(id = R.string.app_name)
            val argumentAlbumId = backStackEntry.arguments?.getLong("albumId") ?: -1
            val viewModel: MediaViewModel = hiltViewModel<MediaViewModel>()
                .apply { albumId = argumentAlbumId }
                .apply { groupByMonth = groupTimelineByMonth }
            TimelineScreen(
                paddingValues = paddingValues,
                albumId = argumentAlbumId,
                albumName = argumentAlbumName,
                handler = viewModel.handler,
                mediaState = viewModel.mediaState,
                selectionState = viewModel.multiSelectState,
                selectedMedia = viewModel.selectedPhotoState,
                allowNavBar = false,
                toggleSelection = viewModel::toggleSelection,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar,
                isScrolling = isScrolling
            )
        }
        composable(
            route = Screen.MediaViewScreen.route +
                    "?isPhoto={isPhoto}&mediaId={mediaId}&albumId={albumId}",
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation },
            arguments = listOf(
                navArgument(name = "mediaId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "albumId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "isPhoto") {
                    type = NavType.BoolType
                    defaultValue = true
                }
            )
        ) { backStackEntry ->
            val mediaId: Long = backStackEntry.arguments?.getLong("mediaId") ?: -1
            val albumId: Long = backStackEntry.arguments?.getLong("albumId") ?: -1
            val isPhoto: Boolean = backStackEntry.arguments?.getBoolean("isPhoto") ?: true
            val route = if (isPhoto) Screen.TimelinePhotoScreen.route else Screen.TimelineVideoScreen.route
            val entryName =
                if (albumId == -1L) route else Screen.AlbumViewScreen.route
            val parentEntry = remember(backStackEntry) {
                try {
                    navController.getBackStackEntry(entryName)
                } catch (e: Exception){
                    navController.getBackStackEntry(Screen.TimelinePhotoScreen.route)
                }

            }
            val viewModel = hiltViewModel<MediaViewModel>(parentEntry)

            /*if (route == Screen.TimelineVideoScreen.route){
                viewModel.onServer()
            }
            else{
                viewModel.offServer()
                viewModel.cleaner()
            }*/


            MediaViewScreen(
                paddingValues = paddingValues,
                mediaId = mediaId,
                mediaState = viewModel.mediaState,
                handler = viewModel.handler,
                navigateUp = navPipe::navigateUp,
                toggleRotate = toggleRotate
            )
        }
        composable(
            route = Screen.MediaViewScreen.route +
                    "?isPhoto={isPhoto}&mediaId={mediaId}&target={target}",
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation },
            arguments = listOf(
                navArgument(name = "mediaId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "target") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(name = "isPhoto") {
                    type = NavType.BoolType
                    defaultValue = true
                }
            )
        ) { backStackEntry ->
            val mediaId: Long = backStackEntry.arguments?.getLong("mediaId") ?: -1
            val target: String? = backStackEntry.arguments?.getString("target")
            val isPhoto: Boolean = backStackEntry.arguments?.getBoolean("isPhoto") ?: true
            val route = if (isPhoto) Screen.TimelinePhotoScreen.route else Screen.TimelineVideoScreen.route
            val entryName = when (target) {
                TARGET_FAVORITES -> Screen.FavoriteScreen.route
                TARGET_TRASH -> Screen.TrashedScreen.route
                else -> route//Screen.TimelinePhotoScreen.route
            }
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(entryName)
            }
            val viewModel = hiltViewModel<MediaViewModel>(parentEntry)
            MediaViewScreen(
                paddingValues = paddingValues,
                mediaId = mediaId,
                target = target,
                mediaState = viewModel.mediaState,
                handler = viewModel.handler,
                navigateUp = navPipe::navigateUp,
                toggleRotate = toggleRotate
            )
        }
        composable(
            route = Screen.SettingsScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation },
        ) {
            val viewModel = hiltViewModel<SettingsViewModel>()
            SettingsScreen(
                navigateUp = navPipe::navigateUp,
                navigate = navPipe::navigate,
                viewModel = viewModel
            )
        }
        composable(
            route = Screen.AlbumSizeScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation },
        ) {
            AlbumSizeScreen(
                navigateUp = navPipe::navigateUp
            )
        }
    }
}