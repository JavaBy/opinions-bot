package by.jprof.telegram.opinions.config

import by.dev.madhead.telek.telek.Telek
import by.dev.madhead.telek.telek.hc.TelekHC
import kotlinx.serialization.ImplicitReflectionSerializer
import org.koin.core.qualifier.named
import org.koin.dsl.module

@ImplicitReflectionSerializer
val telegramModule = module {
    single<Telek> {
        TelekHC(get(named(TELEGRAM_BOT_TOKEN)))
    }
}
