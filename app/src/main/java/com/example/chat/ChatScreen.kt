package com.example.chat

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.theme.FastAiBackground
import com.example.ui.theme.FastAiHeaderBlue
import com.example.ui.theme.FastAiOnHeaderBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val copiedToastText = stringResource(R.string.copied_toast)
    val cameraPermissionDeniedText = stringResource(R.string.camera_permission_denied)
    val audioPermissionDeniedText = stringResource(R.string.audio_permission_denied)
    val imageLoadErrorText = stringResource(R.string.image_load_error)

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(uiState.voiceNotice) {
        val notice = uiState.voiceNotice
        if (!notice.isNullOrBlank()) {
            Toast.makeText(context, notice, Toast.LENGTH_SHORT).show()
            viewModel.clearVoiceNotice()
        }
    }

    fun loadBitmapFromUri(uri: Uri) {
        coroutineScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                ImageUtils.decodeScaledBitmapFromUri(context, uri)
            }
            if (bitmap != null) {
                viewModel.onImageSelected(bitmap)
            } else {
                Toast.makeText(context, imageLoadErrorText, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedUri = pendingCameraUri
        pendingCameraUri = null
        if (success && capturedUri != null) {
            loadBitmapFromUri(capturedUri)
        }
    }

    fun launchCamera() {
        val uri = runCatching { ImageUtils.createTempCameraUri(context) }.getOrNull()
        if (uri != null) {
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, imageLoadErrorText, Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(context, cameraPermissionDeniedText, Toast.LENGTH_SHORT).show()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startVoiceListening()
        } else {
            Toast.makeText(context, audioPermissionDeniedText, Toast.LENGTH_SHORT).show()
        }
    }

    // Zero-permission Android Photo Picker per Google Play Policy
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            loadBitmapFromUri(uri)
        }
    }

    val lastMessageLength = uiState.messages.lastOrNull()?.text?.length ?: 0
    val errorState = uiState.aiState as? AiRequestState.Error
    val totalChatItems = uiState.messages.size + (if (errorState != null) 1 else 0)

    LaunchedEffect(totalChatItems, lastMessageLength) {
        if (totalChatItems > 0) {
            listState.scrollToItem(totalChatItems - 1)
        }
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalChatItems > 0 && lastVisible < totalChatItems - 1
        }
    }

    Scaffold(
        containerColor = FastAiBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            FastAiHeader(
                hasMessages = uiState.messages.isNotEmpty(),
                autoSpeakEnabled = uiState.autoSpeakVoiceReplies,
                onToggleAutoSpeak = viewModel::toggleAutoSpeakVoiceReplies,
                onNewChatClick = { viewModel.startNewChat() }
            )
        },
        bottomBar = {
            ChatFooter(
                inputText = uiState.inputText,
                pendingImage = uiState.pendingImage,
                isBusy = uiState.isBusy,
                isListening = uiState.isListening,
                partialVoiceText = uiState.partialVoiceText,
                onInputChanged = viewModel::onInputChanged,
                onCameraClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        launchCamera()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onGalleryClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClearImageClick = viewModel::clearPendingImage,
                onMicClick = {
                    keyboardController?.hide()
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        viewModel.startVoiceListening()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopListeningClick = viewModel::stopVoiceListening,
                onSendClick = {
                    keyboardController?.hide()
                    viewModel.sendCurrentInputOrImage()
                },
                onStopClick = viewModel::stopGenerating
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FastAiBackground,
                            FastAiBackground,
                            FastAiHeaderBlue.copy(alpha = 0.03f)
                        )
                    )
                )
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
            ) {
                if (uiState.messages.isEmpty() && errorState == null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("welcome_list"),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            WelcomeEmptyState(
                                onStarterClick = { starterPrompt ->
                                    viewModel.sendMessage(overridePrompt = starterPrompt)
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("chat_message_list"),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
                    ) {
                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { message ->
                            MessageBubble(
                                message = message,
                                isSpeaking = uiState.speakingMessageId == message.id,
                                onCopyClick = { textToCopy ->
                                    clipboardManager.setText(AnnotatedString(textToCopy))
                                    Toast.makeText(context, copiedToastText, Toast.LENGTH_SHORT)
                                        .show()
                                },
                                onSpeakClick = viewModel::toggleSpeakMessage
                            )
                        }

                        if (errorState != null) {
                            item(key = "error_banner_item") {
                                ChatErrorBanner(
                                    message = errorState.message,
                                    canRetry = errorState.retryPrompt != null,
                                    onRetry = viewModel::retryLastFailedPrompt,
                                    onDismiss = viewModel::dismissError
                                )
                            }
                        }

                        item(key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showScrollToBottom,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 14.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                if (totalChatItems > 0) {
                                    listState.animateScrollToItem(totalChatItems - 1)
                                }
                            }
                        },
                        shape = CircleShape,
                        containerColor = FastAiHeaderBlue,
                        contentColor = FastAiOnHeaderBlue,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("scroll_to_bottom_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.scroll_to_bottom_cd)
                        )
                    }
                }
            }
        }
    }
}
