package by.jprof.telegram.opinions.publication.config

import by.jprof.telegram.opinions.publication.Publisher
import by.jprof.telegram.opinions.publication.TelegramPublisher
import org.koin.dsl.bind
import org.koin.dsl.module

val publicationBeansModule = module {
    single<Publisher> {
        TelegramPublisher(
            get(),
            get(),
            get()
        )
    } bind Publisher::class
}