package com.gws.gws_mobile.ui.insight

sealed class ItemData {
    data class Recommendation(val title: String, val description: String) : ItemData()
    data class News(val title: String, val description: String) : ItemData()
}
