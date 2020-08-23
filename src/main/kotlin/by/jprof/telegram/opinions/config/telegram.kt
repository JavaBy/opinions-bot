package by.jprof.telegram.opinions.config

import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.telegramBot
import com.github.insanusmokrassar.TelegramBotAPI.utils.TelegramAPIUrlsKeeper
import kotlinx.serialization.ImplicitReflectionSerializer
import org.koin.core.qualifier.named
import org.koin.dsl.module

@ImplicitReflectionSerializer
val telegramModule = module {
    single {
        TelegramAPIUrlsKeeper(get(named(TELEGRAM_BOT_TOKEN)))
    }
    single {
        telegramBot(get<TelegramAPIUrlsKeeper>())
    }
}
