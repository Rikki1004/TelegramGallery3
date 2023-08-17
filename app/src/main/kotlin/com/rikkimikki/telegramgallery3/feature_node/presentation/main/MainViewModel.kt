package com.rikkimikki.telegramgallery3.feature_node.presentation.main
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rikkimikki.telegramgallery3.feature_node.domain.use_case.MediaUseCases
import com.rikkimikki.telegramgallery3.feature_node.domain.util.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mediaUseCases: MediaUseCases
) : ViewModel() {

    val authState = mediaUseCases.authPerformResultUseCase()

    var initState: AuthState = AuthState.Initial

    fun performAuthResult() {
        mediaUseCases.authStartTelegramUseCase()
    }

    fun sendPhone(phone: String) {
        viewModelScope.launch {
            mediaUseCases.authSendPhoneUseCase(phone)
        }
    }

    fun sendCode(code: String) {
        viewModelScope.launch {
            mediaUseCases.authSendCodeUseCase(code)
        }
    }

    fun sendPassword(password: String) {
        viewModelScope.launch {
            mediaUseCases.authSendPasswordUseCase(password)
        }
    }

}