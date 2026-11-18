package com.example.chat

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "FastAiChatViewModel"
private const val MODEL_NAME = "gemini-3.8-flash"
private const val RESPONSE_TIMEOUT_MS = 60_000L
private const val DEFAULT_IMAGE_QUESTION = "Is photo me kya hai?"
private const val HINDI_LOCALE_TAG = "hi-IN"

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.9f
                topP = 1.0f
                maxOutputTokens = 8192
            },
            systemInstruction = content {
                text(
                    """
                    You are FAST AI. Your job is to answer every safe, lawful question clearly and helpfully in Hindi.
                    - Avoid robotic phrases like "I don't know", "Mujhe nahi pata", "Mai ye nahi bata sakta", "Quota exceeded", "Model busy", or "As an AI".
                    - Cover broad topics including Maths, Science, Health & Sex Education, Medical concepts, Love & Relationships, Coding, Cybersecurity concepts (strictly educational and defensive), History, and General Knowledge.
                    - When exact facts are unavailable or uncertain, share a well-reasoned best estimate clearly prefaced with "Mere anusar...".
                    - When the user sends a photo, describe what you see in simple Hindi with bullet points and emoji, and solve any maths or homework questions step-by-step.
                    - Respond quickly, concisely, and always in Hindi (unless the user explicitly asks for another language).
                    """.trimIndent()
                )
            }
        )
    }

    private var activeJob: Job? = null
    private var activeStreamingMessageId: String? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var pendingAutoSpeakText: Pair<String, String>? = null

    private val hindiLocale: Locale = Locale.Builder().setLanguage("hi").setRegion("IN").build()
    private val timeFormatter = SimpleDateFormat("hh:mm a", hindiLocale)

    private val responseCache = ConcurrentHashMap<String, String>().apply {
        val intro = "Namaste! Mai FAST AI hu, duniya ka sabse tez aur sab kuch janne wala AI. Pucho jo puchna hai! ⚡"
        put("hi", intro)
        put("hello", intro)
        put("namaste", intro)
        put("नमस्ते", intro)
        put("who are you", intro)
        put("tum kaun ho", intro)
        put("आप कौन हैं", intro)
    }

    private val _uiState = MutableStateFlow(ChatScreenUiState())
    val uiState: StateFlow<ChatScreenUiState> = _uiState.asStateFlow()

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        runCatching {
            val context = getApplication<Application>()
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val langResult = textToSpeech?.setLanguage(hindiLocale)
                    isTtsReady = langResult != TextToSpeech.LANG_MISSING_DATA &&
                        langResult != TextToSpeech.LANG_NOT_SUPPORTED
                    textToSpeech?.setSpeechRate(1.05f)
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _uiState.update { it.copy(speakingMessageId = utteranceId) }
                        }

                        override fun onDone(utteranceId: String?) {
                            _uiState.update { state ->
                                if (state.speakingMessageId == utteranceId) {
                                    state.copy(speakingMessageId = null)
                                } else {
                                    state
                                }
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _uiState.update { state ->
                                if (state.speakingMessageId == utteranceId) {
                                    state.copy(speakingMessageId = null)
                                } else {
                                    state
                                }
                            }
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _uiState.update { state ->
                                if (state.speakingMessageId == utteranceId) {
                                    state.copy(speakingMessageId = null)
                                } else {
                                    state
                                }
                            }
                        }
                    })

                    pendingAutoSpeakText?.let { (text, messageId) ->
                        pendingAutoSpeakText = null
                        speakText(text, messageId)
                    }
                } else {
                    Log.w(TAG, "TextToSpeech initialization failed with status: $status")
                }
            }
        }.onFailure { error ->
            Log.w(TAG, "TextToSpeech unavailable in this environment", error)
        }
    }

    fun startVoiceListening() {
        if (_uiState.value.isBusy) return
        stopSpeaking()

        val context = getApplication<Application>()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _uiState.update {
                it.copy(voiceNotice = "इस डिवाइस पर वॉइस पहचान (Speech Recognition) उपलब्ध नहीं है।")
            }
            return
        }

        runCatching {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _uiState.update { it.copy(isListening = true, partialVoiceText = "") }
                }

                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit

                override fun onError(error: Int) {
                    val notice = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                            "आवाज़ साफ़ नहीं सुनाई दी, कृपया फिर से बोलें।"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                            "आवाज़ से सवाल पूछने के लिए माइक अनुमति आवश्यक है।"
                        SpeechRecognizer.ERROR_AUDIO ->
                            "माइक से ऑडियो रिकॉर्ड करने में समस्या आई।"
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                            "वॉइस पहचान के लिए इंटरनेट कनेक्शन की जाँच करें।"
                        else -> null
                    }
                    _uiState.update {
                        it.copy(
                            isListening = false,
                            partialVoiceText = "",
                            voiceNotice = notice
                        )
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val recognizedText = matches?.firstOrNull()?.trim().orEmpty()
                    _uiState.update { it.copy(isListening = false, partialVoiceText = "") }

                    if (recognizedText.isNotEmpty()) {
                        val pendingImage = _uiState.value.pendingImage
                        val shouldSpeakReply = _uiState.value.autoSpeakVoiceReplies
                        if (pendingImage != null) {
                            sendImageWithText(
                                bitmap = pendingImage,
                                question = recognizedText,
                                speakReplyWhenDone = shouldSpeakReply
                            )
                        } else {
                            sendMessage(
                                overridePrompt = recognizedText,
                                speakReplyWhenDone = shouldSpeakReply
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(voiceNotice = "आवाज़ समझ नहीं आई, कृपया फिर से बोलें।")
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()
                    if (partial.isNotEmpty()) {
                        _uiState.update { it.copy(partialVoiceText = partial) }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, HINDI_LOCALE_TAG)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, HINDI_LOCALE_TAG)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer = recognizer
            _uiState.update { it.copy(isListening = true, partialVoiceText = "") }
            recognizer.startListening(intent)
        }.onFailure { error ->
            Log.e(TAG, "Failed to start SpeechRecognizer", error)
            _uiState.update {
                it.copy(
                    isListening = false,
                    partialVoiceText = "",
                    voiceNotice = "वॉइस पहचान शुरू नहीं हो सकी।"
                )
            }
        }
    }

    fun stopVoiceListening() {
        runCatching {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        }
        _uiState.update { it.copy(isListening = false, partialVoiceText = "") }
    }

    fun toggleSpeakMessage(message: ChatMessage) {
        if (_uiState.value.speakingMessageId == message.id) {
            stopSpeaking()
        } else {
            speakText(message.text, message.id)
        }
    }

    fun speakText(text: String, messageId: String? = null) {
        val cleanText = cleanForSpeech(text)
        if (cleanText.isBlank()) return

        val utteranceId = messageId ?: UUID.randomUUID().toString()
        val tts = textToSpeech
        if (tts == null || !isTtsReady) {
            pendingAutoSpeakText = cleanText to utteranceId
            return
        }

        _uiState.update { it.copy(speakingMessageId = utteranceId) }
        tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        pendingAutoSpeakText = null
        runCatching { textToSpeech?.stop() }
        _uiState.update { it.copy(speakingMessageId = null) }
    }

    fun toggleAutoSpeakVoiceReplies() {
        _uiState.update { state ->
            val next = !state.autoSpeakVoiceReplies
            if (!next) {
                runCatching { textToSpeech?.stop() }
            }
            state.copy(
                autoSpeakVoiceReplies = next,
                speakingMessageId = if (next) state.speakingMessageId else null
            )
        }
    }

    fun clearVoiceNotice() {
        _uiState.update { it.copy(voiceNotice = null) }
    }

    fun onInputChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun onImageSelected(bitmap: Bitmap) {
        _uiState.update { it.copy(pendingImage = bitmap) }
    }

    fun clearPendingImage() {
        _uiState.update { it.copy(pendingImage = null) }
    }

    fun sendCurrentInputOrImage() {
        val currentState = _uiState.value
        if (currentState.isBusy) return

        val pendingImage = currentState.pendingImage
        if (pendingImage != null) {
            val question = currentState.inputText.trim().ifEmpty { DEFAULT_IMAGE_QUESTION }
            sendImageWithText(bitmap = pendingImage, question = question)
        } else {
            sendMessage()
        }
    }

    fun sendMessage(
        overridePrompt: String? = null,
        speakReplyWhenDone: Boolean = false
    ) {
        val currentState = _uiState.value
        if (currentState.isBusy) return

        val prompt = (overridePrompt ?: currentState.inputText).trim()
        if (prompt.isEmpty()) return

        val now = currentTimestamp()
        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = prompt,
            timestampLabel = now
        )

        val cacheKey = normalizePrompt(prompt)
        val cachedAnswer = responseCache[cacheKey]
        if (cachedAnswer != null) {
            val cachedAiMessage = ChatMessage(
                sender = MessageSender.AI,
                text = cachedAnswer,
                timestampLabel = now,
                isStreaming = false
            )
            _uiState.update { state ->
                state.copy(
                    messages = state.messages + userMessage + cachedAiMessage,
                    inputText = if (overridePrompt == null) "" else state.inputText,
                    aiState = AiRequestState.Idle
                )
            }
            if (speakReplyWhenDone) {
                speakText(cachedAnswer, cachedAiMessage.id)
            }
            return
        }

        val conversationContents = buildConversationContents(
            existingMessages = currentState.messages,
            latestUserPrompt = prompt,
            latestImageBytes = null
        )

        val aiMessageId = UUID.randomUUID().toString()
        val streamingPlaceholder = ChatMessage(
            id = aiMessageId,
            sender = MessageSender.AI,
            text = "",
            timestampLabel = now,
            isStreaming = true
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage + streamingPlaceholder,
                inputText = if (overridePrompt == null) "" else state.inputText,
                aiState = AiRequestState.Streaming
            )
        }

        streamGeminiResponse(
            prompt = prompt,
            cacheKey = cacheKey,
            conversationContents = conversationContents,
            aiMessageId = aiMessageId,
            retryImage = null,
            speakReplyWhenDone = speakReplyWhenDone
        )
    }

    fun sendImageWithText(
        bitmap: Bitmap,
        question: String,
        speakReplyWhenDone: Boolean = false
    ) {
        val currentState = _uiState.value
        if (currentState.isBusy) return

        val cleanQuestion = question.trim().ifEmpty { DEFAULT_IMAGE_QUESTION }
        val visionPrompt =
            "Is photo ko dekho aur iske bare me Hindi me detail me batao: $cleanQuestion"
        val now = currentTimestamp()

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = cleanQuestion,
            imageBitmap = bitmap,
            timestampLabel = now
        )

        val aiMessageId = UUID.randomUUID().toString()
        val streamingPlaceholder = ChatMessage(
            id = aiMessageId,
            sender = MessageSender.AI,
            text = "",
            timestampLabel = now,
            isStreaming = true
        )

        val existingMessagesSnapshot = currentState.messages

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage + streamingPlaceholder,
                inputText = "",
                pendingImage = null,
                aiState = AiRequestState.Streaming
            )
        }

        activeJob?.cancel()
        activeStreamingMessageId = aiMessageId
        activeJob = viewModelScope.launch {
            val imageBytes = withContext(Dispatchers.Default) {
                ImageUtils.bitmapToJpegByteArray(bitmap)
            }
            val conversationContents = buildConversationContents(
                existingMessages = existingMessagesSnapshot,
                latestUserPrompt = visionPrompt,
                latestImageBytes = imageBytes
            )
            executeStreamingCall(
                promptForRetry = cleanQuestion,
                cacheKey = null,
                conversationContents = conversationContents,
                aiMessageId = aiMessageId,
                retryImage = bitmap,
                speakReplyWhenDone = speakReplyWhenDone
            )
        }
    }

    fun stopGenerating() {
        val streamingId = activeStreamingMessageId
        activeJob?.cancel()
        activeJob = null
        activeStreamingMessageId = null

        _uiState.update { state ->
            val updatedMessages = state.messages.mapNotNull { msg ->
                if (msg.id == streamingId) {
                    if (msg.text.isBlank()) null else msg.copy(isStreaming = false)
                } else {
                    msg
                }
            }
            state.copy(
                messages = updatedMessages,
                aiState = AiRequestState.Idle
            )
        }
    }

    fun retryLastFailedPrompt() {
        val errorState = _uiState.value.aiState as? AiRequestState.Error ?: return
        val prompt = errorState.retryPrompt ?: return
        if (_uiState.value.isBusy) return

        val retryImage = errorState.retryImage
        val aiMessageId = UUID.randomUUID().toString()
        val streamingPlaceholder = ChatMessage(
            id = aiMessageId,
            sender = MessageSender.AI,
            text = "",
            timestampLabel = currentTimestamp(),
            isStreaming = true
        )

        val existingMessagesSnapshot = _uiState.value.messages

        _uiState.update { state ->
            state.copy(
                messages = state.messages + streamingPlaceholder,
                aiState = AiRequestState.Streaming
            )
        }

        if (retryImage != null) {
            val visionPrompt =
                "Is photo ko dekho aur iske bare me Hindi me detail me batao: $prompt"
            activeJob?.cancel()
            activeStreamingMessageId = aiMessageId
            activeJob = viewModelScope.launch {
                val imageBytes = withContext(Dispatchers.Default) {
                    ImageUtils.bitmapToJpegByteArray(retryImage)
                }
                val conversationContents = buildConversationContents(
                    existingMessages = existingMessagesSnapshot,
                    latestUserPrompt = visionPrompt,
                    latestImageBytes = imageBytes
                )
                executeStreamingCall(
                    promptForRetry = prompt,
                    cacheKey = null,
                    conversationContents = conversationContents,
                    aiMessageId = aiMessageId,
                    retryImage = retryImage,
                    speakReplyWhenDone = false
                )
            }
        } else {
            val conversationContents = buildConversationContents(
                existingMessages = existingMessagesSnapshot,
                latestUserPrompt = null,
                latestImageBytes = null
            )
            streamGeminiResponse(
                prompt = prompt,
                cacheKey = normalizePrompt(prompt),
                conversationContents = conversationContents,
                aiMessageId = aiMessageId,
                retryImage = null,
                speakReplyWhenDone = false
            )
        }
    }

    fun dismissError() {
        _uiState.update { state ->
            if (state.aiState is AiRequestState.Error) {
                state.copy(aiState = AiRequestState.Idle)
            } else {
                state
            }
        }
    }

    fun startNewChat() {
        activeJob?.cancel()
        activeJob = null
        activeStreamingMessageId = null
        stopVoiceListening()
        stopSpeaking()
        _uiState.value = ChatScreenUiState(
            autoSpeakVoiceReplies = _uiState.value.autoSpeakVoiceReplies
        )
    }

    internal fun getCachedResponseForTest(prompt: String): String? =
        responseCache[normalizePrompt(prompt)]

    override fun onCleared() {
        super.onCleared()
        runCatching { speechRecognizer?.destroy() }
        speechRecognizer = null
        runCatching {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
        textToSpeech = null
    }

    private fun buildConversationContents(
        existingMessages: List<ChatMessage>,
        latestUserPrompt: String?,
        latestImageBytes: ByteArray?
    ): List<Content> {
        val history = existingMessages
            .filter { (it.text.isNotBlank() || it.imageBitmap != null) && !it.isStreaming }
            .map { msg ->
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val msgBitmap = msg.imageBitmap
                if (msgBitmap != null) {
                    content(role = role) {
                        image(msgBitmap)
                        if (msg.text.isNotBlank()) {
                            text(msg.text)
                        }
                    }
                } else {
                    content(role = role) { text(msg.text) }
                }
            }
            .toMutableList()

        if (!latestUserPrompt.isNullOrBlank() || latestImageBytes != null) {
            history.add(
                content(role = "user") {
                    if (latestImageBytes != null) {
                        inlineData(latestImageBytes, "image/jpeg")
                    }
                    if (!latestUserPrompt.isNullOrBlank()) {
                        text(latestUserPrompt)
                    }
                }
            )
        }
        return history
    }

    private fun streamGeminiResponse(
        prompt: String,
        cacheKey: String?,
        conversationContents: List<Content>,
        aiMessageId: String,
        retryImage: Bitmap?,
        speakReplyWhenDone: Boolean
    ) {
        activeJob?.cancel()
        activeStreamingMessageId = aiMessageId
        activeJob = viewModelScope.launch {
            executeStreamingCall(
                promptForRetry = prompt,
                cacheKey = cacheKey,
                conversationContents = conversationContents,
                aiMessageId = aiMessageId,
                retryImage = retryImage,
                speakReplyWhenDone = speakReplyWhenDone
            )
        }
    }

    private suspend fun executeStreamingCall(
        promptForRetry: String,
        cacheKey: String?,
        conversationContents: List<Content>,
        aiMessageId: String,
        retryImage: Bitmap?,
        speakReplyWhenDone: Boolean
    ) {
        val accumulatedText = StringBuilder()

        try {
            withTimeout(RESPONSE_TIMEOUT_MS) {
                model.generateContentStream(conversationContents).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        accumulatedText.append(chunkText)
                        val updatedText = accumulatedText.toString()

                        _uiState.update { state ->
                            state.copy(
                                messages = state.messages.map { msg ->
                                    if (msg.id == aiMessageId) {
                                        msg.copy(text = updatedText)
                                    } else {
                                        msg
                                    }
                                }
                            )
                        }
                    }
                }
            }

            val finalText = accumulatedText.toString().trim()
            if (finalText.isNotEmpty()) {
                if (cacheKey != null) {
                    responseCache[cacheKey] = finalText
                }
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(text = finalText, isStreaming = false)
                            } else {
                                msg
                            }
                        },
                        aiState = AiRequestState.Idle
                    )
                }
                if (speakReplyWhenDone) {
                    speakText(finalText, aiMessageId)
                }
            } else {
                removeStreamingPlaceholder(aiMessageId)
                _uiState.update { state ->
                    state.copy(
                        aiState = AiRequestState.Error(
                            message = "Bhai server thoda busy hai, sawal fir se pucho mai turant bata raha hu...",
                            retryPrompt = promptForRetry,
                            retryImage = retryImage
                        )
                    )
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini streaming timed out", e)
            finalizeOrRemovePartialMessage(aiMessageId, accumulatedText)
            _uiState.update { state ->
                state.copy(
                    aiState = AiRequestState.Error(
                        message = "Bhai server thoda busy hai, sawal fir se pucho mai turant bata raha hu...",
                        retryPrompt = promptForRetry,
                        retryImage = retryImage
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed", e)
            finalizeOrRemovePartialMessage(aiMessageId, accumulatedText)
            val detail = e.localizedMessage?.takeIf { it.isNotBlank() }
            val fallbackMessage = if (detail != null) {
                "Bhai server thoda busy hai, sawal fir se pucho mai turant bata raha hu...\n($detail)"
            } else {
                "Bhai server thoda busy hai, sawal fir se pucho mai turant bata raha hu..."
            }
            _uiState.update { state ->
                state.copy(
                    aiState = AiRequestState.Error(
                        message = fallbackMessage,
                        retryPrompt = promptForRetry,
                        retryImage = retryImage
                    )
                )
            }
        } finally {
            if (activeStreamingMessageId == aiMessageId) {
                activeStreamingMessageId = null
            }
        }
    }

    private fun finalizeOrRemovePartialMessage(
        aiMessageId: String,
        accumulatedText: StringBuilder
    ) {
        if (accumulatedText.isBlank()) {
            removeStreamingPlaceholder(aiMessageId)
        } else {
            _uiState.update { state ->
                state.copy(
                    messages = state.messages.map { msg ->
                        if (msg.id == aiMessageId) msg.copy(isStreaming = false) else msg
                    }
                )
            }
        }
    }

    private fun removeStreamingPlaceholder(aiMessageId: String) {
        _uiState.update { state ->
            state.copy(messages = state.messages.filterNot { it.id == aiMessageId })
        }
    }

    private fun cleanForSpeech(raw: String): String {
        return raw
            .replace(Regex("[*#`•]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun normalizePrompt(prompt: String): String =
        prompt.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

    private fun currentTimestamp(): String = timeFormatter.format(Date())
}
