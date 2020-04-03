package by.jprof.telegram.opinions.processors

import by.dev.madhead.telek.model.Update

interface UpdateProcessor {
    suspend fun process(update: Update)
}
