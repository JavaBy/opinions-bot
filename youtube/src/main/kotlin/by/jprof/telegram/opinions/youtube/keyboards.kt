package by.jprof.telegram.opinions.youtube

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
