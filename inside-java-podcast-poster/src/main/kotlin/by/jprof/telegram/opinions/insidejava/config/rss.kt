package by.jprof.telegram.opinions.insidejava.config

import org.koin.dsl.module
import tw.ktrssreader.kotlin.parser.ITunesParser

val rssModule = module {
    single {
        ITunesParser()
    }
}
