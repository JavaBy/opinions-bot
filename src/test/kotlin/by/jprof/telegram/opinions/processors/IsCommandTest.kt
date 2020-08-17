package by.jprof.telegram.opinions.processors

import com.github.insanusmokrassar.TelegramBotAPI.CommonAbstracts.TextPart
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.CommonUser
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.BotCommandTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.TelegramDate
import com.github.insanusmokrassar.TelegramBotAPI.types.chat.GroupChatImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.AnonymousForwardInfo
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.soywiz.klock.DateTime
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class IsCommandTest {
    private val cmd = "kotlin-stats"

    @Test
    fun `test calling command then should be called`() =
            assertTrue(isCommand(cmd, mockMessage("/kotlin-stats")))

    @Test
    fun `test calling command with bot username then should be called`() =
            assertTrue(isCommand(cmd, mockMessage("/kotlin-stats@opinions-bot")))

    @Test
    fun `test typo in command then should not be called`() =
            assertFalse(isCommand(cmd, mockMessage("/kotl1n-stats")))

    @Test
    fun `test command without leading slash then should not be called`() =
            assertFalse(isCommand(cmd, mockMessage("kotlin-stats")))

    private fun mockMessage(text: String) = MessageUpdate(1, CommonMessageImpl(
            1L,
            CommonUser(ChatId(1L), "soprano"),
            GroupChatImpl(ChatId(1L), "jprofby"),
            TextContent(text, entities = listOf(
                    TextPart(IntRange(1, 10),
                            BotCommandTextSource(text, emptyList())))),
            DateTime.now(),
            DateTime.now(),
            AnonymousForwardInfo(TelegramDate(DateTime.now()), "unknown"),
            null, null, null, null))
}