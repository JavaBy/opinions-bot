package by.jprof.telegram.opinions.processors

import org.bytedeco.javacpp.lept
import org.bytedeco.javacpp.tesseract
import java.io.File
import java.nio.charset.StandardCharsets

enum class Lang(val label: String) {
    RUS("rus"), ENG("eng")
}

fun image2text(img: File, tessdata: File, lang: Lang): String = tesseract.TessBaseAPI().use { api ->
    tesseract.TessBaseAPIInit2(api, tessdata.toString(), lang.label, tesseract.OEM_DEFAULT)
    lept.pixRead(img.absolutePath).use {
        tesseract.TessBaseAPISetImage2(api, it)
        tesseract.TessBaseAPIRecognize(api, null)
        tesseract.TessBaseAPIGetIterator(api).use { resultIter ->
            tesseract.TessResultIteratorGetUTF8Text(resultIter, tesseract.RIL_BLOCK).use { bytes ->
                String(bytes.stringBytes, StandardCharsets.UTF_8)
            }
        }
    }
}