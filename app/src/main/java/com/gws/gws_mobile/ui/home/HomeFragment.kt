package com.gws.gws_mobile.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.gws.gws_mobile.R
import com.gws.gws_mobile.api.response.MoodData
import com.gws.gws_mobile.databinding.FragmentHomeBinding
import com.gws.gws_mobile.ui.home.addmood.AddMoodActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.quoteText.observe(viewLifecycleOwner) {
            binding.textViewQuotes.text = it
        }

        homeViewModel.quoteAuthor.observe(viewLifecycleOwner) {
            binding.textViewAuthorQuote.text = it
        }
        // Ambil userId yang aktif atau yang sesuai
        val userId = "kirmanzz" // Ganti dengan userId yang valid

        // Mengambil mood history berdasarkan userId
        homeViewModel.fetchMoodHistory(userId)

        // Observasi mood history
        homeViewModel.moodHistory.observe(viewLifecycleOwner) { moodData ->
            // Karena moodData adalah list, kita bisa mengambil data pertama atau lebih
            if (moodData != null) {
                moodData.firstOrNull()?.let {
                    updateMoodCard(it)
                }
            }
        }
        Glide.with(this)
            .load("https://picsum.photos/300/200")
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.imageViewBackground)

        setupEmojiClickListeners()

        homeViewModel.fetchQuote()
        return root
    }

    private fun setupEmojiClickListeners() {
        val emojiButtons = listOf(
            binding.emojiButton1,
            binding.emojiButton2,
            binding.emojiButton3,
            binding.emojiButton4,
            binding.emojiButton5
        )
        val moodName = listOf("bliss", "bright", "neutral", "low", "crumble")
        emojiButtons.forEachIndexed { index, emojiButton ->
            emojiButton.setOnClickListener {
                val intent = Intent(requireContext(), AddMoodActivity::class.java)
                intent.putExtra("moodName", moodName[index])
                startActivity(intent)
            }
        }
    }
    private fun updateMoodCard(moodData: MoodData) {
        // Update emoji mood
        val emojiResource = when (moodData.emotion) {
            "bliss" -> R.drawable.ic_45_lupbgt
            "bright" -> R.drawable.ic_45_okegpp
            "neutral" -> R.drawable.ic_45_smile
            "low" -> R.drawable.ic_45_sad
            "crumble" -> R.drawable.ic_45_sadbed
            else -> R.drawable.ic_45_smile
        }

        binding.emojiMood.setImageResource(emojiResource)
        binding.textMoodName.text = moodData.emotion?.capitalize() ?: "Neutral"

        // Update activities log
        val activitiesText = "Logged Activities: ${moodData.activities}"
        binding.textLogActivities.text = activitiesText

        // Update date
        binding.textDate.text = "WED, DEC 24, 10:30 AM MST"  // Ganti dengan data aktual dari moodData jika ada

        // Update voice note info
        binding.textNotes.text = "Catatan: Aku ingin pentol"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
