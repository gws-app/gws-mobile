package com.gws.gws_mobile.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R
import com.gws.gws_mobile.database.mood.Mood
import com.gws.gws_mobile.databinding.ItemMoodHistoryBinding

class MoodHistoryAdapter(private var moods: List<Mood>) :
    RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

    fun updateData(newMoods: List<Mood>) {
        moods = newMoods.sortedByDescending { it.created_at }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodData = moods[position]
        holder.bind(moodData)
    }

    override fun getItemCount(): Int = moods.size

    inner class MoodViewHolder(private val binding: ItemMoodHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(moodData: Mood) {
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
                textMoodName.text = moodData.emotion.replaceFirstChar { it.uppercaseChar() }

                val activityString = moodData.activities.trim('{', '}')
                val activityList = mutableListOf<String>()

                val keyValuePairs = activityString.split("],")

                keyValuePairs.forEach { pair ->
                    val cleanedPair = if (!pair.endsWith("]")) "$pair]" else pair
                    val splitPair = cleanedPair.split("=")
                    if (splitPair.size == 2) {
                        val activitiesPart = splitPair[1].trim('[', ']')
                        val activities = activitiesPart.split(",")
                        activities.forEach { activity ->
                            activityList.add(activity.trim())
                        }
                    }
                }

                textLogActivities.text = "Activities: ${activityList.joinToString(", ")}"

                Log.d("MoodHistoryAdapter", "Activities: ${activityList.joinToString(", ")}")

                textDate.text = moodData.created_at

                textNotes.text = "Notes: ${moodData.note ?: "No notes"}"
            }
        }
    }
}