//
// NOTE: THIS FILE IS AUTO-GENERATED by the "TdApiKtxGenerator".kt
// See: https://github.com/tdlibx/td-ktx-generator/
//
package com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines

import kotlin.Long
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramFlow
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.Sessions

/**
 * Suspend function, which returns all active sessions of the current user.
 *
 * @return [Sessions] Contains a list of sessions.
 */
suspend fun TelegramFlow.getActiveSessions(): Sessions =
    this.sendFunctionAsync(TdApi.GetActiveSessions())

/**
 * Suspend function, which terminates all other sessions of the current user.
 */
suspend fun TelegramFlow.terminateAllOtherSessions() =
    this.sendFunctionLaunch(TdApi.TerminateAllOtherSessions())

/**
 * Suspend function, which terminates a session of the current user.
 *
 * @param sessionId Session identifier.
 */
suspend fun TelegramFlow.terminateSession(sessionId: Long) =
    this.sendFunctionLaunch(TdApi.TerminateSession(sessionId))
