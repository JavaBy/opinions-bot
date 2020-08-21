package by.jprof.telegram.opinions.processors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileOutputStream


class Image2textKtTest {
    @TempDir
    lateinit var tessdata: File

    @BeforeEach
    fun setUp() {
        this::class.java.classLoader.getResource("tessdata/rus.traineddata")?.openStream()?.use {
            it.transferTo(FileOutputStream(File(tessdata, "rus.traineddata")))
        }
        this::class.java.classLoader.getResource("ocr/1.jpg")?.openStream()?.use {
            it.transferTo(FileOutputStream(File(tessdata, "1.jpg")))
        }
    }

    @Test
    fun `test image to text`() = assertEquals(
            """
                Привет, я подсяду? Спасибо.
                
                Почему у меня на маке наклейка с котлином? Ну,
                просто мне понравились цвета...
                
                Поддерживаю ли я синтаксический сахар? - Да.
                Да, я являюсь контрибьютером гитхаба
                "джетбреинс", а почему ты спрашиваешь?
                
                В смысле "нарушаю корпоративные кодстайлы"?
                Кодстайлы писались под жаву в 2002, они сами
                виноваты. Ладно...
                
                Хочу ли я шеймить джавистов? Боже, нет конечно,
                а почему я должен хотеть шеймить их?
                
                В смысле, "всех"? Нет, постой это немножко не так
                работает, тебе объяснить?
                
                Не надо подходить к коллегам и нажимать
                айн+с!+зМ!+К? Я не нажимаю, они сами спросили,
                что это за комбинация...
                
                Ясно, я котлинодрочер... Угу... Как и все.
                
                Ладно, извини что потревожил... Пойду писать
                экстеншены.
                """.trimIndent(),
            image2text(File(tessdata, "1.jpg"), tessdata, Lang.RUS).trim()
    )
}