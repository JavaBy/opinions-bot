package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.asText
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
    return (text.text.startsWith("/") &&
            text.text.drop(1).split(" ")[0].split("@")[0] == cmd)
}