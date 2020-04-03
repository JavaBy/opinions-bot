package by.jprof.telegram.opinions.processors

import by.dev.madhead.telek.model.Update
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.apache.logging.log4j.LogManager

class UpdateProcessingPipeline(
        private val processors: List<UpdateProcessor>
) {
    companion object {
        val logger = LogManager.getLogger(UpdateProcessingPipeline::class.java)!!
    }

    fun process(update: Update) = runBlocking {
        supervisorScope {
            processors
                    .map { launch(exceptionHandler(it)) { it.process(update) } }
                    .joinAll()
        }
    }

    private fun exceptionHandler(updateProcessor: UpdateProcessor) = CoroutineExceptionHandler { _, exception ->
        logger.error("{} failed!", updateProcessor::class.simpleName, exception)
    }
}
