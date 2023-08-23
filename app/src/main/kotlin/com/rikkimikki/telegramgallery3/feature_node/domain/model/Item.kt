package com.rikkimikki.telegramgallery3.feature_node.domain.model


data class Item (
    val msgId: Long,
    val chatId: Long,
    @Transient val mediaId: Long? = null,
    val label: String,
    val timestamp: Long,
    val size: Long,
    val tags: MutableList<String>,
    val mimeType: String,
    val orientation: Int,
    val favorite: Boolean,
    val trashed: Boolean,
    val duration: String? = null,
    val thumbnailMsgId: Long? = null,
)