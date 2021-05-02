package by.jprof.telegram.opinions.webhook.processors

import by.jprof.telegram.opinions.voting.JEPLinksVoting
import com.github.insanusmokrassar.TelegramBotAPI.CommonAbstracts.justTextSources
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.TextLinkTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.URLTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.ContentMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.CallbackQueryUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.apache.logging.log4j.LogManager

class JEPLinksProcessor(
    private val jepsVoting: JEPLinksVoting
) : UpdateProcessor {
    companion object {
        val logger = LogManager.getLogger(JEPLinksProcessor::class.java)!!
        val siteRegex = "https?://openjdk\\.java\\.net/jeps/(\\d+)/?".toRegex()
    }

    override suspend fun process(update: Update) {
        when (update) {
            is MessageUpdate -> processMessage(update.data)
            is CallbackQueryUpdate -> jepsVoting.processCallbackQuery(update)
        }
    }

    private suspend fun processMessage(message: Message) {
        logger.debug("Processing message")

        val jepMentions = extractJEPMentions(message) ?: return

        logger.debug("JEP mentions: {}", jepMentions)

        supervisorScope {
            jepMentions
                    .map { launch { replyToJEPMention(it, message) } }
                    .joinAll()
        }
    }

    private suspend fun replyToJEPMention(jep: String, message: Message) {
        jepsVoting.sendVoteForJep(
            message.chat.id,
            jep,
            replyToMessageId = message.messageId,
        )
    }

    private fun extractJEPMentions(message: Message): List<String>? {
        return (message as ContentMessage<*>).let { msg ->
            (msg.content as? TextContent).let { textContent ->
                textContent?.entities?.justTextSources()
                        ?.mapNotNull {
                            (it as? URLTextSource)?.source ?: (it as? TextLinkTextSource)?.url
                        }
                        ?.mapNotNull { siteRegex.matchEntire(it)?.destructured }
                        ?.map { (jep) -> jep }
            }
        }
    }
}
