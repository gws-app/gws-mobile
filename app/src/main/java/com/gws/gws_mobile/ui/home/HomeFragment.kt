package com.gws.gws_mobile.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gws.gws_mobile.databinding.FragmentHomeBinding
import com.gws.gws_mobile.databinding.ItemCardQuotesBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val cardQuotesBinding = ItemCardQuotesBinding.bind(binding.cardQuotesInclude.root)

        homeViewModel.quoteText.observe(viewLifecycleOwner) {
            cardQuotesBinding.textViewQuotes.text = it
        }

        homeViewModel.quoteAuthor.observe(viewLifecycleOwner) {
            cardQuotesBinding.textViewAuthorQuote.text = it
        }

        homeViewModel.fetchQuote()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}