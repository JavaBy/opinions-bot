package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.VotesDAO
import by.jprof.telegram.opinions.entity.*
import com.github.insanusmokrassar.TelegramBotAPI.CommonAbstracts.TextSource
import com.github.insanusmokrassar.TelegramBotAPI.CommonAbstracts.justTextSources
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.answers.answerCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.edit.ReplyMarkup.editMessageReplyMarkup
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.TextLinkTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.URLTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.MarkdownV2ParseMode
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
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
        private val bot: RequestsExecutor,
        private val votesDAO: VotesDAO
) : UpdateProcessor {
    companion object {
        val logger = LogManager.getLogger(JEPLinksProcessor::class.java)!!
        val siteRegex = "https?://openjdk\\.java\\.net/jeps/(\\d+)/?".toRegex()
    }

    override suspend fun process(update: Update) {
        when (update) {
            is MessageUpdate -> processMessage(update.data)
            is CallbackQueryUpdate -> processCallbackQuery(update)
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

    private suspend fun processCallbackQuery(callbackQueryUpdate: CallbackQueryUpdate) {
        logger.debug("Processing callback query")

        (callbackQueryUpdate.data as? MessageDataCallbackQuery).let { callbackQuery ->
            callbackQuery?.data?.let { data ->
                if (data.startsWith("JEP")) {
                    val (votesId, vote) = try {
                        data.split(":")
                    } catch (e: Exception) {
                        logger.warn("Bad callback data", e)

                        return
                    }
                    val fromUserId = callbackQuery.user.id.chatId.toString()

                    logger.debug("Tracking {}'s '{}' vote for {}", fromUserId, vote, votesId)

                    val votes = (votesDAO.get(votesId) ?: Votes(votesId))
                    val updatedVotes = votes.copy(votes = votes.votes + (fromUserId to vote))

                    votesDAO.save(updatedVotes)
                    bot.answerCallbackQuery(callbackQuery)

                    bot.editMessageReplyMarkup(
                            message = callbackQuery.message,
                            replyMarkup = InlineKeyboardMarkup(keyboard = votingKeyBoard(votes, votesId))
                    )

                } else {
                    logger.debug("Unknown callback query. Skipping")
                }
            }

        }
    }

    private suspend fun replyToJEPMention(jep: String, message: Message) {
        logger.debug("Reply to JEP {} mention", jep)
        val votesId = constructVotesID(jep)
        val votes = votesDAO.get(votesId) ?: Votes(votesId)
        bot.sendMessage(
                chatId = message.chat.id,
                text = "Cast your vote for *JEP $jep* now ⤵️",
                parseMode = MarkdownV2ParseMode,
                replyToMessageId = message.messageId,
                replyMarkup = InlineKeyboardMarkup(keyboard = votingKeyBoard(votes, votesId))

        )
    }

    private fun extractJEPMentions(message: Message): List<String>? {
        return (message as ContentMessage<*>).let { msg ->
            (msg.content as? TextContent).let { textSource ->
                textSource?.entities?.justTextSources()
                        ?.filter { isUrlOrTextLink(it) }
                        ?.mapNotNull { siteRegex.matchEntire(it.source)?.destructured }
                        ?.map { (jep) -> jep }
            }
        }
    }

    private fun isUrlOrTextLink(it: TextSource) = it is URLTextSource || it is TextLinkTextSource

    private fun constructVotesID(jep: String) = "JEP-${jep}"

}
