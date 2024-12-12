package com.gws.gws_mobile.ui.home

import android.content.Intent
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
import com.gws.gws_mobile.helper.SharedPreferences
import com.gws.gws_mobile.ui.home.addmood.AddMoodActivity
import com.gws.gws_mobile.ui.login.LoginActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var moodHistoryAdapter: MoodHistoryAdapter

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
        val userId = SharedPreferences.getUserId(requireContext())

        // Mengambil mood history berdasarkan userId
        if (userId != null) {
            homeViewModel.fetchMoodHistory(userId)
        }

        moodHistoryAdapter = MoodHistoryAdapter(emptyList())
        binding.recyclerViewMoodHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMoodHistory.adapter = moodHistoryAdapter

        homeViewModel.moodHistory.observe(viewLifecycleOwner) { moodData ->
            moodData?.let {
                moodHistoryAdapter = MoodHistoryAdapter(it)
                binding.recyclerViewMoodHistory.adapter = moodHistoryAdapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
