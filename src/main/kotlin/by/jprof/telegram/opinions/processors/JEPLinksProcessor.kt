package by.jprof.telegram.opinions.processors

import by.dev.madhead.telek.model.CallbackQuery
import by.dev.madhead.telek.model.InlineKeyboardButton
import by.dev.madhead.telek.model.InlineKeyboardMarkup
import by.dev.madhead.telek.model.Message
import by.dev.madhead.telek.model.ParseMode
import by.dev.madhead.telek.model.Update
import by.dev.madhead.telek.model.communication.AnswerCallbackQueryRequest
import by.dev.madhead.telek.model.communication.ChatId
import by.dev.madhead.telek.model.communication.EditMessageReplyMarkupRequest
import by.dev.madhead.telek.model.communication.SendMessageRequest
import by.dev.madhead.telek.telek.Telek
import by.jprof.telegram.opinions.dao.VotesDAO
import by.jprof.telegram.opinions.entity.Votes
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.apache.logging.log4j.LogManager

class JEPLinksProcessor(
        private val telek: Telek,
        private val votesDAO: VotesDAO
) : UpdateProcessor {
    companion object {
        val logger = LogManager.getLogger(JEPLinksProcessor::class.java)
        val siteRegex = "https?://openjdk\\.java\\.net/jeps/(\\d+)/?".toRegex()
    }

    override suspend fun process(update: Update) {
        val message = update.message
        val callbackQuery = update.callbackQuery

        when {
            message != null -> processMessage(message)
            callbackQuery != null -> processCallbackQuery(callbackQuery)
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

    private suspend fun processCallbackQuery(callbackQuery: CallbackQuery) {
        logger.debug("Processing callback query")

        callbackQuery.data?.let { data ->
            if (data.startsWith("JEP")) {
                val (votesId, vote) = try {
                    data.split(":")
                } catch (e: Exception) {
                    logger.warn("Bad callback data", e)

                    return
                }

                logger.debug("Tracking {}'s '{}' vote for {}", callbackQuery.from.id, vote, votesId)

                val votes = (votesDAO.get(votesId) ?: Votes(votesId))
                val updatedVotes = votes.copy(votes = votes.votes + (callbackQuery.from.id.toString() to vote))

                votesDAO.save(updatedVotes)
                telek.answerCallbackQuery(AnswerCallbackQueryRequest(callbackQueryId = callbackQuery.id))
                callbackQuery.message?.let { message ->
                    telek.editMessageReplyMarkup(EditMessageReplyMarkupRequest(
                            chatId = ChatId.of(message.chat.id),
                            messageId = message.messageId,
                            replyMarkup = InlineKeyboardMarkup(
                                    inlineKeyboard = listOf(
                                            listOf(
                                                    InlineKeyboardButton(text = "${updatedVotes.upvotes} 👍", callbackData = "$votesId:+"),
                                                    InlineKeyboardButton(text = "${updatedVotes.downvotes} 👎", callbackData = "$votesId:-")
                                            )
                                    )
                            )
                    ))
                }
            } else {
                logger.debug("Unknown callback query. Skipping")
            }
        }
    }

    private suspend fun replyToJEPMention(jep: String, message: Message) {
        logger.debug("Reply to JEP {} mention", jep)

        val votesId = constructVotesID(jep)
        val votes = votesDAO.get(votesId)

        if (null != votes) {
            logger.debug("Votes record found for {} : {}", jep, votes)

            telek.sendMessage(
                    SendMessageRequest(
                            chatId = ChatId.of(message.chat.id),
                            text = "Cast your vote for *JEP $jep* now ⤵️",
                            parseMode = ParseMode.MarkdownV2,
                            replyToMessageId = message.messageId,
                            replyMarkup = InlineKeyboardMarkup(
                                    inlineKeyboard = listOf(
                                            listOf(
                                                    InlineKeyboardButton(text = "${votes.upvotes} 👍", callbackData = "$votesId:+"),
                                                    InlineKeyboardButton(text = "${votes.downvotes} 👎", callbackData = "$votesId:-")
                                            )
                                    )
                            )
                    )
            )
        } else {
            logger.debug("No votes for {} yet", jep)

            votesDAO.save(Votes(votesId))
            telek.sendMessage(
                    SendMessageRequest(
                            chatId = ChatId.of(message.chat.id),
                            text = "Cast your vote for *JEP $jep* now ⤵️",
                            parseMode = ParseMode.MarkdownV2,
                            replyToMessageId = message.messageId,
                            replyMarkup = InlineKeyboardMarkup(
                                    inlineKeyboard = listOf(
                                            listOf(
                                                    InlineKeyboardButton(text = "👍", callbackData = "$votesId:+"),
                                                    InlineKeyboardButton(text = "👎", callbackData = "$votesId:-")
                                            )
                                    )
                            )
                    )
            )
        }
    }

    private fun extractJEPMentions(message: Message): List<String>? {
        return message
                .entities
                ?.let { entities ->
                    entities
                            .filter { (it.type == "text_link") || (it.type == "url") }
                            .mapNotNull {
                                when (it.type) {
                                    "text_link" -> it.url
                                    "url" -> message.text?.substring(it.offset, it.offset + it.length)
                                    else -> throw IllegalStateException("Unexpected entity type: ${it.type}")
                                }
                            }
                            .mapNotNull {
                                siteRegex.matchEntire(it)?.destructured
                            }
                            .map { (jep) ->
                                jep
                            }
                }
    }

    private fun constructVotesID(jep: String) = "JEP-${jep}"

    private val Votes.upvotes: Int
        get() = this.votes.count { (_, vote) -> vote == "+" }

    private val Votes.downvotes: Int
        get() = this.votes.count { (_, vote) -> vote == "-" }
}
