package com.gws.gws_mobile.ui.home

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R
import com.gws.gws_mobile.database.mood.Mood
import com.gws.gws_mobile.databinding.ItemMoodHistoryBinding
import java.io.File
import java.io.IOException

import java.text.SimpleDateFormat
import java.util.*

class MoodHistoryAdapter(private var moods: List<Mood>) :
    RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

    private val activityList = mutableListOf<String>()

    interface OnResultListener {
        fun onResultReceived(result: String)
    }

    private var onResultListener: OnResultListener? = null

    fun setOnResultListener(listener: OnResultListener) {
        onResultListener = listener
    }

    fun updateData(newMoods: List<Mood>) {
        moods = newMoods.sortedByDescending { it.created_at }
        updateActivityList(newMoods)
        notifyDataSetChanged()

        val todayActivities = getTodayActivities()
        val result = """"Activities": "${todayActivities.joinToString(" | ")}""""

        onResultListener?.onResultReceived(result)
    }

    private fun updateActivityList(newMoods: List<Mood>) {
        activityList.clear()
        newMoods.forEach { mood ->
            val activityString = mood.activities.trim('{', '}')
            val activityItems = mutableListOf<String>()

            val keyValuePairs = activityString.split("],")
            keyValuePairs.forEach { pair ->
                val cleanedPair = if (!pair.endsWith("]")) "$pair]" else pair
                val splitPair = cleanedPair.split("=")
                if (splitPair.size == 2) {
                    val activitiesPart = splitPair[1].trim('[', ']')
                    val activities = activitiesPart.split(",")
                    activities.forEach { activity ->
                        activityItems.add(activity.trim())
                    }
                }
            }
            activityList.addAll(activityItems)
        }
    }

    private fun getTodayActivities(): List<String> {
        val today = getTodayDateString()
        return moods.filter { it.created_at.startsWith(today) }
            .flatMap { mood ->
                val activityString = mood.activities.trim('{', '}')
                val activityItems = mutableListOf<String>()

                val keyValuePairs = activityString.split("],")
                keyValuePairs.forEach { pair ->
                    val cleanedPair = if (!pair.endsWith("]")) "$pair]" else pair
                    val splitPair = cleanedPair.split("=")
                    if (splitPair.size == 2) {
                        val activitiesPart = splitPair[1].trim('[', ']')
                        val activities = activitiesPart.split(",")
                        activities.forEach { activity ->
                            activityItems.add(activity.trim())
                        }
                    }
                }
                activityItems
            }
    }

    private fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
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

        private var mediaPlayer: MediaPlayer? = null
        private var isPlaying = false
        private val handler = Handler(Looper.getMainLooper())

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

                // Menampilkan hanya aktivitas yang relevan untuk mood ini
                val activityString = moodData.activities.trim('{', '}')
                val activityItems = mutableListOf<String>()

                val keyValuePairs = activityString.split("],")
                keyValuePairs.forEach { pair ->
                    val cleanedPair = if (!pair.endsWith("]")) "$pair]" else pair
                    val splitPair = cleanedPair.split("=")
                    if (splitPair.size == 2) {
                        val activitiesPart = splitPair[1].trim('[', ']')
                        val activities = activitiesPart.split(",")
                        activities.forEach { activity ->
                            activityItems.add(activity.trim())
                        }
                    }
                }
                textLogActivities.text = "Activities: ${activityItems.joinToString(", ")}"
                textDate.text = moodData.created_at
                textNotes.text = "Notes: ${moodData.note ?: "No notes"}"

                if (!moodData.voice_note_url.isNullOrEmpty()) {
                    val filePath = moodData.voice_note_url
                    val audioFile = File(filePath)
                    if (audioFile.exists()) {
                        cardVoiceNotes.visibility = View.VISIBLE
                        buttonPlayPause.setImageResource(R.drawable.ic_play_circle_24)
                        initMediaPlayer(filePath)

                        buttonPlayPause.setOnClickListener {
                            if (isPlaying) {
                                pauseAudio()
                                buttonPlayPause.setImageResource(R.drawable.ic_play_circle_24)
                            } else {
                                playAudio()
                                buttonPlayPause.setImageResource(R.drawable.ic_play_circle_24)
                            }
                        }

                        seekbarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                if (fromUser) {
                                    mediaPlayer?.seekTo(progress)
                                    textCurrentTime.text = formatDuration(progress)
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                        })
                    } else {
                        cardVoiceNotes.visibility = View.GONE
                    }
                } else {
                    cardVoiceNotes.visibility = View.GONE
                }
            }
        }

        private fun initMediaPlayer(filePath: String) {
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer?.apply {
                    setDataSource(filePath)
                    prepare()
                    binding.seekbarAudio.max = duration
                    binding.textAudioDuration.text = formatDuration(duration)
                    binding.textCurrentTime.text = formatDuration(0)

                    setOnCompletionListener {
                        this@MoodViewHolder.isPlaying = false
                        binding.buttonPlayPause.setImageResource(R.drawable.ic_play_circle_24)
                        binding.seekbarAudio.progress = 0
                        binding.textCurrentTime.text = formatDuration(0)
                        handler.removeCallbacksAndMessages(null)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(binding.root.context, "Error initializing audio", Toast.LENGTH_SHORT).show()
            }
        }

        private fun playAudio() {
            mediaPlayer?.start()
            isPlaying = true
            updateSeekBar()
        }

        private fun pauseAudio() {
            mediaPlayer?.pause()
            isPlaying = false
            handler.removeCallbacksAndMessages(null)
        }

        private fun updateSeekBar() {
            handler.post(object : Runnable {
                override fun run() {
                    mediaPlayer?.let {
                        binding.seekbarAudio.progress = it.currentPosition
                        binding.textCurrentTime.text = formatDuration(it.currentPosition)
                        if (isPlaying) {
                            handler.postDelayed(this, 500)
                        }
                    }
                }
            })
        }

        private fun formatDuration(durationInMillis: Int): String {
            val totalSeconds = durationInMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        fun releaseMediaPlayer() {
            mediaPlayer?.let {
                if (isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null
            }
            handler.removeCallbacksAndMessages(null)
        }
    }

    override fun onViewRecycled(holder: MoodViewHolder) {
        super.onViewRecycled(holder)
        holder.releaseMediaPlayer()
    }
}
