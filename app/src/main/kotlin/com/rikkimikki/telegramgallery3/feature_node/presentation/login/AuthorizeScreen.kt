package com.rikkimikki.telegramgallery3.feature_node.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rikkimikki.telegramgallery3.R
import com.rikkimikki.telegramgallery3.feature_node.domain.util.AuthState

private val defaultPadding: Dp = 16.dp
@OptIn(ExperimentalMaterial3Api::class)
//@Preview
@Composable
fun AuthorizeScreen(
    state: AuthState,
    clickListener: (inputString:String)->Unit
) {

    var inputValue by rememberSaveable { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(defaultPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_blue),
                contentDescription = "Application Logo",
                modifier = Modifier.size(defaultPadding * 8)
            )
            Spacer(modifier = Modifier.height(defaultPadding))
            Text(
                text = when(state){
                    AuthState.EnterPhone -> stringResource(R.string.login_screen_input_number_help_text)
                    AuthState.EnterPassword -> stringResource(R.string.login_screen_input_password_help_text)
                    AuthState.EnterCode -> stringResource(R.string.login_screen_input_code_help_text)
                    else -> throw RuntimeException("Invalid state")
                },
                textAlign = TextAlign.Justify
            )
            Spacer(modifier = Modifier.height(defaultPadding))
            TextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = {
                    Text(
                        text = when(state){
                            AuthState.EnterPhone -> stringResource(R.string.phone_number_label)
                            AuthState.EnterPassword -> stringResource(R.string.password_label)
                            AuthState.EnterCode -> stringResource(R.string.sms_code_label)
                            else -> throw RuntimeException("Invalid state")
                        }
                    )
                },
                placeholder = {
                    if (state == AuthState.EnterPhone)
                        Text(text = stringResource(R.string.login_screen_phone_placeholder))
                }
            )
            Spacer(modifier = Modifier.height(defaultPadding))
            Button(
                onClick = {
                    clickListener(inputValue)
                }
            ) {
                Text(text = stringResource(R.string.submit_button))
            }
        }
    }
}


/*
@Preview
@Composable
fun AuthorizeScreenPreview(){
    AuthorizeScreen(AuthState.EnterPhone)
}*/
