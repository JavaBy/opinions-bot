package by.jprof.telegram.opinions.config

import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.telegramBot
import com.github.insanusmokrassar.TelegramBotAPI.utils.TelegramAPIUrlsKeeper
import org.koin.core.qualifier.named
import org.koin.dsl.module

val telegramModule = module {
    single {
        TelegramAPIUrlsKeeper(get(named(TELEGRAM_BOT_TOKEN)))
    }
    single {
        telegramBot(get<TelegramAPIUrlsKeeper>())
    }
}
