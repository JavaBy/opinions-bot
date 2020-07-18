package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.entity.DOWN_VOTE
import by.jprof.telegram.opinions.entity.UP_VOTE
import by.jprof.telegram.opinions.entity.Votes
import by.jprof.telegram.opinions.entity.downVotes
import by.jprof.telegram.opinions.entity.upVotes
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
