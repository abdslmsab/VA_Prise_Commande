package com.example.va_prisecommande.adapter

import DocumentType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.va_prisecommande.R
import com.example.va_prisecommande.model.ArticlePourPanier

class BasketAdapter(private val documentType: DocumentType) : RecyclerView.Adapter<BasketAdapter.ViewHolder>() {

    private var articlesDansLePanier: List<ArticlePourPanier> = emptyList()

    var onDeleteArticle: ((ArticlePourPanier) -> Unit)? = null

    fun setArticlesDansLePanier(articles: List<ArticlePourPanier>) {
        this.articlesDansLePanier = articles
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eanTextView: TextView = view.findViewById(R.id.ean)
        val productNameTextView: TextView = view.findViewById(R.id.product_name)

        val quantityTitle: TextView = view.findViewById(R.id.quantity)
        val quantityTextView: TextView = view.findViewById(R.id.quantity_input)

        val packagingTitle: TextView = view.findViewById(R.id.packaging)
        val packagingTextView: TextView = view.findViewById(R.id.packaging_input)

        val quantityUvcTitle: TextView = view.findViewById(R.id.quantity_uvc)
        val quantityUvcTextView: TextView = view.findViewById(R.id.quantity_uvc_input)

        val numeroLotTitle: TextView = view.findViewById(R.id.numerolot)
        val numeroLotTextView: TextView = view.findViewById(R.id.numerolot_input)

        val ddmTitle: TextView = view.findViewById(R.id.ddm)
        val ddmTextView: TextView = view.findViewById(R.id.ddm_input)

        val pvcTitle: TextView = view.findViewById(R.id.pvc)
        val pvcTextView: TextView = view.findViewById(R.id.pvc_input)

        val euroTextView: TextView = view.findViewById(R.id.euro)

        val deleteButton: Button = view.findViewById(R.id.delete_article_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_vertical_panier, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = articlesDansLePanier.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articlesDansLePanier[position]

        holder.eanTextView.text = article.ean
        holder.productNameTextView.text = article.nom

        when (documentType) {
            DocumentType.COMMANDE -> {
                // Affichage pour une commande
                holder.packagingTextView.text = article.conditionnement
                holder.quantityTextView.text = article.quantite.toString()

                holder.quantityUvcTitle.visibility = View.GONE
                holder.quantityUvcTextView.visibility = View.GONE

                holder.numeroLotTitle.visibility = View.GONE
                holder.numeroLotTextView.visibility = View.GONE

                holder.ddmTitle.visibility = View.GONE
                holder.ddmTextView.visibility = View.GONE

                holder.pvcTitle.visibility = View.GONE
                holder.pvcTextView.visibility = View.GONE

                holder.euroTextView.visibility = View.GONE
            }
            DocumentType.RETOUR -> {
                // Affichage pour un retour
                holder.quantityUvcTextView.text = article.quantiteUvc.toString()
                holder.numeroLotTextView.text = article.numLot.toString()
                holder.ddmTextView.text = article.ddm.toString()
                holder.pvcTextView.text = article.pvc.toString()

                holder.packagingTitle.visibility = View.GONE
                holder.packagingTextView.visibility = View.GONE

                holder.quantityTitle.visibility = View.GONE
                holder.quantityTextView.visibility = View.GONE
            }
            DocumentType.AVOIR -> {
                // Affichage pour un avoir
                holder.quantityUvcTextView.text = article.quantiteUvc.toString()
                holder.numeroLotTextView.text = article.numLot.toString()
                holder.ddmTextView.text = article.ddm.toString()
                holder.pvcTextView.text = article.pvc.toString()

                holder.packagingTitle.visibility = View.GONE
                holder.packagingTextView.visibility = View.GONE

                holder.quantityTitle.visibility = View.GONE
                holder.quantityTextView.visibility = View.GONE
            }
        }

        /*
        // Gestion du bouton supprimer
        holder.deleteButton.setOnClickListener {
            articlesDansLePanier.remove(article)
            notifyDataSetChanged()
        }
         */

        holder.deleteButton.setOnClickListener {
            onDeleteArticle?.invoke(article)
        }
    }
}