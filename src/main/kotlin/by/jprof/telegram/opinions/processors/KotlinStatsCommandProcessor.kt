package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.dao.KotlinMentionsDAO
import com.github.insanusmokrassar.TelegramBotAPI.bot.RequestsExecutor
import com.github.insanusmokrassar.TelegramBotAPI.types.message.CommonMessageImpl
import com.github.insanusmokrassar.TelegramBotAPI.types.update.MessageUpdate
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update

class KotlinStatsCommandProcessor(
        private val bot: RequestsExecutor,
        private val kotlinMentionsDAO: KotlinMentionsDAO
) : CommandProcessor("kotlin-stats") {
    override suspend fun doProcess(update: Update) {
        val message = (update as? MessageUpdate) ?: return
        val content = (message.data as? CommonMessageImpl<*>) ?: return
        val chatId = content.chat.id.chatId
        val mention = kotlinMentionsDAO.find(chatId)
        // TODO: generate nice table to show top N kotlin fans
    }
}