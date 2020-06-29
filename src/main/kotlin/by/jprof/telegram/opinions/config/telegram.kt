package by.jprof.telegram.opinions.config

import by.jprof.telegram.opinions.commands.*
import com.github.insanusmokrassar.TelegramBotAPI.extensions.api.telegramBot
import kotlinx.serialization.ImplicitReflectionSerializer
import org.koin.core.qualifier.named
import org.koin.dsl.module

@ImplicitReflectionSerializer
val telegramModule = module {

    single {
        telegramBot(get(named(TELEGRAM_BOT_TOKEN)))
    }


    single<BotCommand>(named(ADD_TO_WHITE_LIST)) {
        AddYoutubeChannelBotCommand(get(), get(), get())
    }

    single<BotCommand>(named(REMOVE_FROM_WHITE_LIST)) {
        RemoveYoutubeChannelBotCommand(get(), get(), get())
    }

    single {
        mapOf<String, BotCommand>(
                ADD_TO_WHITE_LIST to get(named(ADD_TO_WHITE_LIST)),
                REMOVE_FROM_WHITE_LIST to get(named(REMOVE_FROM_WHITE_LIST))
        )
    }

}
