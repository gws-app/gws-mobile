package com.gws.gws_mobile.ui.insight

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R
import com.gws.gws_mobile.api.response.ContentsItem
import com.gws.gws_mobile.ui.insight.detail.DetailActivity

class NewsAdapter(private val contents: List<ContentsItem?>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val readMoreButton: TextView = itemView.findViewById(R.id.readMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_recommendation_card, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val contentItem = contents[position]

        if (contentItem != null) {
            holder.title.text = contentItem.headline ?: "No title available"
            holder.description.text = contentItem.description ?: "No description available"

            holder.readMoreButton.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("NEWS_TITLE", contentItem.description)
                    putExtra("NEWS_DESCRIPTION", contentItem.text)
                }
                context.startActivity(intent)
            }
        } else {
            holder.title.text = "No title available"
            holder.description.text = "No description available"
        }
    }


    override fun getItemCount(): Int = contents.size
}