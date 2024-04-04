package com.example.va_prisecommande.fragments

import DocumentType
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.va_prisecommande.R
import com.example.va_prisecommande.adapter.BasketAdapter
import com.example.va_prisecommande.databinding.FragmentPanierBinding
import com.example.va_prisecommande.model.Article
import com.example.va_prisecommande.viewmodel.DateViewModel
import com.example.va_prisecommande.viewmodel.SharedViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PanierFragment : Fragment() {
    private lateinit var basketAdapter: BasketAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var dateViewModel: DateViewModel
    private var articles: List<Article> = emptyList()
    private var articleTrouve: Article? = null
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

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        dateViewModel = ViewModelProvider(this).get(DateViewModel::class.java)

        basketAdapter = BasketAdapter(documentType!!)
    }

    private var _binding:FragmentPanierBinding? = null;
    private val binding get() = _binding!!;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPanierBinding.inflate(inflater,container,false);
        val view = binding.root;

        adjustFieldsBasedOnDocumentType(view, documentType)
        val recyclerView = view.findViewById<RecyclerView>(R.id.vertical_recycler_view)

        documentType?.let {
            basketAdapter = BasketAdapter(it)
        }

        // basketAdapter = BasketAdapter(articlesDansLePanier, documentType!!)
        recyclerView.adapter = basketAdapter

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateViewModel.setDateActuelle(dateFormat.format(Date()))

        val listener = MaskedTextChangedListener("[00]/[00]/[00]", binding.ddmEditText)
        binding.ddmEditText.addTextChangedListener(listener)

        val listenerPvc = MaskedTextChangedListener("[09]{.}[99]", binding.pvcEditText)
        binding.pvcEditText.addTextChangedListener(listenerPvc)

        binding.quantityInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        sharedViewModel.changementQuantite(s.toString())
                    }
                }
            }
        })

        binding.quantityUvcInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        sharedViewModel.changementQuantiteUVC(s.toString())
                    }
                }
            }
        })

        binding.numerolotInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        // Vérification du champ "numeroLot"
                        lifecycleScope.launch {
                            sharedViewModel.estNumeroLotValide.collect { estNumeroLotValide ->
                                if (!estNumeroLotValide) {
                                    binding.numerolotInput.error = "Le numéro de lot n'est pas valide."
                                } else {
                                    binding.numerolotInput.isErrorEnabled = false
                                }
                            }
                        }
                        sharedViewModel.changementNumeroLot(s.toString())
                    }
                }
            }
        })

        binding.ddmInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        // Vérification du champ "DDM"
                        lifecycleScope.launch {
                            sharedViewModel.estDdmValide.collect { estDdmValide ->
                                if (!estDdmValide) {
                                    binding.ddmInput.error = "La Date de Durabilité Minimale n'est pas conforme."
                                } else {
                                    binding.ddmInput.isErrorEnabled = false
                                }
                            }
                        }

                        sharedViewModel.changementDdm(s.toString())
                    }
                }
            }
        })

        binding.pvcInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        sharedViewModel.changementPvc(s.toString())
                    }
                }
            }
        })

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
                val eanComplet = eanInputLayout.prefixText.toString() + s.toString()
                if (s != null) {
                    if (s.length == 5) {
                        chargerArticleEtConditionnements(eanComplet, conditionnementSpinner)
                    }

                    // Vérification du champ "Ean"
                    lifecycleScope.launch {
                        sharedViewModel.estEanValide.collect { estEanValide ->
                            if (!estEanValide) {
                                binding.eanInput.error = "Le code opérateur requiert 13 caractères"
                            } else {
                                binding.eanInput.isErrorEnabled = false
                            }
                        }
                    }
                    sharedViewModel.changementEan(eanComplet)
                }
            }
        })

        view.findViewById<Button>(R.id.add_button).setOnClickListener {
            val ean = eanInputLayout.prefixText.toString() + eanEditText.text.toString()
            val selectedItem = conditionnementSpinner.selectedItem
            val conditionnementSelected = selectedItem?.toString() ?: "Valeur par défaut"
            val quantite = quantiteEditText.text.toString().toIntOrNull() ?: 0

            val quantiteUvc = quantiteUvcEditText.text.toString().toIntOrNull() ?: 0
            val numLot = numLotEditText.text.toString().toIntOrNull()
            val ddm = ddmEditText.text.toString()
            val pvc = pvcEditText.text.toString().toFloatOrNull()
            val code = articleTrouve?.code ?: "" // TODO
            val nombreArticles = sharedViewModel.articlesDansLePanier.value?.size ?: 0

            val article = sharedViewModel.articles.value?.firstOrNull { it.ean == ean }

            if (article != null) {
                if (documentType == DocumentType.COMMANDE) {
                    if (quantite == 0) {
                        // Afficher un Toast ou un message d'erreur indiquant que la quantité ne peut pas être zéro
                        Toast.makeText(
                            requireContext(),
                            "La quantité ne peut pas être zéro. Veuillez entrer une valeur valide.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Ajouter l'article au panier uniquement si la quantité n'est pas zéro
                        sharedViewModel.ajouterAuPanier(
                            ean,
                            code,
                            conditionnementSelected,
                            quantite,
                            quantiteUvc,
                            numLot,
                            ddm,
                            pvc,
                        )
                        eanEditText.text?.clear()
                        quantiteEditText.text?.clear()
                        eanInputLayout.error = null

                        val message = if (nombreArticles == 0) {
                            "${nombreArticles + 1} article dans le panier"
                        } else {
                            "${nombreArticles + 1} articles dans le panier"
                        }
                        binding.nombreArticlePanier.text = message
                    }
                }

                if (documentType == DocumentType.AVOIR || documentType == DocumentType.RETOUR) {
                    if (quantiteUvc == 0) {
                        Toast.makeText(
                            requireContext(),
                            "La quantité UVC ne peut pas être zéro. Veuillez entrer une valeur valide.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (pvc == null) {
                        // Afficher un Toast ou un message d'erreur indiquant que le pvc ne peut pas être zéro
                        Toast.makeText(
                            requireContext(),
                            "Le prix de vente constaté ne peut pas être nul. Veuillez entrer une valeur valide.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        sharedViewModel.ajouterAuPanier(
                            ean,
                            code,
                            conditionnementSelected,
                            quantite,
                            quantiteUvc,
                            numLot,
                            ddm,
                            pvc
                        )
                        eanEditText.text?.clear()
                        quantiteUvcEditText.text?.clear()
                        eanInputLayout.error = null
                        numLotEditText.text?.clear()
                        ddmEditText.text?.clear()
                        pvcEditText.text?.clear()

                        val message = if (nombreArticles == 0) {
                            "${nombreArticles + 1} article dans le panier"
                        } else {
                            "${nombreArticles + 1} articles dans le panier"
                        }
                        binding.nombreArticlePanier.text = message
                    }
                }
            } else {
                Toast.makeText(requireContext(), "L'article avec le code-barres $ean n'existe pas.", Toast.LENGTH_SHORT).show()
            }
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
        articleTrouve = articles.firstOrNull { it.ean == ean }

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

        sharedViewModel.chargerArticles()

        sharedViewModel.articles.observe(viewLifecycleOwner) { articlesList ->
            articles = articlesList
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.vertical_recycler_view)
        recyclerView.adapter = basketAdapter

        sharedViewModel.articlesDansLePanier.observe(viewLifecycleOwner) { articles ->
            basketAdapter.setArticlesDansLePanier(articles)
        }
        basketAdapter.onDeleteArticle = { article ->
            sharedViewModel.retirerDuPanier(article)
        }

        view.findViewById<Button>(R.id.left_button).setOnClickListener {
            // Permet de revenir en arrière lorsque le bouton gauche est cliqué
            requireActivity().supportFragmentManager.popBackStack()

            sharedViewModel.viderToutLePanier()
        }

        val validerBouton = view.findViewById<Button>(R.id.right_button)
        validerBouton.setOnClickListener {
            if (sharedViewModel.articlesDansLePanier.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Le panier est vide. Ajoutez des articles avant de valider.", Toast.LENGTH_SHORT).show()
            } else {
                val consignesFragment = ConsignesFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("documentType", documentType)
                    }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, consignesFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}