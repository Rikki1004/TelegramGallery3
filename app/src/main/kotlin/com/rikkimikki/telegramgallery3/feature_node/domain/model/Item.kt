package com.rikkimikki.telegramgallery3.feature_node.domain.model


data class Item (
    val msgId: Long,
    val chatId: Long,
    @Transient val mediaId: Long? = null,
    val label: String,
    val date: Long,
    val size: Long,
    val tags: MutableList<String>,
    val mimeType: String,
    val orientation: Int,
    var favorite: Boolean,
    var trashed: Boolean,
    val duration: String? = null,
    val thumbnailMsgId: Long? = null,
)