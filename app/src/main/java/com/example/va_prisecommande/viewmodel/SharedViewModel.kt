package com.example.va_prisecommande.viewmodel

import Client
import DocumentType
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.va_prisecommande.model.Article
import com.example.va_prisecommande.model.ArticlePourPanier
import com.example.va_prisecommande.model.Commercial
import com.example.va_prisecommande.singleton.DataRepository
import com.example.va_prisecommande.utils.NumeroBonManager
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.Style
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.HorizontalAlignment
import com.itextpdf.layout.property.TextAlignment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


class SharedViewModel : ViewModel() {
    private var dateBonFormat = SimpleDateFormat("yy", Locale.getDefault())

    val selectedCommercial = MutableLiveData<Commercial?>()

    val selectedClient = MutableLiveData<Client?>()

    fun selectCommercial(commercial: Commercial) {
        selectedCommercial.value = commercial
    }

    fun selectClient(client: Client) {
        selectedClient.value = client
    }

    // Création d'un MutableStateFlow pour stocker l'information eanSaisi
    private val _eanSaisi = MutableStateFlow("")

    // Rend la variable eanSaisi accessible depuis l'extérieur de cette classe,
    // mais permet uniquement de la lire (pas de la changer directement)
    val eanSaisi = _eanSaisi.asStateFlow()

    // Fonction pour mettre à jour la valeur de eanSaisi
    fun changementEan(texte: String) {
        _eanSaisi.value = texte
    }

    // Utilisation de la fonction map pour transformer la valeur de eanSaisi
    val estEanValide = eanSaisi.map { ean ->
        // Vérification si la longueur de la chaîne ean est égale à 13
        ean.length == 13
    }

    private val _quantiteSaisie = MutableStateFlow("")
    val quantiteSaisie = _quantiteSaisie.asStateFlow()
    fun changementQuantite(texte: String){
        _quantiteSaisie.value = texte
    }
    val estQuantiteValide = quantiteSaisie.map { quantite ->
        quantite.isNotEmpty() && quantite.toIntOrNull() != null
    }

    private val _quantiteUVCSaisie = MutableStateFlow("")
    val quantiteUVCSaisie = _quantiteUVCSaisie.asStateFlow()
    fun changementQuantiteUVC(texte: String){
        _quantiteUVCSaisie.value = texte
    }
    val estQuantiteUVCValide = quantiteUVCSaisie.map { quantiteUVC ->
        quantiteUVC.isNotEmpty() && quantiteUVC.toIntOrNull() != null
    }

    private val _numeroLotSaisi = MutableStateFlow("")
    val numeroLotSaisi = _numeroLotSaisi.asStateFlow()
    fun changementNumeroLot(texte: String){
        _numeroLotSaisi.value = texte
    }
    val estNumeroLotValide = numeroLotSaisi.map { numeroLot ->
        if (numeroLot == null || numeroLot.length != 5) {
            false
        } else {
            val anneeLot = numeroLot.substring(0, 2)
            val jourDansLAnnee = Integer.parseInt(numeroLot.substring(2))
            val anneeActuelle = Calendar.getInstance().get(Calendar.YEAR) % 100
            val jourDansLAnneeLimiteInf = 1
            val jourDansLAnneeLimiteSup = if (anneeActuelle % 4 == 0) 366 else 365

            anneeLot == anneeActuelle.toString() && jourDansLAnnee in jourDansLAnneeLimiteInf..jourDansLAnneeLimiteSup
        }
    }

    private val _ddmSaisie = MutableStateFlow("")
    val ddmSaisie = _ddmSaisie.asStateFlow()
    fun changementDdm(texte: String) {
        _ddmSaisie.value = texte
    }

    val estDdmValide = ddmSaisie.map { ddm ->
        try {
            val tokens = ddm.split("/")
            if (tokens.size >= 3) {
                val jour = tokens[0].toInt()
                val mois = tokens[1].toInt()
                val annee = tokens[2].toInt()

                if (jour < 1 || jour > 31) {
                    return@map false
                }
                if (mois < 1 || mois > 12) {
                    return@map false
                }
                if (annee < 0 || annee > 99) {
                    return@map false
                }
                true
            } else {
                false
            }
        } catch (e: NumberFormatException) {
            false
        }
    }

    private val _pvcSaisi = MutableStateFlow("")
    val pvcSaisi = _pvcSaisi.asStateFlow()
    fun changementPvc(texte: String) {
        _pvcSaisi.value = texte
    }

    private val _articlesDansLePanier = MutableLiveData<MutableList<ArticlePourPanier>>(mutableListOf())
    val articlesDansLePanier: LiveData<MutableList<ArticlePourPanier>> = _articlesDansLePanier

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> = _articles

    fun ajouterAuPanier(ean: String, code:String, conditionnement: String?, quantite: Int?, quantiteUvc: Int?, numLot: Int?, ddm: String?, pvc: Float?) {
        val article = _articles.value?.firstOrNull { it.ean == ean }

        if (article != null) {
            val nouvelArticlePourPanier = ArticlePourPanier(
                ean = ean,
                nom = article.nom,
                conditionnement = conditionnement,
                quantite = quantite,
                quantiteUvc = quantiteUvc,
                numLot = numLot,
                ddm = ddm,
                pvc = pvc,
                code = code
            )

            // Ajout de l'article au panier et notification aux observers
            val currentList = _articlesDansLePanier.value ?: mutableListOf()
            currentList.add(nouvelArticlePourPanier)
            _articlesDansLePanier.value = currentList
        }
    }


    fun retirerDuPanier(article: ArticlePourPanier) {
        val currentList = _articlesDansLePanier.value ?: mutableListOf()
        currentList.remove(article)
        _articlesDansLePanier.value = currentList
    }

    fun viderToutLePanier() {
        _articlesDansLePanier.value = mutableListOf()
    }

    fun chargerArticles() {
        viewModelScope.launch {
            if (DataRepository.articles == null) {
                DataRepository.loadAllData()
            }
            _articles.value = DataRepository.articles
        }
    }

    private val _dateLivraisonSaisie = MutableStateFlow("")
    val dateLivraisonSaisie = _dateLivraisonSaisie.asStateFlow()
    fun changementDateLivraison(texte: String){
        _dateLivraisonSaisie.value = texte
    }
    val estDateLivraisonValide = dateLivraisonSaisie.map { ddm ->
        try {
            val tokens = ddm.split("/")
            if (tokens.size >= 3) {
                val jour = tokens[0].toInt()
                val mois = tokens[1].toInt()
                val annee = tokens[2].toInt()

                if (jour < 1 || jour > 31) {
                    return@map false
                }
                if (mois < 1 || mois > 12) {
                    return@map false
                }
                if (annee < 0 || annee > 99) {
                    return@map false
                }
                true
            } else {
                false
            }
        } catch (e: NumberFormatException) {
            false
        }
    }

    private val _dateRetourSaisie = MutableStateFlow("")
    val dateRetourSaisie = _dateRetourSaisie.asStateFlow()
    fun changementDateRetour(texte: String){
        _dateRetourSaisie.value = texte
    }
    val estDateRetourValide = dateRetourSaisie.map { ddm ->
        try {
            val tokens = ddm.split("/")
            if (tokens.size >= 3) {
                val jour = tokens[0].toInt()
                val mois = tokens[1].toInt()
                val annee = tokens[2].toInt()

                if (jour < 1 || jour > 31) {
                    return@map false
                }
                if (mois < 1 || mois > 12) {
                    return@map false
                }
                if (annee < 0 || annee > 99) {
                    return@map false
                }
                true
            } else {
                false
            }
        } catch (e: NumberFormatException) {
            false
        }
    }

    private val _plvSaisi = MutableStateFlow("")
    val plvSaisi = _plvSaisi.asStateFlow()
    fun changementPlv(texte: String){
        _plvSaisi.value = texte
    }

    private val _commentaireSaisi = MutableStateFlow("")
    val commentaireSaisi = _commentaireSaisi.asStateFlow()
    fun changementCommentaire(texte: String){
        _commentaireSaisi.value = texte
    }

    fun genererNumeroBon(): String {
        val numeroBon: String

        val heureFormat = SimpleDateFormat("HHmm")
        val today = LocalDate.now()
        val dayOfYear = today.format(DateTimeFormatter.ofPattern("DDD"))

        numeroBon = "${dateBonFormat.format(Date())}$dayOfYear${heureFormat.format(Date())}"

        return numeroBon
    }
}