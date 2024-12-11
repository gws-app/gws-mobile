package com.gws.gws_mobile.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R
import com.gws.gws_mobile.api.response.MoodDataResponse
import com.gws.gws_mobile.databinding.ItemMoodHistoryBinding

class MoodHistoryAdapter(private val moodList: List<MoodDataResponse>) :
    RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodData = moodList[position]
        holder.bind(moodData)
    }

    override fun getItemCount(): Int = moodList.size

    inner class MoodViewHolder(private val binding: ItemMoodHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(moodData: MoodDataResponse) {
            binding.apply {
                emojiMood.setImageResource(
                    when (moodData.emotion) {
                        "bliss" -> R.drawable.ic_45_lupbgt
                        "bright" -> R.drawable.ic_45_okegpp
                        "neutral" -> R.drawable.ic_45_smile
                        "low" -> R.drawable.ic_45_sad
                        "crumble" -> R.drawable.ic_45_sadbed
                        else -> R.drawable.ic_45_smile
                    }
                )
                textMoodName.text = moodData.emotion?.capitalize() ?: "Neutral"
                textLogActivities.text = "Logged Activities: ${
                    moodData.activities}"
                Log.d("MoodHistoryAdapter", "Activities: ${moodData.activities}")
                textDate.text = moodData.created_at
                textNotes.text = " Notes: ${moodData.note}"
            }
        }
    }
}