package by.jprof.telegram.opinions.webhook.config

import by.jprof.telegram.opinions.webhook.processors.CustomVotesProcessor
import by.jprof.telegram.opinions.webhook.processors.JEPLinksProcessor
import by.jprof.telegram.opinions.webhook.processors.KotlinMentionsProcessor
import by.jprof.telegram.opinions.webhook.processors.KotlinStatsCommandProcessor
import by.jprof.telegram.opinions.webhook.processors.UpdateProcessingPipeline
import by.jprof.telegram.opinions.webhook.processors.UpdateProcessor
import by.jprof.telegram.opinions.webhook.processors.YoutubeLinksProcessor
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
        YoutubeLinksProcessor(get())
    }

    single<UpdateProcessor>(named("KotlinMentionsProcessor")) {
        KotlinMentionsProcessor(
                bot = get(),
                kotlinMentionsDAO = get(),
        )
    }

    single<UpdateProcessor>(named("KotlinStatsCommandProcessor")) {
        KotlinStatsCommandProcessor(get(), get())
    }

    single<UpdateProcessor>(named("CustomVotesProcessor")) {
        CustomVotesProcessor(get(), get(), get())
    }
}
