package com.gws.gws_mobile.ui.insight

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R

class RecommendationTagAdapter(private var tags: List<String>) :
    RecyclerView.Adapter<RecommendationTagAdapter.TagViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recommendation_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tags[position])
    }

    override fun getItemCount() = tags.size

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tagText: TextView = itemView.findViewById(R.id.tagText)
        fun bind(tag: String) {
            tagText.text = tag
        }
    }

    fun updateTags(newTags: List<String>) {
        tags = newTags
        notifyDataSetChanged()
    }
}