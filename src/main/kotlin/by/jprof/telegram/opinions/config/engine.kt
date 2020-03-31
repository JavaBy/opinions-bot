package by.jprof.telegram.opinions.config

import by.jprof.telegram.opinions.processors.JEPLinksProcessor
import by.jprof.telegram.opinions.processors.UpdateProcessingEngine
import by.jprof.telegram.opinions.processors.UpdateProcessor
import org.koin.dsl.module

val engineModule = module {
    single {
        UpdateProcessingEngine()
    }

    single<UpdateProcessor> {
        JEPLinksProcessor(get())
    }
}
