package com.example.va_prisecommande

import com.example.va_prisecommande.ftp.FtpDownloadTask
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        println(FtpDownloadTask().downloadXmlFile(
            "141.94.170.53", "ftpVital", "Kz5Jkud6GG", "/commerciaux.xml"
        ))
    }
}