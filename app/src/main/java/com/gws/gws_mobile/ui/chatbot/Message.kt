package com.gws.gws_mobile.ui.chatbot

data class Message(val text: String, val sender: Int) {
    companion object {
        const val SENDER_USER = 0
        const val SENDER_BOT = 1
    }
}
