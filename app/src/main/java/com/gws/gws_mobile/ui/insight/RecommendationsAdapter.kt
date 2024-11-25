package com.gws.gws_mobile.ui.insight

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R
import com.gws.gws_mobile.api.response.RecommendationsItem
import com.gws.gws_mobile.ui.insight.detail.DetailActivity

class RecommendationsAdapter(private val recommendationsList: List<RecommendationsItem>) :
    RecyclerView.Adapter<RecommendationsAdapter.RecommendationsViewHolder>() {

    class RecommendationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val readMoreButton: TextView = itemView.findViewById(R.id.readMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_recommendation_card, parent, false)
        return RecommendationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationsViewHolder, position: Int) {
        val recommendationItem = recommendationsList[position]
        holder.title.text = recommendationItem.title
        holder.description.text = recommendationItem.summary
        holder.readMoreButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("ITEM_ID", recommendationItem.id)
            intent.putExtra("TYPE", "recommendations")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = recommendationsList.size
}
