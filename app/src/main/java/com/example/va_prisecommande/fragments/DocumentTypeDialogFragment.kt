import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.va_prisecommande.R
import com.example.va_prisecommande.fragments.PanierFragment

class DocumentTypeDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.popup_doc_type, container,false)
    }

    private fun navigateToPanierFragmentWithType(documentType: DocumentType) {
        val fragment = PanierFragment.newInstance(documentType)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val commandeButton = view.findViewById<Button>(R.id.commande_button)
        commandeButton.setOnClickListener {
            navigateToPanierFragmentWithType(DocumentType.COMMANDE)
        }


        val retourButton = view.findViewById<Button>(R.id.retour_button)
        retourButton.setOnClickListener {
            navigateToPanierFragmentWithType(DocumentType.RETOUR)
        }


        val avoirButton = view.findViewById<Button>(R.id.avoir_button)
        avoirButton.setOnClickListener {
            navigateToPanierFragmentWithType(DocumentType.AVOIR)
        }

        // Close dialog if "Annuler" text is clicked
        val cancelButton = view.findViewById<TextView>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}