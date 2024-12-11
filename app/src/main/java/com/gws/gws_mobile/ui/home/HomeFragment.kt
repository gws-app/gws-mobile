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

        emojiButtons.forEach { emojiButton ->
            emojiButton.setOnClickListener {
                val intent = Intent(requireContext(), AddMoodActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
