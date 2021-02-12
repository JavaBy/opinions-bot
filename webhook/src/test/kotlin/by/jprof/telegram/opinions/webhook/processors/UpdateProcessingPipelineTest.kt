package by.jprof.telegram.opinions.webhook.processors

import com.github.insanusmokrassar.TelegramBotAPI.types.UpdateIdentifier
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

data class MockUpdate(override val data: Any, override val updateId: UpdateIdentifier) : Update

class UpdateProcessingPipelineTest {
    @Test
    fun process() {
        val testProcessingDelay = 1000L
        val testProcessors = 3
        val pipeline = UpdateProcessingPipeline(
                (1..testProcessors)
                        .map {
                            object : UpdateProcessor {
                                override suspend fun process(update: Update) {
                                    delay(testProcessingDelay)
                                }
                            }
                        }
                        .toList())

        Assertions.assertTrue(
                measureTimeMillis {
                    pipeline.process(MockUpdate(1, 1))
                } < testProcessors * testProcessingDelay
        )
    }

    @Test
    fun processWithException() {
        val states = mutableListOf(false, false, false)
        val pipeline = UpdateProcessingPipeline(
                listOf(
                        object : UpdateProcessor {
                            override suspend fun process(update: Update) {
                                states[0] = true
                                throw IllegalArgumentException("Test exception 1")
                            }
                        },
                        object : UpdateProcessor {
                            override suspend fun process(update: Update) {
                                delay(500)
                                states[1] = true
                            }
                        },
                        object : UpdateProcessor {
                            override suspend fun process(update: Update) {
                                delay(1000)
                                states[2] = true
                                throw IllegalArgumentException("Test exception 3")
                            }
                        }
                )
        )

        pipeline.process(MockUpdate(1, 1))

        Assertions.assertTrue(states.all { it })
    }
}
