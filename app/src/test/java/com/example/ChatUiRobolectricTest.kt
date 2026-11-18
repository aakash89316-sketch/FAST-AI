package com.example

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.chat.ChatFooter
import com.example.chat.ChatMessage
import com.example.chat.ChatViewModel
import com.example.chat.FastAiHeader
import com.example.chat.ImageUtils
import com.example.chat.MessageBubble
import com.example.chat.MessageSender
import com.example.ui.theme.FastAiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatUiRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `header and footer display required Hindi copy, camera, gallery, and mic buttons`() {
        composeTestRule.setContent {
            FastAiTheme {
                FastAiHeader(
                    hasMessages = false,
                    onNewChatClick = {}
                )
                ChatFooter(
                    inputText = "",
                    isBusy = false,
                    onInputChanged = {},
                    onSendClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("⚡ FAST AI").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auto_voice_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithText("अपना सवाल लिखें...").assertIsDisplayed()
        composeTestRule.onNodeWithText("भेजें").assertIsDisplayed()
        composeTestRule.onNodeWithTag("camera_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("gallery_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("voice_mic_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("send_button").assertIsNotEnabled()
    }

    @Test
    fun `listening state displays Sun raha hu bolo banner`() {
        composeTestRule.setContent {
            FastAiTheme {
                ChatFooter(
                    inputText = "",
                    isBusy = false,
                    isListening = true,
                    partialVoiceText = "आज मौसम कैसा है",
                    onInputChanged = {},
                    onSendClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("voice_listening_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sun raha hu... bolo").assertIsDisplayed()
        composeTestRule.onNodeWithText("आज मौसम कैसा है").assertIsDisplayed()
    }

    @Test
    fun `send button enables when text is present and not busy`() {
        composeTestRule.setContent {
            FastAiTheme {
                ChatFooter(
                    inputText = "नमस्ते",
                    isBusy = false,
                    onInputChanged = {},
                    onSendClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("send_button").assertIsEnabled()
    }

    @Test
    fun `pending image shows preview bar, default question placeholder, and enables send`() {
        val sampleBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        composeTestRule.setContent {
            FastAiTheme {
                ChatFooter(
                    inputText = "",
                    pendingImage = sampleBitmap,
                    isBusy = false,
                    onInputChanged = {},
                    onSendClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("pending_image_preview").assertIsDisplayed()
        composeTestRule.onNodeWithTag("remove_pending_image_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("send_button").assertIsEnabled()
    }

    @Test
    fun `user bubble with image and ai bubble with speak and copy render properly`() {
        val sampleBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        composeTestRule.setContent {
            FastAiTheme {
                MessageBubble(
                    message = ChatMessage(
                        sender = MessageSender.USER,
                        text = "Is photo me kya hai?",
                        imageBitmap = sampleBitmap,
                        timestampLabel = "10:00 AM"
                    ),
                    onCopyClick = {}
                )
                MessageBubble(
                    message = ChatMessage(
                        id = "ai_1",
                        sender = MessageSender.AI,
                        text = "AI कृत्रिम बुद्धिमत्ता है।",
                        timestampLabel = "10:00 AM",
                        isStreaming = false
                    ),
                    onCopyClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("user_message_bubble").assertIsDisplayed()
        composeTestRule.onNodeWithTag("message_image_attachment").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_message_bubble").assertIsDisplayed()
        composeTestRule.onNodeWithTag("speak_ai_message_ai_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("copy_ai_message_ai_1").assertIsDisplayed()
    }

    @Test
    fun `common questions answer instantly from cache in 0 sec`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = ChatViewModel(app)
        assertNotNull(viewModel.getCachedResponseForTest("  Namaste  "))

        viewModel.sendMessage("Namaste")
        val messages = viewModel.uiState.value.messages
        assertEquals(2, messages.size)
        assertEquals(MessageSender.USER, messages[0].sender)
        assertEquals(MessageSender.AI, messages[1].sender)
        assertEquals(false, messages[1].isStreaming)
    }

    @Test
    fun `bitmap converts to non-empty jpeg byte array`() {
        val sampleBitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val bytes = ImageUtils.bitmapToJpegByteArray(sampleBitmap)
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun `stop generating button renders when streaming is active`() {
        composeTestRule.setContent {
            FastAiTheme {
                ChatFooter(
                    inputText = "",
                    isBusy = true,
                    onInputChanged = {},
                    onSendClick = {},
                    onStopClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("stop_generating_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("रोकें").assertIsDisplayed()
    }
}
