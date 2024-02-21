package com.example.va_prisecommande.viewmodel

import Client
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.va_prisecommande.model.Article
import com.example.va_prisecommande.model.ArticlePourPanier
import com.example.va_prisecommande.model.Commercial

class SharedViewModel : ViewModel() {
    val selectedCommercial = MutableLiveData<Commercial?>()

    val selectedClient = MutableLiveData<Client?>()

    private val _articlesDansLePanier = MutableLiveData<MutableList<ArticlePourPanier>>(mutableListOf())
    val articlesDansLePanier: LiveData<MutableList<ArticlePourPanier>> = _articlesDansLePanier

    private var _articles = listOf<Article>()
    val articles: List<Article>
        get() = _articles

    val message = MutableLiveData<String>()

    // Fonctions pour mettre à jour les données
    fun selectCommercial(commercial: Commercial) {
        selectedCommercial.value = commercial
    }

    fun selectClient(client: Client) {
        selectedClient.value = client
    }

    fun setArticles(articles: List<Article>) {
        _articles = articles
    }

    fun ajouterAuPanier(ean: String, conditionnement: String? = null, quantite: Int? = null, quantiteUvc: Int? = null, numLot: Int? = null, ddm: String? = null, pvc: Float? = null) {
        val article = _articles.firstOrNull { it.ean == ean }

        if (article != null) {
            val articlePourPanier = ArticlePourPanier(
                ean = ean,
                nom = article.nom,
                conditionnement = conditionnement,
                quantite = quantite,
                quantiteUvc = quantiteUvc,
                numLot = numLot,
                ddm = ddm,
                pvc = pvc
            )
            val currentList = _articlesDansLePanier.value ?: mutableListOf()
            currentList.add(articlePourPanier)
            _articlesDansLePanier.value = currentList
        } else {
            message.value = "Article non trouvé"
        }
    }
}