package com.example.va_prisecommande.fragments

import DocumentType
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.va_prisecommande.R
import com.example.va_prisecommande.databinding.FragmentConsignesBinding
import com.example.va_prisecommande.utils.PathsConstants
import com.example.va_prisecommande.viewmodel.SharedViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class ConsignesFragment : Fragment() {
    private var documentType: DocumentType? = null
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentType = arguments?.getSerializable("documentType") as DocumentType?
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    private var _binding: FragmentConsignesBinding? = null;
    private val binding get() = _binding!!;

    private var dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var dateBonFormat = SimpleDateFormat("ddMMyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConsignesBinding.inflate(inflater, container, false)
        val view = binding.root;

        val livraisonInputLayout = view?.findViewById<TextInputLayout>(R.id.livraison_input)
        val livraisonEditText = livraisonInputLayout?.editText as TextInputEditText

        val plvInputLayout = view.findViewById<TextInputLayout>(R.id.plv_input)
        val plvEditText = plvInputLayout?.editText as TextInputEditText

        val listenerDateLivraison =
            MaskedTextChangedListener("[00]/[00]/[00]", binding.livraisonEditText)
        binding.livraisonEditText.addTextChangedListener(listenerDateLivraison)

        val listenerDateRetour = MaskedTextChangedListener("[00]/[00]/[00]", binding.retourEditText)
        binding.retourEditText.addTextChangedListener(listenerDateRetour)

        if (view != null) {
            adjustFieldsBasedOnDocumentType(view, documentType)
        }

        return view
    }

    private fun adjustFieldsBasedOnDocumentType(view: View, documentType: DocumentType?) {
        when (documentType) {
            DocumentType.COMMANDE -> {
                view.findViewById<TextView>(R.id.retour_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.retour_input).visibility = View.GONE
            }

            DocumentType.RETOUR -> {
                view.findViewById<TextView>(R.id.livraison_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.livraison_input).visibility = View.GONE

                view.findViewById<TextView>(R.id.plv_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.plv_input).visibility = View.GONE
            }

            DocumentType.AVOIR -> {
                view.findViewById<TextView>(R.id.livraison_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.livraison_input).visibility = View.GONE

                view.findViewById<TextView>(R.id.plv_title).visibility = View.GONE
                view.findViewById<TextInputLayout>(R.id.plv_input).visibility = View.GONE
            }

            else -> { /* Gérer le cas par défaut, si nécessaire */
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val livraisonInputLayout = view.findViewById<TextInputLayout>(R.id.livraison_input)
        val livraisonEditText = livraisonInputLayout.editText as TextInputEditText

        val plvInputLayout = view.findViewById<TextInputLayout>(R.id.plv_input)
        val plvEditText = plvInputLayout.editText as TextInputEditText

        val retourInputLayout = view.findViewById<TextInputLayout>(R.id.retour_input)
        val retourEditText = retourInputLayout.editText as TextInputEditText

        val commentaireInputLayout = view.findViewById<TextInputLayout>(R.id.commentaire_input)
        val commentaireEditText = commentaireInputLayout.editText as TextInputEditText

        binding.commentaireInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        sharedViewModel.changementCommentaire(s.toString())
                    }
                }
            }
        })

        binding.plvInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        sharedViewModel.changementPlv(s.toString())
                    }
                }
            }
        })

        binding.livraisonInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        // Vérification du champ "DateLivraison"
                        lifecycleScope.launch {
                            sharedViewModel.estDateLivraisonValide.collect { estDateLivraisonValide ->
                                if (!estDateLivraisonValide) {
                                    binding.livraisonInput.error =
                                        "La Date de Livraison n'est pas conforme."
                                } else {
                                    binding.livraisonInput.isErrorEnabled = false
                                }
                            }
                        }
                        sharedViewModel.changementDateLivraison(s.toString())
                    }
                }
            }
        })

        binding.retourInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        // Vérification du champ "DateRetour"
                        lifecycleScope.launch {
                            sharedViewModel.estDateRetourValide.collect { estDateRetourValide ->
                                if (!estDateRetourValide) {
                                    binding.retourInput.error =
                                        "La Date de retour n'est pas conforme."
                                } else {
                                    binding.retourInput.isErrorEnabled = false
                                }
                            }
                        }

                        sharedViewModel.changementDateRetour(s.toString())
                    }
                }
            }
        })

        view.findViewById<Button>(R.id.left_button).setOnClickListener {
            // Permet de revenir en arrière lorsque le bouton gauche est cliqué
            requireActivity().supportFragmentManager.popBackStack()
        }

        val validerBouton = view.findViewById<Button>(R.id.right_button)
        validerBouton.setOnClickListener {
            //On génère le PDF
            val numeroBon = genererNumeroBon()
            val pdfName = (sharedViewModel.selectedClient.value?.code
                ?: 0).toString() + "-$numeroBon.pdf"
            val file = File(PathsConstants.LOCAL_STORAGE, pdfName)
            genererPdf(file, documentType)

            Toast.makeText(
                requireContext(),
                "PDF généré dans le fichier 'Documents' de la tablette",
                Toast.LENGTH_LONG
            ).show()

            val recapitulatifFragment = RecapitulatifFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("pdfName", pdfName)
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, recapitulatifFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun genererNumeroBon(): String {
        val numeroBon: String

        val prefix = when (documentType) {
            DocumentType.COMMANDE -> "BC"
            DocumentType.AVOIR -> "BA"
            DocumentType.RETOUR -> "BR"
            else -> {}
        }

        val heureFormat = SimpleDateFormat("HHmm")
        // val today = LocalDate.now()
        // val dayOfYear = today.format(DateTimeFormatter.ofPattern("DDD"))
        val initialePrenomCommercial = sharedViewModel.selectedCommercial.value?.prenom?.get(0)
        val initialeNomCommercial = sharedViewModel.selectedCommercial.value?.nom?.get(0)

        numeroBon = "${prefix}${dateBonFormat.format(Date())}${heureFormat.format(Date())}$initialePrenomCommercial$initialeNomCommercial"

        return numeroBon
    }

    fun genererPdf(file: File?, documentType: DocumentType?) {
        try {
            val writer = PdfWriter(file)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc, PageSize.A4)
            document.setBackgroundColor(ColorConstants.WHITE)

            val tailleTitre = 15F
            val taillePetitTitre = 12F

            //Crée le tableau à une ligne et trois colonnes
            val tableInfos: Table = Table(3).useAllAvailableWidth()

            val numeroBon = genererNumeroBon()

            //Crée les cellules du tableau
            val cell1: Cell = Cell().add(Paragraph(dateFormat.format(Date())))
                .setTextAlignment(TextAlignment.CENTER)

            val preposition = if (documentType == DocumentType.AVOIR) "d'" else "de"
            val cell2: Cell = Cell().add(Paragraph("BON $preposition $documentType n° $numeroBon"))
                .setTextAlignment(TextAlignment.CENTER)

            val cell3: Cell = Cell().add(Paragraph(sharedViewModel.selectedCommercial.value?.nom))
                .setTextAlignment(TextAlignment.CENTER)

            val titreStyle = Style().setBold().setFontSize(tailleTitre)

            cell1.addStyle(titreStyle)
            cell2.addStyle(titreStyle)
            cell3.addStyle(titreStyle)

            //Rend les bordures des cellules invisibles
            cell1.setBorder(Border.NO_BORDER)
            cell2.setBorder(Border.NO_BORDER)
            cell3.setBorder(Border.NO_BORDER)

            //Ajoute les cellules au tableau
            tableInfos.addCell(cell1)
            tableInfos.addCell(cell2)
            tableInfos.addCell(cell3)

            tableInfos.setHorizontalAlignment(HorizontalAlignment.CENTER)

            //Ajoute le tableau au document
            document.add(tableInfos)

            document.add(Paragraph("\n"))

            val tableClient: Table = Table(3)
                .useAllAvailableWidth()

            val cell4: Cell = Cell().add(Paragraph("N° Client"))
                .setTextAlignment(TextAlignment.CENTER)
            val cell5: Cell = Cell().add(Paragraph("Libellé Client"))
                .setTextAlignment(TextAlignment.CENTER)

            tableClient.addCell(cell4)
            tableClient.addCell(cell5)

            cell4.addStyle(titreStyle)
            cell5.addStyle(titreStyle)

            if (documentType == DocumentType.COMMANDE) {
                val cell6: Cell = Cell().add(Paragraph("Date de Livraison"))
                    .setTextAlignment(TextAlignment.CENTER)
                tableClient.addCell(cell6)
                cell6.addStyle(titreStyle)
            } else {
                val cell6: Cell = Cell().add(Paragraph("Date de Reprise"))
                    .setTextAlignment(TextAlignment.CENTER)
                tableClient.addCell(cell6)
                cell6.addStyle(titreStyle)
            }

            tableClient.addCell(sharedViewModel.selectedClient.value?.code)
                .setTextAlignment(TextAlignment.CENTER)
            tableClient.addCell(sharedViewModel.selectedClient.value?.nom)
                .setTextAlignment(TextAlignment.CENTER)

            if (documentType == DocumentType.COMMANDE) {
                tableClient.addCell(sharedViewModel.dateLivraisonSaisie.value)
                    .setTextAlignment(TextAlignment.CENTER)
            } else {
                tableClient.addCell(sharedViewModel.dateRetourSaisie.value)
                    .setTextAlignment(TextAlignment.CENTER)
            }

            document.add(tableClient)

            document.add(Paragraph("\n"))

            val nombreDeColonnes = if (documentType == DocumentType.COMMANDE) 4 else 5

            val tableArticle: Table = Table(nombreDeColonnes)
                .useAllAvailableWidth()

            val cell7: Cell = Cell().add(Paragraph("N° Article"))
                .setTextAlignment(TextAlignment.CENTER)
            val cell8: Cell = Cell().add(Paragraph("Libellé Article"))
                .setTextAlignment(TextAlignment.CENTER)

            tableArticle.addCell(cell7)
            tableArticle.addCell(cell8)

            cell7.addStyle(titreStyle)
            cell8.addStyle(titreStyle)

            val petitTitreStyle = Style().setBold().setFontSize(taillePetitTitre)

            if (documentType == DocumentType.COMMANDE) {
                val cell9: Cell = Cell().add(Paragraph("Qté"))
                    .setTextAlignment(TextAlignment.CENTER)
                val cell10: Cell = Cell().add(Paragraph("PCB"))
                    .setTextAlignment(TextAlignment.CENTER)

                tableArticle.addCell(cell9)
                tableArticle.addCell(cell10)

                cell9.addStyle(titreStyle)
                cell10.addStyle(titreStyle)

                sharedViewModel.articlesDansLePanier.value?.forEach { articlePourPanier ->
                    tableArticle.addCell(articlePourPanier.code)
                        .setTextAlignment(TextAlignment.CENTER)
                    tableArticle.addCell(articlePourPanier.nom)
                        .setTextAlignment(TextAlignment.LEFT)
                    tableArticle.addCell(articlePourPanier.quantite.toString())
                        .setTextAlignment(TextAlignment.CENTER)
                    tableArticle.addCell(articlePourPanier.conditionnement)
                        .setTextAlignment(TextAlignment.CENTER)
                }

                for (i in 1..(21 - (sharedViewModel.articlesDansLePanier.value?.size ?: 0))) {
                    val cell1: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                    val cell2: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                    val cell3: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                    val cell4: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)

                    tableArticle.addCell(cell1)
                    tableArticle.addCell(cell2)
                    tableArticle.addCell(cell3)
                    tableArticle.addCell(cell4)
                }
            } else {
                val cell9: Cell = Cell().add(Paragraph("UVC"))
                    .setTextAlignment(TextAlignment.CENTER)
                val cell10: Cell = Cell().add(Paragraph("LOT"))
                    .setTextAlignment(TextAlignment.CENTER)
                val cell11: Cell = Cell().add(Paragraph("PVC"))
                    .setTextAlignment(TextAlignment.CENTER)

                tableArticle.addCell(cell9)
                tableArticle.addCell(cell10)
                tableArticle.addCell(cell11)

                cell9.addStyle(petitTitreStyle)
                cell10.addStyle(petitTitreStyle)
                cell11.addStyle(petitTitreStyle)

                sharedViewModel.articlesDansLePanier.value?.forEach { articlePourPanier ->
                    tableArticle.addCell(articlePourPanier.code)
                        .setTextAlignment(TextAlignment.CENTER)
                    tableArticle.addCell(articlePourPanier.nom)
                        .setTextAlignment(TextAlignment.LEFT)
                    tableArticle.addCell(articlePourPanier.quantiteUvc.toString())
                        .setTextAlignment(TextAlignment.CENTER)
                    tableArticle.addCell(articlePourPanier.numLot.toString())
                        .setTextAlignment(TextAlignment.CENTER)
                    tableArticle.addCell(articlePourPanier.pvc.toString() + " €")
                        .setTextAlignment(TextAlignment.CENTER)
                }

                for (i in 1..(21 - (sharedViewModel.articlesDansLePanier.value?.size ?: 0))) {
                    val cell1: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                    val cell2: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                    val cell3: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                    val cell4: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                    val cell5: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)

                    tableArticle.addCell(cell1)
                    tableArticle.addCell(cell2)
                    tableArticle.addCell(cell3)
                    tableArticle.addCell(cell4)
                    tableArticle.addCell(cell5)
                }
            }

            document.add(tableArticle)

            document.add(Paragraph("\n"))

            val nombreDeColonnesConsignes = if (documentType == DocumentType.COMMANDE) 2 else 1

            val tableConsignes: Table = Table(nombreDeColonnesConsignes)
                .useAllAvailableWidth()

            if (documentType == DocumentType.COMMANDE) {
                val cell11: Cell = Cell().add(Paragraph("Consignes +"))
                    .setTextAlignment(TextAlignment.CENTER)
                val cell12: Cell = Cell().add(Paragraph("PLV"))
                    .setTextAlignment(TextAlignment.CENTER)

                tableConsignes.addCell(cell11)
                tableConsignes.addCell(cell12)

                cell11.addStyle(titreStyle)
                cell12.addStyle(titreStyle)

                if (sharedViewModel.commentaireSaisi.value.isNotEmpty() || sharedViewModel.plvSaisi.value.isNotEmpty()) {
                    tableConsignes.addCell(sharedViewModel.commentaireSaisi.value)
                        .setTextAlignment(TextAlignment.LEFT)
                    tableConsignes.addCell(sharedViewModel.plvSaisi.value)
                        .setTextAlignment(TextAlignment.CENTER)
                } else {
                    val cell13: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                    val cell14: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)

                    tableConsignes.addCell(cell13)
                    tableConsignes.addCell(cell14)
                }
            } else {
                val cell11: Cell = Cell().add(Paragraph("Consignes +"))
                    .setTextAlignment(TextAlignment.CENTER)

                tableConsignes.addCell(cell11)

                if (sharedViewModel.commentaireSaisi.value.isNotEmpty()) {
                    tableConsignes.addCell(sharedViewModel.commentaireSaisi.value)
                        .setTextAlignment(TextAlignment.LEFT)
                } else {
                    val cell13: Cell = Cell().add(Paragraph(" - "))
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)

                    tableConsignes.addCell(cell13)
                }

                cell11.addStyle(titreStyle)
            }


            document.add(tableConsignes)

            document.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
}