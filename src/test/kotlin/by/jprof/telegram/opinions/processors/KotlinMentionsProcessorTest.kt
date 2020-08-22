package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.KotlinMentionsDAO
import by.jprof.telegram.opinions.entity.KotlinMention
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendSticker
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.InputFile
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.Request
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.toInputFile
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.CommonUser
import com.github.insanusmokrassar.TelegramBotAPI.types.TelegramDate
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.GroupChatImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.AnonymousForwardInfo
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.media.StickerContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.utils.TelegramAPIUrlsKeeper
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(MockKExtension::class)
class KotlinMentionsProcessorTest {
    @RelaxedMockK
    private lateinit var reqExecutorMock: RequestsExecutor
    @RelaxedMockK
    private lateinit var tesseract: Tesseract

    @RelaxedMockK
    private lateinit var kotlinMentionsDAOMock: KotlinMentionsDAO
    private val expectedChatId = ChatId(1L)
    private val expectedUserId = CommonUser(ChatId(1L), "soprano")
    private val expectedStickerMessageId: MessageIdentifier = 1L
    private val expectedPeriodReplyMessageId: MessageIdentifier = 2L
    private val expectedStickerFileId = "CAACA".toInputFile()

    @BeforeEach
    fun setUp() {
        val contentMessage = mockk<ContentMessage<StickerContent>>(relaxed = true) {
            every { messageId } returns expectedPeriodReplyMessageId
        }
        coEvery { kotlinMentionsDAOMock.find(any()) } returns null
        mockkStatic("com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.SendStickerKt")
        coEvery { reqExecutorMock.sendSticker(any(), any(), replyToMessageId = any()) } returns contentMessage
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(reqExecutorMock)
        unmockkAll()
    }

    @Test
    fun `test sticker sending throttling doesnt affect mention stats`() = runBlocking {
        coEvery { kotlinMentionsDAOMock.find(any()) } returns kotlinMention(Instant.now())

        val slot = slot<KotlinMention>()

        processUpdate("I don't like kotlin")
        coVerify { kotlinMentionsDAOMock.save(capture(slot)) }
        coEvery { kotlinMentionsDAOMock.find(any()) } returns slot.captured

        processUpdate("I don't like kotlin")
        coVerify { kotlinMentionsDAOMock.save(capture(slot)) }
        coEvery { kotlinMentionsDAOMock.find(any()) } returns slot.captured

        processUpdate("I don't like kotlin")
        coVerify { kotlinMentionsDAOMock.save(capture(slot)) }
        coEvery { kotlinMentionsDAOMock.find(any()) } returns slot.captured

        assertEquals(1, slot.captured.users.size)
        assertEquals(3, slot.captured.users[1]?.count)
    }

    @Test
    fun `test second reply shouldn't be send if less than X hours spent since first reply`() = runBlocking {
        testStickerWasSent("I don't like kotlin")
        coEvery { kotlinMentionsDAOMock.find(any()) } returns kotlinMention(Instant.now())
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
            kotlinMentionsDAOMock.find(any())
        } returns kotlinMention(Instant.now().minus(2, ChronoUnit.HOURS))

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
        assertEquals("Passed 01d:02h:03m:04s without an incident", message)
    }

    @ParameterizedTest(name = "{index} test mentioning \"{0}\" then should send")
    @ValueSource(strings = [
        "А если к0тлин?",
        "А если к0тлен?",
        "А если к0тл1н?",
        "А если котлен?",
        "А если котл1н?",
        "А если ккккотлин?",
        "А если кооооотлин?",
        "А если котлиииин?",
        "А если котл1ннннн?",
        "А если котл1HHHHH?",
        "А если k0tlin?",
        "А если k0tlеn?",
        "А если k0tl1n?",
        "А если kotlen?",
        "А если kotl1n?",
        "А если k0ooottttlin?",
        "А если k0tliiii111n?",
        "А если koootliiiiin?",
        "А если kkoottlliinn?",
        "А если kкоo0тtлlие1ieнn?",
        "А если K.o.t.l.i.n?",
        "А если катлен?",
        "А если котлен?",
        "а с котлином прокатывает)",
        "Jsf c kotlin dsl",
        "Kotlin с иммутабельными датаклассами",
        """
            лююдиии
            мб кто-то помнит твит какого-то чувак, где он говорит что kotlin разработчики были бы намного эффективнее, 
            если бы не тратили кучу времени доказывая всем вокруг что котлин крут
            никак найти не могу...
        """]
    )
    fun `test mentioning fuzzy kotlin then should be send`(kotlinTypo: String) = testStickerWasSent(kotlinTypo)

    @Test
    fun `test without kotlin mentioning then sticker shouldn't be sent`() = runBlocking {
        processUpdate("Любимый вопрос после собеса \"Часто ли пользуетесь тем о чем сейчас спрашивали?\"")
        coVerify { reqExecutorMock.execute(any<Request<*>>()) wasNot Called }
    }

    private suspend fun processUpdate(message: String) {
        val processor = KotlinMentionsProcessor(
                reqExecutorMock, kotlinMentionsDAOMock, tesseract,
                TelegramAPIUrlsKeeper("qwerty"),
                stickerFileId = expectedStickerFileId.fileId)
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
        return CommonMessageImpl(
                expectedStickerMessageId,
                expectedUserId,
                GroupChatImpl(expectedChatId, "jprofby"),
                TextContent(text),
                DateTime.now(),
                DateTime.now(),
                AnonymousForwardInfo(TelegramDate(DateTime.now()), "unknown"),
                null, null, null, null)
    }

    private fun kotlinMention(
            timestamp: Instant,
            chatId: ChatId = expectedChatId
    ) = KotlinMention(chatId.chatId, timestamp)
}