package com.example.va_prisecommande.fragments

import DocumentType
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.va_prisecommande.R
import com.example.va_prisecommande.adapter.BasketAdapter
import com.example.va_prisecommande.model.Article
import com.example.va_prisecommande.model.ArticlePourPanier
import com.example.va_prisecommande.singleton.DataRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class PanierFragment : Fragment() {
    private lateinit var basketAdapter: BasketAdapter
    private var articlesDansLePanier = mutableListOf<ArticlePourPanier>()
    private var articles: List<Article> = emptyList()
    private var documentType: DocumentType? = null

    companion object {
        fun newInstance(documentType: DocumentType) = PanierFragment().apply {
            arguments = Bundle().apply {
                putSerializable("documentType", documentType)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentType = arguments?.getSerializable("documentType") as DocumentType?
        basketAdapter = BasketAdapter(articlesDansLePanier, documentType!!)
        chargerArticles()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_panier_commande, container, false)
        adjustFieldsBasedOnDocumentType(view, documentType)
        val recyclerView = view.findViewById<RecyclerView>(R.id.vertical_recycler_view)

        basketAdapter = BasketAdapter(articlesDansLePanier, documentType!!)
        recyclerView.adapter = basketAdapter

        val eanInputLayout = view.findViewById<TextInputLayout>(R.id.ean_input)
        val eanEditText = eanInputLayout.editText as TextInputEditText

        val conditionnementSpinner = view.findViewById<Spinner>(R.id.packaging_input)

        val quantiteInputLayout = view.findViewById<TextInputLayout>(R.id.quantity_input)
        val quantiteEditText = quantiteInputLayout.editText as TextInputEditText

        val quantiteUvcInputLayout = view.findViewById<TextInputLayout>(R.id.quantity_uvc_input)
        val quantiteUvcEditText = quantiteUvcInputLayout.editText as TextInputEditText

        val numLotInputLayout = view.findViewById<TextInputLayout>(R.id.numerolot_input)
        val numLotEditText = numLotInputLayout.editText as TextInputEditText

        val ddmInputLayout = view.findViewById<TextInputLayout>(R.id.ddm_input)
        val ddmEditText = ddmInputLayout.editText as TextInputEditText

        val pvcInputLayout = view.findViewById<TextInputLayout>(R.id.pvc_input)
        val pvcEditText = pvcInputLayout.editText as TextInputEditText



        eanEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length == 4) {
                    val eanComplet = eanInputLayout.prefixText.toString() + s.toString()
                    chargerArticleEtConditionnements(eanComplet, conditionnementSpinner)
                }
            }

        })

        view.findViewById<Button>(R.id.add_button).setOnClickListener {
            val ean = eanInputLayout.prefixText.toString() + eanEditText.text.toString()
            val selectedItem = conditionnementSpinner.selectedItem
            val conditionnementSelected = selectedItem?.toString() ?: "Valeur par défaut"
            val quantite = quantiteEditText.text.toString().toIntOrNull() ?: 0

            val quantiteUvc = quantiteUvcEditText.text.toString().toIntOrNull()
            val numLot = numLotEditText.text.toString().toIntOrNull()
            val ddm = ddmEditText.text.toString()
            val pvc = pvcEditText.text.toString().toFloatOrNull()

            ajouterAuPanier(ean, conditionnementSelected, quantite, quantiteUvc, numLot, ddm, pvc)
        }

        return view
    }

    private fun adjustFieldsBasedOnDocumentType(view: View, documentType: DocumentType?) {
        when (documentType) {
            DocumentType.COMMANDE -> {
                view.findViewById<TextView>(R.id.quantity_uvc_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.quantity_uvc_input).visibility = View.GONE
                view.findViewById<TextView>(R.id.numerolot_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.numerolot_input).visibility = View.GONE
                view.findViewById<TextView>(R.id.ddm_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.ddm_input).visibility = View.GONE
                view.findViewById<TextView>(R.id.pvc_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.pvc_input).visibility = View.GONE            }
            DocumentType.RETOUR -> {
                view.findViewById<TextView>(R.id.quantity_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.quantity_input).visibility = View.GONE
                view.findViewById<TextView>(R.id.packaging_title).visibility = View.GONE
                view.findViewById<Spinner>(R.id.packaging_input).visibility = View.GONE            }
            DocumentType.AVOIR -> {
                view.findViewById<TextView>(R.id.quantity_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.quantity_input).visibility = View.GONE
                view.findViewById<TextView>(R.id.packaging_title).visibility = View.GONE
                view.findViewById<Spinner>(R.id.packaging_input).visibility = View.GONE
            }
            else -> { /* Gérer le cas par défaut, si nécessaire */ }
        }
    }

    private fun chargerArticleEtConditionnements(ean: String, conditionnementSpinner: Spinner) {
        val articleTrouve = articles.firstOrNull { it.ean == ean }

        articleTrouve?.let { article ->
            // Mettez à jour le spinner avec les conditionnements de l'article
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, article.conditionnements)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            conditionnementSpinner.adapter = adapter

            // Sélectionnez le conditionnement par défaut (s'il y en a un)
            article.conditionnementDefaut?.let { defaultConditionnement ->
                val position = adapter.getPosition(defaultConditionnement)
                conditionnementSpinner.setSelection(position)
            }
        } ?: run {
            Toast.makeText(context, "Article avec EAN: $ean non trouvé", Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<Button>(R.id.left_button).setOnClickListener {
            // Permet de revenir en arrière lorsque le bouton gauche est cliqué
            requireActivity().supportFragmentManager.popBackStack()
        }

        val validerBouton = view.findViewById<Button>(R.id.right_button)
        validerBouton.setOnClickListener {
            val fragment = ConsignesCommandeFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    fun findArticleByEan(ean: String, articles: List<Article>): Article? {
        return articles.firstOrNull { it.ean == ean }
    }

    fun ajouterAuPanier(ean: String,
                        conditionnement: String? = null,
                        quantite: Int? = null,
                        quantiteUvc: Int? = null,
                        numLot: Int? = null,
                        ddm: String? = null,
                        pvc: Float? = null) {
        val article = findArticleByEan(ean, articles)

        if (article != null) {
            // Création d'un nouvel objet ArticlePourPanier avec les valeurs disponibles
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
            articlesDansLePanier.add(articlePourPanier)
            basketAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(context, "Article non trouvé", Toast.LENGTH_LONG).show()
        }
    }


    private fun chargerArticles() {
        lifecycleScope.launch {
            if (DataRepository.articles == null) {
                DataRepository.loadAllData()
            }

            DataRepository.articles?.let { articlesList ->
                articles = articlesList
            } ?: run {
                Toast.makeText(
                    context,
                    "Impossible de charger les données des articles.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
