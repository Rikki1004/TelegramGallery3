//
// NOTE: THIS FILE IS AUTO-GENERATED by the "TdApiKtxGenerator".kt
// See: https://github.com/tdlibx/td-ktx-generator/
//
package com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines

import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramFlow
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.AutoDownloadSettings
import org.drinkless.td.libcore.telegram.TdApi.AutoDownloadSettingsPresets
import org.drinkless.td.libcore.telegram.TdApi.NetworkType

/**
 * Suspend function, which returns auto-download settings presets for the currently logged in user.
 *
 * @return [AutoDownloadSettingsPresets] Contains auto-download settings presets for the user.
 */
suspend fun TelegramFlow.getAutoDownloadSettingsPresets(): AutoDownloadSettingsPresets =
    this.sendFunctionAsync(TdApi.GetAutoDownloadSettingsPresets())

/**
 * Suspend function, which sets auto-download settings.
 *
 * @param settings New user auto-download settings.  
 * @param type Type of the network for which the new settings are applied.
 */
suspend fun TelegramFlow.setAutoDownloadSettings(settings: AutoDownloadSettings?,
    type: NetworkType?) = this.sendFunctionLaunch(TdApi.SetAutoDownloadSettings(settings, type))
