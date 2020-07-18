package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.KotlinMentionsDAO
import by.jprof.telegram.opinions.entity.KotlinMention
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.media.sendSticker
import com.github.insanusmokrassar.TelegramBotAPI.requests.abstracts.FileId
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import org.apache.logging.log4j.LogManager
import java.time.Instant

class KotlinMentionsProcessor(
        private val bot: RequestsExecutor,
        private val kotlinMentionsDAO: KotlinMentionsDAO,
        private val cooldown: Long
) : UpdateProcessor {
    companion object {
        private val logger = LogManager.getLogger(KotlinMentionsProcessor::class.java)!!
    }

    override suspend fun process(update: Update) {
        logger.debug("Processing update {}", update)

        when (update) {
            is MessageUpdate -> processMessage(update.data)
        }
    }

    private suspend fun processMessage(message: Message) {
        (message as ContentMessage<*>).let { msg ->
            (msg.content as? TextContent).let { textContent ->
                if (textContent?.text?.contains("kotlin|котлин".toRegex(RegexOption.IGNORE_CASE)) == true) {
                    val latestKotlinMention = kotlinMentionsDAO.get(message.chat.id.chatId.toString())
                            ?: KotlinMention(message.chat.id.chatId.toString(), 0)

                    if (Instant.now().toEpochMilli() - latestKotlinMention.timestamp > cooldown) {
                        logger.debug("Latest Kotlin mention was at ${latestKotlinMention.timestamp}, triggered!")

                        kotlinMentionsDAO.save(KotlinMention(message.chat.id.chatId.toString(), Instant.now().toEpochMilli()))
                        bot.sendSticker(
                                chatId = message.chat.id,
                                sticker = FileId("CAACAgIAAxkBAAO9XxN_MPcGhdgk0A7HyJ0FsmvGwhEAAkABAAKpIiYE6zkOZI20qWYaBA"),
                                replyToMessageId = message.messageId
                        )
                    }
                }
            }
        }
    }
}
