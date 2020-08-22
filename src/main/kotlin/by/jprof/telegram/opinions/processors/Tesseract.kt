package by.jprof.telegram.opinions.processors

import org.bytedeco.javacpp.lept
import org.bytedeco.javacpp.tesseract
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

enum class Lang(val label: String) {
    RUS("rus"), ENG("eng")
}

class Tesseract(private val tessdatasrc: String = "tessdata") {
    companion object {
        private fun copyTessdata(tessdatapath: String, dest: File) {
            Lang.values().forEach { lang ->
                this::class.java.classLoader.getResource("${tessdatapath}/${lang.label}.traineddata")?.openStream()?.use {
                    it.transferTo(FileOutputStream(File(dest, "${lang.label}.traineddata")))
                }
            }
        }
    }

    private lateinit var tessdata: File

    fun ocr(img: File, lang: Lang): String {
        ensureTessdata()
        return tesseract.TessBaseAPI().use { api ->
            tesseract.TessBaseAPIInit2(api, tessdata.toString(), lang.label, tesseract.OEM_DEFAULT)
            lept.pixRead(img.absolutePath).use {
                tesseract.TessBaseAPISetImage2(api, it)
                tesseract.TessBaseAPIRecognize(api, null/*monitor*/)
                tesseract.TessBaseAPIGetIterator(api).use { resultIter ->
                    tesseract.TessResultIteratorGetUTF8Text(resultIter, tesseract.RIL_BLOCK).use { bytes ->
                        String(bytes.stringBytes, StandardCharsets.UTF_8)
                    }
                }
            }
        }
    }

    private fun ensureTessdata() {
        if (!this::tessdata.isInitialized) {
            tessdata = Files.createTempDirectory("jprof-ocr").toFile()
            tessdata.deleteOnExit()
            copyTessdata(tessdatasrc, tessdata)
        }
        check()
    }

    private fun check() {
        if (!tessdata.isDirectory) {
            throw IOException("'tessdata' exists but is not a directory")
        }
        Lang.values().forEach {
            if (!File(tessdata, "${it.label}.traineddata").isFile) {
                throw IOException("'{tessdata}/${it.label}.traineddata' not found")
            }
        }
    }
}