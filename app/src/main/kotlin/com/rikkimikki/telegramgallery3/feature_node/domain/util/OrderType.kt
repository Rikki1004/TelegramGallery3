package com.rikkimikki.telegramgallery3.feature_node.domain.util

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
