package by.jprof.telegram.opinions.webhook.processors

import by.jprof.telegram.opinions.voting.Votes
import by.jprof.telegram.opinions.voting.VotesDAO
import by.jprof.telegram.opinions.webhook.dao.KeyboardsDAO
import by.jprof.telegram.opinions.webhook.entity.Button
import by.jprof.telegram.opinions.webhook.entity.toInlineKeyboardMarkup
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.answers.answerCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.edit.ReplyMarkup.editMessageReplyMarkup
import com.github.insanusmokrassar.TelegramBotAPI.types.CallbackQuery.MessageDataCallbackQuery
import com.github.insanusmokrassar.TelegramBotAPI.types.update.CallbackQueryUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import org.apache.logging.log4j.LogManager

class CustomVotesProcessor(
    private val bot: RequestsExecutor,
    private val votesDAO: VotesDAO,
    private val keyboardsDAO: KeyboardsDAO
) : UpdateProcessor {
    companion object {
        val logger = LogManager.getLogger(CustomVotesProcessor::class.java)!!
    }

    override suspend fun process(update: Update) {
        when (update) {
            is CallbackQueryUpdate -> processCallbackQuery(update)
        }
    }

    private suspend fun processCallbackQuery(callbackQueryUpdate: CallbackQueryUpdate) {
        logger.debug("Processing callback query")

        (callbackQueryUpdate.data as? MessageDataCallbackQuery).let { callbackQuery ->
            callbackQuery?.data?.let { data ->
                if (data.startsWith("CUSTOM")) {
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
                    keyboardsDAO.get(votesId)?.let {
                        logger.debug("Custom keyboard: {}", it)

                        val keyboard = it.copy(buttons = it.buttons.map { row ->
                            row.map { button ->
                                Button("${updatedVotes.votes.count { (_, vote) -> vote == button.data }} ${button.text}", "${it.id}:${button.data}")
                            }
                        })

                        logger.debug("Rendered keyboard: {}", keyboard)

                        bot.editMessageReplyMarkup(
                                message = callbackQuery.message,
                                replyMarkup = keyboard.toInlineKeyboardMarkup()
                        )
                    }
                } else {
                    logger.debug("Unknown callback query. Skipping")
                }
            }

        }
    }
}
