package com.example.va_prisecommande.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.va_prisecommande.R
import com.example.va_prisecommande.databinding.FragmentRecapitulatifBinding
import com.example.va_prisecommande.utils.PathsConstants
import com.rajat.pdfviewer.PdfViewerActivity
import com.rajat.pdfviewer.util.saveTo
import java.io.File
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.redmadrobot.inputmask.BuildConfig

class RecapitulatifFragment : Fragment() {
    private var pdfName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pdfName = arguments?.getSerializable("pdfName") as String?
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

            val subject = "TEST"
            val message = "Ceci est un test"

            // Envoi de l'email avec le fichier PDF en pièce jointe
            if (file.exists()) {
                sendEmail("commandes@vital-aine.com", subject, message, file)
            } else {
                Toast.makeText(requireContext(), "Fichier PDF non trouvé", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.left_button).setOnClickListener {
            // Permet de revenir en arrière lorsque le bouton gauche est cliqué
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    @SuppressLint("IntentReset")
    private fun sendEmail(recipient: String, subject: String, message: String, file: File) {
        val mIntent = Intent(Intent.ACTION_SEND).apply {
            data = Uri.parse("mailto:")
            type = "vnd.android.cursor.dir/email"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)

            val fileUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${context?.packageName}.provider",
                file
            )
            putExtra(Intent.EXTRA_STREAM, fileUri)
        }

        try {
            startActivity(Intent.createChooser(mIntent, "Choisissez une application de messagerie"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }
}