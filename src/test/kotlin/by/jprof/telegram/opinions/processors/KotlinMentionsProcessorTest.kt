package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.KotlinMentionsDAO
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendSticker
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.InputFile
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.Request
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.toInputFile
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.GroupChatImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.abstracts.Chat
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.media.StickerContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.soywiz.klock.DateTime
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(MockKExtension::class)
class KotlinMentionsProcessorTest {
    @RelaxedMockK
    private lateinit var reqExecutorMock: RequestsExecutor

    @RelaxedMockK
    private lateinit var kotlinMentionsDAOMock: KotlinMentionsDAO
    private val expectedChatId = ChatId(1L)
    private val expectedStickerMessageId: MessageIdentifier = 1L
    private val expectedPeriodReplyMessageId: MessageIdentifier = 2L
    private val expectedStickerFileId = KotlinMentionsProcessor.zeroDaysWithoutKotlinStickerFileId.toInputFile()

    @BeforeEach
    fun setUp() {
        val contentMessage = mockk<ContentMessage<StickerContent>>(relaxed = true) {
            every { messageId } returns expectedPeriodReplyMessageId
        }
        coEvery { kotlinMentionsDAOMock.getKotlinLastMentionAt(any()) } returns null
        mockkStatic("com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.SendStickerKt")
        coEvery { reqExecutorMock.sendSticker(any(), any(), replyToMessageId = any()) } returns contentMessage
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(reqExecutorMock)
        unmockkAll()
    }

    @Test
    fun `test second reply shouldn't be send if less than X hours spent since first reply`() = runBlocking {
        testStickerWasSent("I don't like kotlin")
        coEvery { kotlinMentionsDAOMock.getKotlinLastMentionAt(any()) } returns Instant.now()
        processUpdate("I don't like kotlin")
        // check by number of invocations that reply wasn't sent
        assertSticker()
    }

    @Test
    fun `test second reply should be send if more than X hours spent since first reply`() = runBlocking {
        processUpdate("I don't like kotlin")
        assertSticker()

        // emulate some delay by shifting 'last-mention' value back
        coEvery {
            kotlinMentionsDAOMock.getKotlinLastMentionAt(any())
        } returns Instant.now().minus(2, ChronoUnit.HOURS)

        processUpdate("I don't like kotlin")
        assertSticker(exactly = 2)

        coVerify { reqExecutorMock.execute(any()) }
    }

    @Test
    fun `test compose without mention message`() {
        val now = Instant.parse("2020-07-20T23:30:30.0Z")
        val message = KotlinMentionsProcessor.composeStickerMessage(
                Duration.between(now
                        .minus(1, ChronoUnit.DAYS)
                        .minus(2, ChronoUnit.HOURS)
                        .minus(3, ChronoUnit.MINUTES)
                        .minusSeconds(4),
                        now))
        assertEquals("We have been existing 01d:02h:03m:04s without mentioning", message)
    }

    @Test
    fun `test send message with previous period without mentioning`() {
        testStickerWasSent("а с котлином прокатывает)")

    }

    @Test
    fun `test with "kotlin" is russian then should send sticker`() {
        testStickerWasSent("а с котлином прокатывает)")
    }

    @Test
    fun `test with "kotlin" in english then should send sticker`() {
        testStickerWasSent("Jsf c kotlin dsl")
    }

    @Test
    fun `test with "kotlin" in english case insensitive then should send sticker`() {
        testStickerWasSent("Kotlin с иммутабельными датаклассами")
    }

    @Test
    fun `test with "kotlin" in english multiline then should send sticker`() {
        testStickerWasSent("""
            лююдиии
            мб кто-то помнит твит какого-то чувак, где он говорит что kotlin разработчики были бы намного эффективнее, 
            если бы не тратили кучу времени доказывая всем вокруг что котлин крут
            никак найти не могу...
        """.trimIndent())
    }

    @Test
    fun `test without kotlin mentioning then sticker shouldn't be sent`() = runBlocking {
        processUpdate("Любимый вопрос после собеса \"Часто ли пользуетесь тем о чем сейчас спрашивали?\"")
        coVerify { reqExecutorMock.execute(any<Request<*>>()) wasNot Called }
    }

    private suspend fun processUpdate(message: String) {
        val processor = KotlinMentionsProcessor(reqExecutorMock, kotlinMentionsDAOMock)
        processor.process(MessageUpdate(1L, mockMessage(message)))
    }

    private fun testStickerWasSent(message: String) = runBlocking {
        processUpdate(message)
        assertSticker()
    }

    private fun assertSticker(exactly: Int = 1) {
        val chatIdSlot = slot<ChatIdentifier>()
        val stickerFileIdSlot = slot<InputFile>()
        val replyToMessageIdSlot = slot<MessageIdentifier>()
        coVerify(exactly = exactly) {
            reqExecutorMock.sendSticker(
                    capture(chatIdSlot),
                    capture(stickerFileIdSlot),
                    replyToMessageId = capture(replyToMessageIdSlot)
            )
        }
        assertEquals(expectedChatId, chatIdSlot.captured)
        assertEquals(expectedStickerFileId, stickerFileIdSlot.captured)
        assertEquals(expectedStickerMessageId, replyToMessageIdSlot.captured)
    }

    private fun mockMessage(text: String): ContentMessage<TextContent> {
        return object : ContentMessage<TextContent> {
            override val chat: Chat
                get() = GroupChatImpl(expectedChatId, "jprofby")
            override val content: TextContent
                get() = TextContent(text)
            override val date: DateTime
                get() = DateTime.now()
            override val messageId: MessageIdentifier
                get() = expectedStickerMessageId
        }
    }
}