package com.rikkimikki.telegramgallery3.feature_node.domain.model


data class Index(
    var supportedTags : List<String>,
    var photo : MutableList<Item>,
    var video : MutableList<Item>,
) {
    val all = video+photo
}
