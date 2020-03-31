package by.jprof.telegram.opinions.processors

import by.dev.madhead.telek.model.InlineKeyboardButton
import by.dev.madhead.telek.model.InlineKeyboardMarkup
import by.dev.madhead.telek.model.Update
import by.dev.madhead.telek.model.communication.ChatId
import by.dev.madhead.telek.model.communication.SendMessageRequest
import by.dev.madhead.telek.telek.Telek
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class JEPLinksProcessor(
        private val telek: Telek
) : UpdateProcessor {
    companion object {
        val logger = LogManager.getLogger(UpdateProcessingEngine::class.java)
        val siteRegex = "https?://openjdk\\.java\\.net/jeps/(\\d+)/?".toRegex()
    }

    override fun process(update: Update) = runBlocking {
        update.message?.let { message ->
            message.entities?.let { entities ->
                entities
                        .filter { (it.type == "text_link") || (it.type == "url") }
                        .mapNotNull {
                            when (it.type) {
                                "text_link" -> it.url
                                "url" -> message.text?.substring(it.offset, it.offset + it.length)
                                else -> null
                            }
                        }
                        .mapNotNull {
                            siteRegex.matchEntire(it)
                        }
                        .forEach { matchResult ->
                            val (jep) = matchResult.destructured

                            telek.sendMessage(
                                    SendMessageRequest(
                                            chatId = ChatId.of(message.chat.id),
                                            text = "JEP $jep link detected! Let's vote!",
                                            replyToMessageId = message.messageId,
                                            replyMarkup = InlineKeyboardMarkup(
                                                    inlineKeyboard = listOf(
                                                            listOf(
                                                                    InlineKeyboardButton(text = "\uD83D\uDC4D", callbackData = "\uD83D\uDC4D"),
                                                                    InlineKeyboardButton(text = "\uD83D\uDC4E", callbackData = "\uD83D\uDC4D")
                                                            )
                                                    )
                                            )
                                    )
                            )
                        }
            }
        }

        Unit
    }
}
