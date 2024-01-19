package com.example.va_prisecommande.fragments

import DocumentType
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.va_prisecommande.R
import com.google.android.material.textfield.TextInputLayout

class ConsignesFragment : Fragment() {
    private var documentType: DocumentType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentType = arguments?.getSerializable("documentType") as DocumentType?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater?.inflate(R.layout.fragment_consignes, container, false)

        if (view != null) {
            adjustFieldsBasedOnDocumentType(view, documentType)
        }

        return view
    }

    private fun adjustFieldsBasedOnDocumentType(view: View, documentType: DocumentType?) {
        when (documentType) {
            DocumentType.COMMANDE -> {

            }
            DocumentType.RETOUR -> {

            }
            DocumentType.AVOIR -> {

            }
            else -> { /* Gérer le cas par défaut, si nécessaire */ }
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
            val fragment = RecapitulatifFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}