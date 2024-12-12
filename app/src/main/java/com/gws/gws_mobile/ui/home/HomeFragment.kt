package com.gws.gws_mobile.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.gws.gws_mobile.databinding.FragmentHomeBinding
import com.gws.gws_mobile.ui.home.addmood.AddMoodActivity
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var moodHistoryAdapter: MoodHistoryAdapter
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(HomeViewModel::class.java)

        homeViewModel.quoteText.observe(viewLifecycleOwner) {
            binding.textViewQuotes.text = it
        }

        homeViewModel.quoteAuthor.observe(viewLifecycleOwner) {
            binding.textViewAuthorQuote.text = it
        }

        // Mengambil mood history dari database
        homeViewModel.fetchMoodHistory()

        moodHistoryAdapter = MoodHistoryAdapter(emptyList())
        binding.recyclerViewMoodHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMoodHistory.adapter = moodHistoryAdapter

        homeViewModel.moodHistory.observe(viewLifecycleOwner) { moodData ->
            moodData?.let {
                moodHistoryAdapter.updateData(it)
            }
        }

        Glide.with(this)
            .load("https://picsum.photos/300/200")
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.imageViewBackground)

        setupEmojiClickListeners()
        homeViewModel.fetchQuote()

        displayCurrentDateTime()

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

    @SuppressLint("SetTextI18n")
    private fun displayCurrentDateTime() {
        val currentDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            Instant.now().atZone(ZoneId.systemDefault()).format(formatter)
        } else {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            formatter.format(Date())
        }

        binding.textDateMood.text = currentDateTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
