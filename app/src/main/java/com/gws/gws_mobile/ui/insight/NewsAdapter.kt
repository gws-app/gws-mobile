package com.gws.gws_mobile.ui.insight

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R
import com.gws.gws_mobile.api.response.NewsItem

class NewsAdapter(private val newsList: List<NewsItem>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val readMoreButton: TextView = itemView.findViewById(R.id.readMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_recommendation_card, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.title.text = newsItem.title
        holder.description.text = newsItem.summary
        holder.readMoreButton.setOnClickListener {
            // Tindakan saat tombol dibaca lebih lanjut diklik bisa membuka detail
        }
    }

    override fun getItemCount(): Int = newsList.size
}
