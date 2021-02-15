package by.jprof.telegram.opinions.webhook.processors

import by.jprof.telegram.opinions.webhook.entity.DOWN_VOTE
import by.jprof.telegram.opinions.webhook.entity.UP_VOTE
import by.jprof.telegram.opinions.webhook.entity.Votes
import by.jprof.telegram.opinions.webhook.entity.downVotes
import by.jprof.telegram.opinions.webhook.entity.upVotes
import com.github.insanusmokrassar.TelegramBotAPI.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton

fun votingKeyBoard(votes: Votes, votesId: String) = listOf(
        listOf(
                CallbackDataInlineKeyboardButton(
                        text = "${votes.upVotes} $UP_VOTE",
                        callbackData = "$votesId:+"),
                CallbackDataInlineKeyboardButton(
                        text = "${votes.downVotes} $DOWN_VOTE",
                        callbackData = "$votesId:-")
        )
)
