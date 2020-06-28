package by.jprof.telegram.opinions.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.koin.dsl.module

val jsonModule = module {
    single {
        Json(configuration = JsonConfiguration.Stable.copy(encodeDefaults = false, ignoreUnknownKeys = true))
    }
}
