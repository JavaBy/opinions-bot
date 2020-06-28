package by.jprof.telegram.opinions.config

import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.telegramBot
import kotlinx.serialization.ImplicitReflectionSerializer
import org.koin.core.qualifier.named
import org.koin.dsl.module

@ImplicitReflectionSerializer
val telegramModule = module {

    single {
        telegramBot(get(named(TELEGRAM_BOT_TOKEN)))
    }

}
