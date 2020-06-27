package by.jprof.telegram.opinions.processors

import by.jprof.telegram.opinions.model.Update


interface UpdateProcessor {
    suspend fun process(update: Update)
}
