package com.example.va_prisecommande.ftp

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.FileInputStream
import java.time.Duration

class FtpSendFileTask {
    fun uploadTextFile(host: String, username: String, password: String, localFilePath: String, remoteFilePath: String): Boolean {
        val ftpClient = FTPClient()

        ftpClient.setUseEPSVwithIPv4(true)

        try {
            // Augmenter les délais d'attente pour éviter les expirations prématurées
            ftpClient.connectTimeout = 10000 // 10 secondes
            ftpClient.dataTimeout = Duration.ofSeconds(30) // 30 secondes

            ftpClient.connect(host)

            // Utilisation du mode passif pour mieux fonctionner à travers les NAT et pare-feux
            ftpClient.enterLocalPassiveMode()

            if (!ftpClient.login(username, password)) {
                return false
            }

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)

            FileInputStream(File(localFilePath)).use { input ->
                val result = ftpClient.storeFile(remoteFilePath, input)
                ftpClient.logout()
                ftpClient.disconnect()
                return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}