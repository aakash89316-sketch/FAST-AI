package com.example.chat

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FastAiAiBubble
import com.example.ui.theme.FastAiAiBubbleBorder
import com.example.ui.theme.FastAiBorderSubtle
import com.example.ui.theme.FastAiErrorBorder
import com.example.ui.theme.FastAiErrorContainer
import com.example.ui.theme.FastAiHeaderBlue
import com.example.ui.theme.FastAiInputSurface
import com.example.ui.theme.FastAiOnErrorContainer
import com.example.ui.theme.FastAiOnHeaderBlue
import com.example.ui.theme.FastAiSurface
import com.example.ui.theme.FastAiTextMuted
import com.example.ui.theme.FastAiTextPrimary
import com.example.ui.theme.FastAiTextSecondary
import com.example.ui.theme.FastAiUserBubble
import com.example.ui.theme.FastAiUserBubbleBorder

@Composable
fun FastAiHeader(
    hasMessages: Boolean,
    autoSpeakEnabled: Boolean = true,
    onToggleAutoSpeak: () -> Unit = {},
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FastAiSurface.copy(alpha = 0.94f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FastAiHeaderBlue.copy(alpha = 0.14f))
                            .border(
                                width = 1.dp,
                                color = FastAiHeaderBlue.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = FastAiHeaderBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.header_title),
                            color = FastAiHeaderBlue,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("header_title")
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(FastAiHeaderBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.header_subtitle_hindi),
                                color = FastAiTextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleAutoSpeak,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("auto_voice_toggle")
                    ) {
                        Icon(
                            imageVector = if (autoSpeakEnabled) {
                                Icons.AutoMirrored.Filled.VolumeUp
                            } else {
                                Icons.AutoMirrored.Filled.VolumeOff
                            },
                            contentDescription = stringResource(
                                if (autoSpeakEnabled) R.string.auto_voice_on_cd else R.string.auto_voice_off_cd
                            ),
                            tint = if (autoSpeakEnabled) FastAiHeaderBlue else FastAiTextMuted
                        )
                    }

                    AnimatedVisibility(
                        visible = hasMessages,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = onNewChatClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("new_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddComment,
                                contentDescription = stringResource(R.string.new_chat_cd),
                                tint = FastAiHeaderBlue
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = FastAiHeaderBlue.copy(alpha = 0.22f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeEmptyState(
    onStarterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val starters = listOf(
        stringResource(R.string.starter_1),
        stringResource(R.string.starter_2),
        stringResource(R.string.starter_3),
        stringResource(R.string.starter_4)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            FastAiHeaderBlue.copy(alpha = 0.8f),
                            FastAiHeaderBlue.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.fast_ai_icon_1794925558364),
                contentDescription = stringResource(R.string.ai_avatar_cd),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.welcome_title),
            color = FastAiTextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.welcome_subtitle),
            color = FastAiTextMuted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 440.dp)
        )

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = stringResource(R.string.starter_heading),
            color = FastAiHeaderBlue,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            starters.forEachIndexed { index, prompt ->
                Surface(
                    color = FastAiAiBubble.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, FastAiHeaderBlue.copy(alpha = 0.28f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onStarterClick(prompt) }
                        .testTag("starter_chip_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = FastAiHeaderBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = prompt,
                            color = FastAiTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isSpeaking: Boolean = false,
    onCopyClick: (String) -> Unit,
    onSpeakClick: (ChatMessage) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageSender.USER
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    } else {
        RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        val bubbleMaxWidth = (maxWidth * 0.84f).coerceAtMost(540.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, bottom = 2.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(FastAiAiBubble)
                        .border(1.dp, FastAiHeaderBlue.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = FastAiHeaderBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Surface(
                color = if (isUser) FastAiUserBubble else FastAiAiBubble,
                shape = bubbleShape,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isUser) {
                        FastAiUserBubbleBorder.copy(alpha = 0.4f)
                    } else {
                        FastAiAiBubbleBorder.copy(alpha = 0.7f)
                    }
                ),
                modifier = Modifier
                    .widthIn(min = 64.dp, max = bubbleMaxWidth)
                    .testTag(if (isUser) "user_message_bubble" else "ai_message_bubble")
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 14.dp,
                        end = 14.dp,
                        top = 10.dp,
                        bottom = 8.dp
                    )
                ) {
                    message.imageBitmap?.let { bitmap ->
                        val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = stringResource(R.string.selected_image_cd),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = FastAiHeaderBlue.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("message_image_attachment")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (isUser) {
                        Text(
                            text = message.text,
                            color = FastAiTextPrimary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        val displayText = if (message.isStreaming) {
                            if (message.text.isEmpty()) "▌" else "${message.text} ▌"
                        } else {
                            message.text
                        }
                        FormattedMessageText(
                            text = displayText,
                            color = FastAiTextPrimary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.align(if (isUser) Alignment.End else Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = message.timestampLabel,
                            color = FastAiTextSecondary.copy(alpha = 0.68f),
                            style = MaterialTheme.typography.labelSmall
                        )

                        if (!isUser && !message.isStreaming && message.text.isNotBlank()) {
                            Surface(
                                color = if (isSpeaking) {
                                    FastAiHeaderBlue.copy(alpha = 0.24f)
                                } else {
                                    FastAiHeaderBlue.copy(alpha = 0.12f)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, FastAiHeaderBlue.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSpeakClick(message) }
                                    .testTag("speak_ai_message_${message.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) {
                                            Icons.Filled.Stop
                                        } else {
                                            Icons.AutoMirrored.Filled.VolumeUp
                                        },
                                        contentDescription = stringResource(R.string.speak_message_cd),
                                        tint = FastAiHeaderBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = stringResource(
                                            if (isSpeaking) R.string.stop_speak_label else R.string.speak_label
                                        ),
                                        color = FastAiHeaderBlue,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Surface(
                                color = FastAiHeaderBlue.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, FastAiHeaderBlue.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onCopyClick(message.text) }
                                    .testTag("copy_ai_message_${message.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = stringResource(R.string.copy_message_cd),
                                        tint = FastAiHeaderBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.copy_label),
                                        color = FastAiHeaderBlue,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatErrorBanner(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = FastAiErrorContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, FastAiErrorBorder.copy(alpha = 0.55f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("error_banner")
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    color = FastAiOnErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (canRetry) {
                    TextButton(
                        onClick = onRetry,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("retry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = FastAiHeaderBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.retry_button),
                            color = FastAiHeaderBlue,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("dismiss_error_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.dismiss_cd),
                    tint = FastAiOnErrorContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatFooter(
    inputText: String,
    pendingImage: Bitmap? = null,
    isBusy: Boolean,
    isListening: Boolean = false,
    partialVoiceText: String = "",
    onInputChanged: (String) -> Unit,
    onCameraClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onClearImageClick: () -> Unit = {},
    onMicClick: () -> Unit = {},
    onStopListeningClick: () -> Unit = {},
    onSendClick: () -> Unit,
    onStopClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val canSend = (inputText.isNotBlank() || pendingImage != null) && !isBusy

    Surface(
        color = FastAiSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = FastAiBorderSubtle
            )

            AnimatedVisibility(visible = isListening) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    VoiceListeningBanner(
                        partialText = partialVoiceText,
                        onStopClick = onStopListeningClick,
                        modifier = Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            AnimatedVisibility(visible = pendingImage != null) {
                pendingImage?.let { bitmap ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        PendingImagePreviewBar(
                            bitmap = bitmap,
                            onRemoveClick = onClearImageClick,
                            modifier = Modifier
                                .widthIn(max = 720.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onCameraClick,
                        enabled = !isBusy,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FastAiInputSurface)
                            .border(1.dp, FastAiBorderSubtle, CircleShape)
                            .testTag("camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = stringResource(R.string.camera_button_cd),
                            tint = if (isBusy) FastAiTextMuted else FastAiHeaderBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onGalleryClick,
                        enabled = !isBusy,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FastAiInputSurface)
                            .border(1.dp, FastAiBorderSubtle, CircleShape)
                            .testTag("gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = stringResource(R.string.gallery_button_cd),
                            tint = if (isBusy) FastAiTextMuted else FastAiHeaderBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChanged,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp, max = 140.dp)
                            .testTag("chat_input_field"),
                        placeholder = {
                            Text(
                                text = if (pendingImage != null) {
                                    stringResource(R.string.image_question_placeholder)
                                } else {
                                    stringResource(R.string.input_placeholder)
                                },
                                color = FastAiTextMuted,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = FastAiTextPrimary
                        ),
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FastAiInputSurface,
                            unfocusedContainerColor = FastAiInputSurface,
                            disabledContainerColor = FastAiInputSurface.copy(alpha = 0.7f),
                            focusedBorderColor = FastAiHeaderBlue,
                            unfocusedBorderColor = FastAiBorderSubtle,
                            cursorColor = FastAiHeaderBlue
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (canSend) onSendClick()
                            }
                        )
                    )

                    Surface(
                        color = if (isListening) FastAiHeaderBlue else FastAiInputSurface,
                        shape = CircleShape,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isListening) FastAiHeaderBlue else FastAiBorderSubtle
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                enabled = !isBusy,
                                role = Role.Button,
                                onClickLabel = stringResource(R.string.mic_button_cd),
                                onClick = {
                                    if (isListening) onStopListeningClick() else onMicClick()
                                },
                                onLongClick = {
                                    if (!isListening) onMicClick()
                                }
                            )
                            .testTag("voice_mic_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = stringResource(R.string.mic_button_cd),
                                tint = when {
                                    isListening -> FastAiOnHeaderBlue
                                    isBusy -> FastAiTextMuted
                                    else -> FastAiHeaderBlue
                                },
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    if (isBusy) {
                        Button(
                            onClick = onStopClick,
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FastAiErrorContainer,
                                contentColor = FastAiOnErrorContainer
                            ),
                            border = BorderStroke(1.dp, FastAiErrorBorder.copy(alpha = 0.75f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier
                                .heightIn(min = 50.dp)
                                .testTag("stop_generating_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = stringResource(R.string.stop_generating_cd),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.stop_generating_button),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = onSendClick,
                            enabled = canSend,
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FastAiHeaderBlue,
                                contentColor = FastAiOnHeaderBlue,
                                disabledContainerColor = FastAiInputSurface,
                                disabledContentColor = FastAiTextMuted
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier
                                .heightIn(min = 50.dp)
                                .testTag("send_button")
                        ) {
                            Text(
                                text = stringResource(R.string.send_button_text),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceListeningBanner(
    partialText: String,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse_alpha"
    )

    Surface(
        color = FastAiAiBubble,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, FastAiHeaderBlue.copy(alpha = 0.7f)),
        modifier = modifier.testTag("voice_listening_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(FastAiHeaderBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = FastAiHeaderBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.voice_listening_label),
                    color = FastAiHeaderBlue,
                    style = MaterialTheme.typography.labelLarge
                )
                if (partialText.isNotBlank()) {
                    Text(
                        text = partialText,
                        color = FastAiTextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            TextButton(
                onClick = onStopClick,
                modifier = Modifier.testTag("voice_stop_button")
            ) {
                Text(
                    text = stringResource(R.string.voice_stop_listening),
                    color = FastAiOnErrorContainer,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PendingImagePreviewBar(
    bitmap: Bitmap,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Surface(
        color = FastAiInputSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, FastAiHeaderBlue.copy(alpha = 0.45f)),
        modifier = modifier.testTag("pending_image_preview")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = stringResource(R.string.selected_image_cd),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, FastAiHeaderBlue.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.image_ready_label),
                    color = FastAiTextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.image_question_placeholder),
                    color = FastAiHeaderBlue,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("remove_pending_image_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.remove_image_cd),
                    tint = FastAiTextSecondary
                )
            }
        }
    }
}
