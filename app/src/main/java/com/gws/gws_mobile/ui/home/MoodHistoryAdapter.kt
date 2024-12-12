package com.gws.gws_mobile.ui.home

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R
import com.gws.gws_mobile.api.response.MoodDataResponse
import com.gws.gws_mobile.databinding.ItemMoodHistoryBinding
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

class MoodHistoryAdapter(moods: List<MoodDataResponse>) :
    RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

    private val moodList = moods.sortedByDescending { it.created_at?.let { it1 -> parseDate(it1) } }

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

                val activityList = moodData.activities?.values?.flatten()?.joinToString(", ") ?: "No activities logged"
                textLogActivities.text = "Logged Activities: $activityList"

                Log.d("MoodHistoryAdapter", "Activities: $activityList")

                val createdAt = moodData.created_at
                try {
                    val formattedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        Instant.parse(createdAt)
                            .atZone(ZoneId.systemDefault())
                            .format(formatter)
                    } else {
                        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        parser.timeZone = TimeZone.getTimeZone("UTC")
                        val date = parser.parse(createdAt)
                        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        formatter.format(date!!)
                    }
                    textDate.text = formattedDate
                } catch (e: Exception) {
                    textDate.text = "Invalid date"
                    Log.e("MoodHistoryAdapter", "Error parsing date", e)
                }

                textNotes.text = " Notes: ${moodData.note}"
            }
        }
    }

    private fun parseDate(dateString: String): Long? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Instant.parse(dateString).toEpochMilli()
            } else {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(dateString)
                date?.time
            }
        } catch (e: Exception) {
            Log.e("MoodHistoryAdapter", "Error parsing date for sorting", e)
            null
        }
    }
}

