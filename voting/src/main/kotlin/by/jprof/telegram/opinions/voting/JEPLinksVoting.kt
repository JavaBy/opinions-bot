package by.jprof.telegram.opinions.voting

import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.answers.answerCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.edit.ReplyMarkup.editMessageReplyMarkup
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.send.sendMessage
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.ChatId
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.MarkdownV2ParseMode
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.types.update.CallbackQueryUpdate
import com.github.insanusmokrassar.TelegramBotAPI.utils.extensions.escapeMarkdownV2Common
import org.apache.logging.log4j.LogManager
import org.jsoup.Jsoup

class JEPLinksVoting(
    private val bot: RequestsExecutor,
    private val votesDAO: VotesDAO
) {
    companion object {
        val logger = LogManager.getLogger(JEPLinksVoting::class.java)!!
    }

    suspend fun processCallbackQuery(callbackQueryUpdate: CallbackQueryUpdate) {
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
                        replyMarkup = InlineKeyboardMarkup(keyboard = votingKeyBoard(updatedVotes, votesId))
                    )
                } else {
                    logger.debug("Unknown callback query. Skipping")
                }
            }

        }
    }

    suspend fun sendVoteForJep(
        chatId: ChatId,
        jep: String,
        replyToMessageId: MessageIdentifier? = null
    ) {
        logger.debug("Reply to JEP {} mention", jep)

        val jepLink = "https://openjdk.java.net/jeps/${jep}"
        val summary = try {
            Jsoup
                .connect(jepLink)
                ?.get()
                ?.select("#Summary + p")
                ?.first()?.text()
        } catch (_: Exception) {
            null
        }

        logger.debug("Summary: {}", summary)

        val jepLinkText = if (replyToMessageId != null) "" else "JEP ${jepLink}\n\n"
        val text = if (summary != null) {
            "${summary}\n\n${jepLinkText}Cast your vote for *JEP $jep* now ⤵️".escapeMarkdownV2Common()
        } else {
            "${jepLinkText}Cast your vote for *JEP $jep* now ⤵️"
        }

        val votesId = constructVotesID(jep)
        val votes = votesDAO.get(votesId) ?: Votes(votesId)
        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = MarkdownV2ParseMode,
            replyToMessageId = replyToMessageId,
            replyMarkup = InlineKeyboardMarkup(keyboard = votingKeyBoard(votes, votesId))
        )
    }

    private fun constructVotesID(jep: String) = "JEP-${jep}"
}
