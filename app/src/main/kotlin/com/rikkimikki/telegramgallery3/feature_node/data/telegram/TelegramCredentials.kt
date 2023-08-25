package com.rikkimikki.telegramgallery3.feature_node.data.telegram

import android.content.Context
import org.drinkless.td.libcore.telegram.TdApi


class TelegramCredentials(context: Context) {

    val parameters = TdApi.TdlibParameters().apply {
        databaseDirectory = context.filesDir.absolutePath + "/td"
        useMessageDatabase = false
        useSecretChats = false
        apiId = API_ID
        apiHash = API_HASH
        useFileDatabase = false
        systemLanguageCode = "en"
        deviceModel = "Android"
        systemVersion = "12"
        applicationVersion = "1.0"
        enableStorageOptimizer = true
        useChatInfoDatabase = false

    }
}