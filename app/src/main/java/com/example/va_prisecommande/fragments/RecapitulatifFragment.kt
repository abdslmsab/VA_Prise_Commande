package com.example.va_prisecommande.fragments

import DocumentType
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.va_prisecommande.R
import com.example.va_prisecommande.databinding.FragmentRecapitulatifBinding
import com.example.va_prisecommande.utils.PathsConstants
import java.io.File
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.va_prisecommande.ftp.FtpDownloadTask
import com.example.va_prisecommande.ftp.FtpSendFileTask
import com.example.va_prisecommande.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileWriter

class RecapitulatifFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private var pdfName: String? = null
    private var _pdfName: String? = null
    private var documentType: DocumentType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentType = arguments?.getSerializable("documentType") as DocumentType?
        pdfName = arguments?.getSerializable("pdfName") as String?
        _pdfName = arguments?.getSerializable("_pdfName") as String?
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    private var _binding: FragmentRecapitulatifBinding? = null;
    private val binding get() = _binding!!;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecapitulatifBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val file = File(PathsConstants.LOCAL_STORAGE, pdfName)

        // Vérifier si le fichier existe
        if (file.exists()) {
            // Initialiser la vue PDF avec le fichier local
            binding.pdfView.initWithFile(
                file = file
            )

        } else {
            Toast.makeText(requireContext(), "Fichier PDF non trouvé", Toast.LENGTH_SHORT).show()
        }

        binding.rightButton.setOnClickListener {
            val file = File(PathsConstants.LOCAL_STORAGE, pdfName)
            val textFile = File(PathsConstants.LOCAL_STORAGE, "$_pdfName.txt")
            val nomClient = sharedViewModel.selectedClient.value?.nom
            val plv = sharedViewModel.plvSaisi.value.ifEmpty { "Aucune" }
            val consigne = sharedViewModel.commentaireSaisi.value.ifEmpty { "Aucune" }
            val numeroClient = sharedViewModel.selectedClient.value?.code + " | " + sharedViewModel.selectedClient.value?.nom
            val dateLivraison = sharedViewModel.dateLivraisonSaisie.value
            val dateRetour = sharedViewModel.dateRetourSaisie.value
            val prenomCommercial = sharedViewModel.selectedCommercial.value?.prenom ?: ""
            val nomCommercial = sharedViewModel.selectedCommercial.value?.nom ?: ""
            val commercial = "$prenomCommercial $nomCommercial"
            val listePanier = if (documentType == DocumentType.COMMANDE) sharedViewModel.articlesDansLePanier.value?.joinToString(separator = "\n") { article ->
                "N° Art. : ${article.code} | Qté : ${article.quantite ?: 0} | PCB : ${article.conditionnement ?: "Non spécifié"}"
            } ?: "Le panier est vide" else sharedViewModel.articlesDansLePanier.value?.joinToString(separator = "\n") { article ->
                "N° Art. : ${article.code} | Qté UVC : ${article.quantiteUvc ?: 0} | N° Lot : ${article.numLot ?: "Non spécifié"} | DDM : ${article.ddm ?: "Non spécifiée"} | PVC : ${article.pvc ?: "Non spécifié"}"
            } ?: "Le panier est vide"

            val subject = if (documentType == DocumentType.COMMANDE) "VA x COMMANDES - $nomClient" else if (documentType == DocumentType.RETOUR) "VA x RETOURS - $nomClient" else "VA x AVOIRS - $nomClient"
            val message = if (documentType == DocumentType.COMMANDE) "Commande à enregistrer immédiatement\n\nN° Client : $numeroClient\nDate Livr. : $dateLivraison\nVendeur : $commercial\n\nPLV : $plv\n\n$listePanier\n\nConsigne + : $consigne\n\nMerci" else if (documentType == DocumentType.RETOUR) "Retour à enregistrer immédiatement\n\nN° Client : $numeroClient\nDate Retour : $dateRetour\nVendeur : $commercial\n\n$listePanier\n\nConsigne + : $consigne\n\nMerci" else "Avoir à enregistrer immédiatement\n\nN° Client : $numeroClient\nDate Retour : $dateRetour\nVendeur : $commercial\n\n$listePanier\n\nConsigne + : $consigne\n\nMerci"

            // Envoi de l'email avec le fichier PDF en pièce jointe
            if (file.exists()) {
                generateTextFile()
                sendEmail("commandes@vital-aine.com", subject, message, file, textFile)
                sharedViewModel.viderToutLePanier()
            } else {
                Toast.makeText(requireContext(), "Fichier PDF non trouvé", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.left_button).setOnClickListener {
            // Permet de revenir en arrière lorsque le bouton gauche est cliqué
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    fun generateTextFile() {
        val fileName = "$_pdfName.txt"
        val numeroBon = sharedViewModel.genererNumeroBon()
        val codeClient = sharedViewModel.selectedClient.value?.code
        val dateLivraison = sharedViewModel.dateLivraisonSaisie.value
        val dateRetour = sharedViewModel.dateRetourSaisie.value

        val content = if (documentType == DocumentType.COMMANDE) sharedViewModel.articlesDansLePanier.value?.joinToString(separator = "\n") { article ->
            "$numeroBon,$codeClient,$dateLivraison,${article.code},${article.quantite ?: 0}, ${article.conditionnement ?: 0}, BC"
        } else if (documentType == DocumentType.RETOUR) sharedViewModel.articlesDansLePanier.value?.joinToString(separator = "\n") { article ->
            "$numeroBon,$codeClient,$dateRetour,${article.code},${article.quantiteUvc ?: 0}, BR"
        } else sharedViewModel.articlesDansLePanier.value?.joinToString(separator = "\n") { article ->
            "$numeroBon,$codeClient,$dateRetour,${article.code},${article.quantiteUvc ?: 0}, BA"
        }

        try {
            val file = File(PathsConstants.LOCAL_STORAGE, fileName)
            val writer = FileWriter(file)
            writer.write(content ?: "")
            writer.close()

            lifecycleScope.launch {
                uploadTextFile(file.absolutePath, "/Commandes/$fileName")
            }

        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Une erreur est survenue: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    suspend fun uploadTextFile(localFilePath: String, remoteFilePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            FtpSendFileTask().uploadTextFile("141.94.170.53", "ftpVital", "Kz5Jkud6GG", localFilePath, remoteFilePath)
        }
    }

    @SuppressLint("IntentReset")
    private fun sendEmail(recipient: String, subject: String, message: String, file: File, textFile: File) {
        val mIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            data = Uri.parse("mailto:")
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)

            val files = arrayListOf<Uri>()

            val fileUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${context?.packageName}.provider",
                file
            )
            files.add(fileUri)

            val textFileUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${context?.packageName}.provider",
                textFile
            )
            files.add(textFileUri)

            putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        }

        try {
            startActivity(Intent.createChooser(mIntent, "Choisissez une application de messagerie"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()

        val fragment = ClientFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}