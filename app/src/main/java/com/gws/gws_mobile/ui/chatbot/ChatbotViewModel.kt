package com.gws.gws_mobile.ui.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatbotViewModel : ViewModel() {
    private val _messageList = MutableLiveData<MutableList<Message>>().apply { value = mutableListOf() }
    val messageList: LiveData<MutableList<Message>> = _messageList

    fun addMessage(message: Message) {
        _messageList.value?.apply {
            add(message)
            _messageList.postValue(this)
        }
    }

    fun restoreMessages(messages: MutableList<Message>) {
        _messageList.value = messages
    }

    fun clearMessages() {
        _messageList.value = mutableListOf()
    }

}
