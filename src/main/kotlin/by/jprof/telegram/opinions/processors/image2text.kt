package by.jprof.telegram.opinions.processors

import org.bytedeco.javacpp.lept
import org.bytedeco.javacpp.tesseract
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

enum class Lang(val label: String) {
    RUS("rus"), ENG("eng")
}

class Image2TextConverter(private val tessdata: File) : AutoCloseable {
    var apis = mutableMapOf<Lang, tesseract.TessBaseAPI>()

    init {
        check()
    }

    fun ocr(img: File, lang: Lang): String {
        check()
        val api = resolveApi(lang)
        return lept.pixRead(img.absolutePath).use {
            tesseract.TessBaseAPISetImage2(api, it)
            tesseract.TessBaseAPIRecognize(api, null)
            tesseract.TessBaseAPIGetIterator(api).use { resultIter ->
                tesseract.TessResultIteratorGetUTF8Text(resultIter, tesseract.RIL_BLOCK).use { bytes ->
                    String(bytes.stringBytes, StandardCharsets.UTF_8)
                }
            }
        }
    }

    private fun resolveApi(lang: Lang) = apis.getOrPut(lang) {
        val api = tesseract.TessBaseAPI()
        tesseract.TessBaseAPIInit2(api, tessdata.toString(), lang.label, tesseract.OEM_DEFAULT)
        api
    }

    private fun check() {
        if (!tessdata.isDirectory()) {
            throw IOException("'tessdata' exists but is not a directory");
        }
        Lang.values().forEach {
            if (!File(tessdata, "${it.label}.traineddata").isFile) {
                throw IOException("'{tessdata}/${it.label}.traineddata' not found");
            }
        }
    }

    override fun close() {
        apis.values.forEach { it.use { } }
    }
}