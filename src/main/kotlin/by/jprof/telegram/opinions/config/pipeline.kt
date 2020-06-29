package by.jprof.telegram.opinions.config

import by.jprof.telegram.opinions.processors.*
import org.koin.core.qualifier.named
import org.koin.dsl.module

val pipelineModule = module {
    single {
        UpdateProcessingPipeline(getAll())
    }

    single<UpdateProcessor>(named("JEPLinksProcessor")) {
        JEPLinksProcessor(get(), get())
    }

    single<UpdateProcessor>(named("YoutubeLinksProcessor")) {
        YoutubeLinksProcessor(get(), get(), get(), get())
    }

    single<UpdateProcessor>(named("WhitelistCommandProcessor")) {
        WhitelistCommandProcessor(get(), get())
    }

}
