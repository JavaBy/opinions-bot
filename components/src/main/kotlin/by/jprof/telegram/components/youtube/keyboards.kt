package by.jprof.telegram.components.youtube

import by.jprof.telegram.components.entity.DOWN_VOTE
import by.jprof.telegram.components.entity.UP_VOTE
import by.jprof.telegram.components.entity.Votes
import by.jprof.telegram.components.entity.downVotes
import by.jprof.telegram.components.entity.upVotes
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
