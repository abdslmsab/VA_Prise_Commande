package com.example.va_prisecommande.singleton

import Client
import com.example.va_prisecommande.ftp.FtpDownloadTask
import com.example.va_prisecommande.model.Article
import com.example.va_prisecommande.model.Commercial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object DataRepository {
    var commerciaux: List<Commercial>? = null
    var clients: List<Client>? = null
    var articles: List<Article>? = null
    var commerciauxXml: String? = null
    var clientsXml: String? = null
    var articlesXml: String? = null

    /*
        private lateinit var database: AppDatabase

    fun initializeDatabase(context: Context) {
        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "app-database"
        ).build()
    }

    suspend fun getAllCommerciaux(): List<Commercial> {
        return withContext(Dispatchers.IO) {
            database.commercialDao().getAll()
        }
    }

    suspend fun updateCommerciaux(commerciaux: List<Commercial>) {
        withContext(Dispatchers.IO) {
            database.commercialDao().insertAll(commerciaux)
        }
    }

     */

    suspend fun loadAllData() {
        commerciauxXml = downloadXml("/commerciaux.xml")
        commerciaux = parseXmlToCommerciaux(commerciauxXml ?: "")

        clientsXml = downloadXml("/clients.xml")
        clients = parseXmlToClients(clientsXml ?: "")

        articlesXml = downloadXml("/articles.xml")
        articles = parseXmlToArticles(articlesXml ?: "")
    }

    suspend fun downloadXml(path: String): String {
        return withContext(Dispatchers.IO) {
            FtpDownloadTask().downloadXmlFile("141.94.170.53", "ftpVital", "Kz5Jkud6GG", path)
        }
    }

    fun parseXmlToCommerciaux(xml: String): List<Commercial> {
        val cleanXml = xml.trim().removePrefix("\uFEFF")

        val commerciaux = mutableListOf<Commercial>()
        val xmlPullParserFactory = XmlPullParserFactory.newInstance()
        val xmlPullParser = xmlPullParserFactory.newPullParser()
        xmlPullParser.setInput(StringReader(cleanXml))

        var eventType = xmlPullParser.eventType
        var currentCommercial: Commercial? = null
        var currentTag: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (xmlPullParser.name == "row") {
                        currentCommercial = Commercial("", "")
                    }
                    currentTag = xmlPullParser.name
                }
                XmlPullParser.TEXT -> {
                    val text = xmlPullParser.text
                    currentCommercial?.let {
                        when (currentTag) {
                            "Nom" -> it.nom = text
                            "Prenom" -> it.prenom = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (xmlPullParser.name == "row" && currentCommercial != null) {
                        commerciaux.add(currentCommercial)
                        currentCommercial = null
                    }
                    currentTag = null
                }
            }
            eventType = xmlPullParser.next()
        }
        return commerciaux
    }

    fun parseXmlToClients(xml: String): List<Client> {
        val cleanXml = xml.trim().removePrefix("\uFEFF")

        val clients = mutableListOf<Client>()
        val xmlPullParserFactory = XmlPullParserFactory.newInstance()
        val xmlPullParser = xmlPullParserFactory.newPullParser()
        xmlPullParser.setInput(StringReader(cleanXml))

        var eventType = xmlPullParser.eventType
        var currentClient: Client? = null
        var currentTag: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (xmlPullParser.name == "row") {
                        currentClient = Client("", "", "", "", "")
                    }
                    currentTag = xmlPullParser.name
                }

                XmlPullParser.TEXT -> {
                    val text = xmlPullParser.text
                    currentClient?.let {
                        when (currentTag) {
                            "Code" -> it.code = text
                            "Nom" -> it.nom = text
                            "Adresse" -> it.adresse = text
                            "Code_Postal" -> it.code_postal = text
                            "Ville" -> it.ville = text
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (xmlPullParser.name == "row" && currentClient != null) {
                        clients.add(currentClient)
                        currentClient = null
                    }
                    currentTag = null
                }
            }
            eventType = xmlPullParser.next()
        }
        return clients
    }

    fun parseXmlToArticles(xml: String): List<Article> {
        val cleanXml = xml.trim().removePrefix("\uFEFF")

        val articlesMap = mutableMapOf<String, Article>()
        val xmlPullParserFactory = XmlPullParserFactory.newInstance()
        val xmlPullParser = xmlPullParserFactory.newPullParser()
        xmlPullParser.setInput(StringReader(cleanXml))

        var eventType = xmlPullParser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && xmlPullParser.name == "row") {
                var ean = ""
                var code = ""
                var nom = ""
                var conditionnement: String? = null
                var principal = false

                while (xmlPullParser.nextTag() == XmlPullParser.START_TAG) {
                    val tagName = xmlPullParser.name
                    when (tagName) {
                        "EAN" -> ean = xmlPullParser.nextText()
                        "Code" -> code = xmlPullParser.nextText()
                        "Nom" -> nom = xmlPullParser.nextText()
                        "Conditionnement" -> conditionnement = xmlPullParser.nextText()
                        "Principal" -> principal = xmlPullParser.nextText().toInt() == 1
                    }
                }

                val article = articlesMap[ean]
                if (article == null) {
                    val newArticle = Article(ean, code, nom, mutableListOf(conditionnement ?: ""), conditionnement.takeIf { principal })
                    articlesMap[ean] = newArticle
                } else {
                    if (conditionnement != null && !article.conditionnements.contains(conditionnement)) {
                        article.conditionnements.add(conditionnement)
                        if (principal) {
                            article.conditionnementDefaut = conditionnement
                        }
                    }
                }
            }
            eventType = xmlPullParser.next()
        }

        return articlesMap.values.toList()
    }
}