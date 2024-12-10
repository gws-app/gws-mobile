package com.gws.gws_mobile.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gws.gws_mobile.databinding.FragmentHomeBinding
import com.gws.gws_mobile.databinding.ContainerHomeBinding

class HomeFragment : Fragment() {

    private var _fragmentBinding: FragmentHomeBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!

    private var _containerBinding: ContainerHomeBinding? = null
    private val containerBinding get() = _containerBinding!!

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Inflate the Fragment layout and get reference to the container binding
        _fragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        _containerBinding = ContainerHomeBinding.bind(fragmentBinding.root)

        // Observe LiveData for quote text and author
        homeViewModel.quoteText.observe(viewLifecycleOwner, Observer { quoteText ->
            containerBinding.textViewQuote.text = "\"$quoteText\""
        })

        homeViewModel.quoteAuthor.observe(viewLifecycleOwner, Observer { quoteAuthor ->
            containerBinding.textViewAuthor.text = "- $quoteAuthor"
        })

        // Fetch a quote when fragment is created
        homeViewModel.fetchQuote()

        return fragmentBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentBinding = null // Avoid memory leaks
        _containerBinding = null // Avoid memory leaks
    }
}
