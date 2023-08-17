//
// NOTE: THIS FILE IS AUTO-GENERATED by the "TdApiKtxGenerator".kt
// See: https://github.com/tdlibx/td-ktx-generator/
//
package com.rikkimikki.telegramgallery3.feature_node.data.telegram.coroutines

import kotlin.String
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.core.TelegramFlow
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.FormattedText
import org.drinkless.td.libcore.telegram.TdApi.TextEntities
import org.drinkless.td.libcore.telegram.TdApi.TextParseMode

/**
 * Suspend function, which returns all entities (mentions, hashtags, cashtags, bot commands, URLs,
 * and email addresses) contained in the text. This is an offline method. Can be called before
 * authorization. Can be called synchronously.
 *
 * @param text The text in which to look for entites.
 *
 * @return [TextEntities] Contains a list of text entities.
 */
suspend fun TelegramFlow.getTextEntities(text: String?): TextEntities =
    this.sendFunctionAsync(TdApi.GetTextEntities(text))

/**
 * Suspend function, which parses Bold, Italic, Underline, Strikethrough, Code, Pre, PreCode,
 * TextUrl and MentionName entities contained in the text. This is an offline method. Can be called
 * before authorization. Can be called synchronously.
 *
 * @param text The text which should be parsed.  
 * @param parseMode Text parse mode.
 *
 * @return [FormattedText] A text with some entities.
 */
suspend fun TelegramFlow.parseTextEntities(text: String?, parseMode: TextParseMode?): FormattedText
    = this.sendFunctionAsync(TdApi.ParseTextEntities(text, parseMode))
