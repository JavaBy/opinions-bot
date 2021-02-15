package by.jprof.telegram.opinions.webhook.processors

import by.jprof.telegram.opinions.webhook.dao.asText
import com.github.insanusmokrassar.TelegramBotAPI.types.MessageEntity.textsources.BotCommandTextSource
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.fullEntitiesList
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update

abstract class CommandProcessor(
        private val cmd: String
) : UpdateProcessor {
    override suspend fun process(update: Update) {
        if (isCommand(cmd, update)) {
            doProcess(update)
        }
    }

    abstract suspend fun doProcess(update: Update)
}

fun isCommand(cmd: String, update: Update): Boolean {
    val text = update.asText() ?: return false
    return text.fullEntitiesList().any { it is BotCommandTextSource && it.command == cmd }
}