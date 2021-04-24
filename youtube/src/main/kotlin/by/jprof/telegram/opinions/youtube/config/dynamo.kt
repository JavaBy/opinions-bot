package by.jprof.telegram.opinions.youtube.config

import by.jprof.telegram.opinions.youtube.YoutubeDAO
import org.koin.dsl.module

val youtubeDynamoModule = module {
    single {
        YoutubeDAO(get(), get())
    }
}