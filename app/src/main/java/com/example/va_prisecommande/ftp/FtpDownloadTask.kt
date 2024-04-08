package com.example.va_prisecommande.ftp

import android.util.Log
import org.apache.commons.net.ftp.FTPClient
import java.io.BufferedReader
import java.io.InputStreamReader

class FtpDownloadTask {

    fun downloadXmlFile(host: String, username: String, password: String, filePath: String): String {
        val ftpClient = FTPClient()

        ftpClient.setUseEPSVwithIPv4(true)

        try {
            ftpClient.connect(host)

            if (!ftpClient.login(username, password)) {
                return ""
            }

            val inputStream = ftpClient.retrieveFileStream(filePath) ?: return ""

            val reader = BufferedReader(InputStreamReader(inputStream))
            val data = reader.readText()

            reader.close()
            ftpClient.logout()
            ftpClient.disconnect()

            return data
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
