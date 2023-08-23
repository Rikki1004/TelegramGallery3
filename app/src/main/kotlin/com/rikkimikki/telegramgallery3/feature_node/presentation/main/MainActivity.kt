package com.rikkimikki.telegramgallery3.feature_node.presentation.main

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rikkimikki.telegramgallery3.core.Settings.Misc.getSecureMode
import com.rikkimikki.telegramgallery3.core.presentation.components.AppBarContainer
import com.rikkimikki.telegramgallery3.core.presentation.components.NavigationComp
import com.rikkimikki.telegramgallery3.feature_node.presentation.util.toggleOrientation
import com.rikkimikki.telegramgallery3.ui.theme.GalleryTheme
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.feature_node.domain.util.AuthState
import com.rikkimikki.telegramgallery3.feature_node.presentation.login.AuthorizeScreen
import com.rikkimikki.telegramgallery3.feature_node.presentation.login.LoginScreen
import com.rikkimikki.telegramgallery3.feature_node.presentation.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        println(filesDir.absolutePath)
        //enforceSecureFlag()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            GalleryTheme {

                viewModel = hiltViewModel<MainViewModel>()

                val authState = viewModel.authState.collectAsState(viewModel.initState)

                var isOvercome by rememberSaveable { mutableStateOf(false) }

                val a = authState.value
                viewModel.initState = a
                when (a) {
                    AuthState.LoggedIn -> {
                        val navController = rememberAnimatedNavController()
                        val isScrolling = remember { mutableStateOf(false) }
                        val bottomBarState = rememberSaveable { (mutableStateOf(true)) }
                        val systemBarFollowThemeState = rememberSaveable { (mutableStateOf(true)) }
                        val systemUiController = rememberSystemUiController()
                        systemUiController.systemBarsDarkContentEnabled =
                            systemBarFollowThemeState.value && !isSystemInDarkTheme()
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            content = { paddingValues ->
                                AppBarContainer(
                                    navController = navController,
                                    bottomBarState = bottomBarState,
                                    windowSizeClass = windowSizeClass,
                                    isScrolling = isScrolling
                                ) {
                                    NavigationComp(
                                        navController = navController,
                                        paddingValues = paddingValues,
                                        bottomBarState = bottomBarState,
                                        systemBarFollowThemeState = systemBarFollowThemeState,
                                        windowSizeClass = windowSizeClass,
                                        toggleRotate = ::toggleOrientation,
                                        isScrolling = isScrolling
                                    )
                                }
                            }
                        )
                    }
                    AuthState.EnterCode -> {
                        AuthorizeScreen(AuthState.EnterCode){
                            viewModel.sendCode(it)
                        }
                    }
                    AuthState.EnterPhone -> {
                        if (isOvercome){
                            AuthorizeScreen(AuthState.EnterPhone){
                                viewModel.sendPhone(it)
                            }
                        } else {
                            LoginScreen {
                                isOvercome = true
                            }
                        }
                    }
                    AuthState.EnterPassword -> {
                        AuthorizeScreen(AuthState.EnterPassword){
                            viewModel.sendPassword(it)
                        }
                    }
                    AuthState.Waiting -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center,
                            //color = MaterialTheme.colorScheme.background
                        ) {
                            Column (
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(15.dp))
                                Text(text = getString(R.string.loading_text))
                            }
                        }

                    }
                    AuthState.Initial -> {
                        Text(text = "Init")
                        viewModel.performAuthResult()
                    }
                }
            }
        }
    }

    private fun enforceSecureFlag() {
        lifecycleScope.launch {
            getSecureMode(this@MainActivity).collectLatest { enabled ->
                if (enabled) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }
    }

}