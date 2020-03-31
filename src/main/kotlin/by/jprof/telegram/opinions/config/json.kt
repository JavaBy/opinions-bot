package by.jprof.telegram.opinions.config

import by.dev.madhead.telek.model.ForceReply
import by.dev.madhead.telek.model.InlineKeyboardMarkup
import by.dev.madhead.telek.model.ReplyKeyboardMarkup
import by.dev.madhead.telek.model.ReplyKeyboardRemove
import by.dev.madhead.telek.model.ReplyMarkup
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import org.koin.dsl.module

val jsonModule = module {
    single {
        Json(
                configuration = JsonConfiguration.Stable.copy(encodeDefaults = false, ignoreUnknownKeys = true),
                context = SerializersModule {
                    polymorphic(ReplyMarkup::class) {
                        ForceReply::class with ForceReply.serializer()
                        InlineKeyboardMarkup::class with InlineKeyboardMarkup.serializer()
                        ReplyKeyboardMarkup::class with ReplyKeyboardMarkup.serializer()
                        ReplyKeyboardRemove::class with ReplyKeyboardRemove.serializer()
                    }
                }
        )
    }
}
