package by.jprof.telegram.opinions.webhook.processors

import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update

interface UpdateProcessor {
    suspend fun process(update: Update)
}
