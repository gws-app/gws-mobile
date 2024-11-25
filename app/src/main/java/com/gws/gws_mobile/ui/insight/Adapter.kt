package com.gws.gws_mobile.ui.insight

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R

// ItemAdapter.kt
class ItemAdapter(private val itemList: List<ItemData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_RECOMMENDATION = 0
        private const val VIEW_TYPE_NEWS = 1
    }

    // ViewHolder untuk Recommendation dan News (karena layoutnya sama)
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val readMoreButton: Button = itemView.findViewById(R.id.readMore)
    }

    // Menentukan ViewType untuk item
    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is ItemData.Recommendation -> VIEW_TYPE_RECOMMENDATION
            is ItemData.News -> VIEW_TYPE_NEWS
        }
    }

    // Membuat ViewHolder berdasarkan ViewType
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_recommendation_card, parent, false)
        return ItemViewHolder(view)
    }

    // Menghubungkan data ke tampilan item
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        when (holder) {
            is ItemViewHolder -> {
                when (item) {
                    is ItemData.Recommendation -> {
                        holder.title.text = item.title
                        holder.description.text = item.description
                        holder.readMoreButton.setOnClickListener {
                            // Tindakan saat tombol dibaca lebih lanjut diklik untuk Recommendation
                        }
                    }
                    is ItemData.News -> {
                        holder.title.text = item.title
                        holder.description.text = item.description
                        holder.readMoreButton.setOnClickListener {
                            // Tindakan saat tombol dibaca lebih lanjut diklik untuk News
                        }
                    }
                }
            }
        }
    }

    // Menghitung jumlah item
    override fun getItemCount(): Int {
        return itemList.size
    }
}

