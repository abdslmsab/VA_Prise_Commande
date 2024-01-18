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
import com.example.va_prisecommande.fragments.ClientFragment
import com.example.va_prisecommande.fragments.PanierAvoirFragment
import com.example.va_prisecommande.fragments.PanierCommandeFragment
import com.example.va_prisecommande.fragments.PanierRetourFragment

class DocumentTypeDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.popup_doc_type, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val commandeButton = view.findViewById<Button>(R.id.commande_button)
        commandeButton.setOnClickListener {
            val fragment = PanierCommandeFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()

            dismiss()
        }

        val retourButton = view.findViewById<Button>(R.id.retour_button)
        retourButton.setOnClickListener {
            val fragment = PanierRetourFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()

            dismiss()
        }

        val avoirButton = view.findViewById<Button>(R.id.avoir_button)
        avoirButton.setOnClickListener {
            val fragment = PanierAvoirFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()

            dismiss()
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