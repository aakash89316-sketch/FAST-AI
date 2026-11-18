package com.example.chat

import android.graphics.Bitmap
import java.util.UUID

enum class MessageSender {
    USER,
    AI
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestampLabel: String,
    val imageBitmap: Bitmap? = null,
    val isStreaming: Boolean = false
)

sealed interface AiRequestState {
    data object Idle : AiRequestState
    data object Streaming : AiRequestState
    data class Error(
        val message: String,
        val retryPrompt: String? = null,
        val retryImage: Bitmap? = null
    ) : AiRequestState
}

data class ChatScreenUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val pendingImage: Bitmap? = null,
    val aiState: AiRequestState = AiRequestState.Idle,
    val isListening: Boolean = false,
    val partialVoiceText: String = "",
    val speakingMessageId: String? = null,
    val autoSpeakVoiceReplies: Boolean = true,
    val voiceNotice: String? = null
) {
    val isBusy: Boolean
        get() = aiState is AiRequestState.Streaming
}
