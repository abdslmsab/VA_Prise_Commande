package com.example.va_prisecommande.ftp

import android.util.Log
import org.apache.commons.net.ftp.FTPClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Duration

class FtpDownloadTask {

    fun downloadXmlFile(host: String, username: String, password: String, filePath: String): String {
        val ftpClient = FTPClient()

        ftpClient.setUseEPSVwithIPv4(true)

        try {
            // Augmentation du délai d'attente du client FTP car s'il a un délai d'attente (timeout) court, il se peut qu'il expire avant que la connexion ne soit établie ou que les données soient transférée
            ftpClient.connectTimeout = 10000 // 10 secondes
            ftpClient.dataTimeout = Duration.ofSeconds(30) // 30 secondes

            ftpClient.connect(host)

            // Le mode passif FTP est souvent nécessaire pour traverser les NAT et les pare-feu, surtout dans les réseaux mobiles
            ftpClient.enterLocalPassiveMode()

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
