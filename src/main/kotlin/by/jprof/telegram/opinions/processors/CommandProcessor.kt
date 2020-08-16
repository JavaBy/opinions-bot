package by.jprof.telegram.opinions.processors

import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.message.content.TextContent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update

abstract class CommandProcessor(
        private val cmd: String
) : UpdateProcessor {
    override suspend fun process(update: Update) {
        if (isCommand(update)) {
            doProcess(update)
        }
    }

    abstract suspend fun doProcess(update: Update)

    protected fun isCommand(update: Update): Boolean {
        val message = (update as? MessageUpdate) ?: return false
        val content = (message.data as? CommonMessageImpl<*>) ?: return false
        val text = (content.content as? TextContent) ?: return false

        return (text.text.startsWith("/") && text.text.drop(1).split(" ")[0].split("@")[0] == cmd)
    }
}