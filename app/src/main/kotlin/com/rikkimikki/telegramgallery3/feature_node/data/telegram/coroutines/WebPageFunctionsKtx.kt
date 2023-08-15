//
// NOTE: THIS FILE IS AUTO-GENERATED by the "TdApiKtxGenerator".kt
// See: https://github.com/tdlibx/td-ktx-generator/
//
package com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines

import kotlin.Boolean
import kotlin.String
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramFlow
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.FormattedText
import org.drinkless.td.libcore.telegram.TdApi.WebPage
import org.drinkless.td.libcore.telegram.TdApi.WebPageInstantView

/**
 * Suspend function, which returns an instant view version of a web page if available. Returns a 404
 * error if the web page has no instant view page.
 *
 * @param url The web page URL.  
 * @param forceFull If true, the full instant view for the web page will be returned.
 *
 * @return [WebPageInstantView] Describes an instant view page for a web page.
 */
suspend fun TelegramFlow.getWebPageInstantView(url: String?, forceFull: Boolean): WebPageInstantView
    = this.sendFunctionAsync(TdApi.GetWebPageInstantView(url, forceFull))

/**
 * Suspend function, which returns a web page preview by the text of the message. Do not call this
 * function too often. Returns a 404 error if the web page has no preview.
 *
 * @param text Message text with formatting.
 *
 * @return [WebPage] Describes a web page preview.
 */
suspend fun TelegramFlow.getWebPagePreview(text: FormattedText?): WebPage =
    this.sendFunctionAsync(TdApi.GetWebPagePreview(text))