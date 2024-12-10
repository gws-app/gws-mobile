package com.gws.gws_mobile.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gws.gws_mobile.databinding.FragmentHomeBinding

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

        homeViewModel.fetchQuote()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}