package by.jprof.telegram.opinions.processors

import by.dev.madhead.telek.model.Update
import by.jprof.telegram.opinions.aux.injectAll
import org.apache.logging.log4j.LogManager
import org.koin.core.KoinComponent

class UpdateProcessingEngine : KoinComponent {
    companion object {
        val logger = LogManager.getLogger(UpdateProcessingEngine::class.java)
    }

    private val processors: List<UpdateProcessor> by injectAll()

    fun process(update: Update) {
        processors.forEach { processor ->
            try {
                processor.process(update)
            } catch (e: Exception) {
                logger.error("{} failed!", processor::class.simpleName, e)
            }
        }
    }
}
