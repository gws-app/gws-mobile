package com.gws.gws_mobile.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.gws.gws_mobile.databinding.FragmentHomeBinding
import com.gws.gws_mobile.ui.home.addmood.AddMoodActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
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
    private var uniqueActivities: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(HomeViewModel::class.java)

        homeViewModel.quoteText.observe(viewLifecycleOwner) {
            binding.textViewQuotes.text = it
            checkIfDataLoaded()
        }

        homeViewModel.quoteAuthor.observe(viewLifecycleOwner) {
            binding.textViewAuthorQuote.text = it
            checkIfDataLoaded()
        }

        homeViewModel.moodHistory.observe(viewLifecycleOwner) { moodData ->
            moodData?.let {
                moodHistoryAdapter.updateData(it)
                checkIfDataLoaded()
            }
        }

        showProgressIndicator()

        homeViewModel.fetchMoodHistory()
        homeViewModel.fetchQuote()

        moodHistoryAdapter = MoodHistoryAdapter(emptyList())
        moodHistoryAdapter.setOnResultListener(object : MoodHistoryAdapter.OnResultListener {
            override fun onResultReceived(result: String) {
                val activitiesString = result.substringAfter(":").trim().trim('"')
                val activitiesList = activitiesString
                    .split(" | ")
                    .map { it.trim().toLowerCase(Locale.getDefault()) }
                    .distinct()
                uniqueActivities = activitiesList

                saveActivitiesToJsonFile(uniqueActivities)
            }
        })

        binding.recyclerViewMoodHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMoodHistory.adapter = moodHistoryAdapter

        Glide.with(this)
            .load("https://picsum.photos/300/200")
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.imageViewBackground)

        setupEmojiClickListeners()
        displayCurrentDateTime()

        return root
    }

    private fun saveActivitiesToJsonFile(activitiesList: List<String>) {
        val fileName = "activities.json"
        val file = File(requireContext().filesDir, fileName)
        val jsonObject = JSONObject()
        jsonObject.put("activities", JSONArray(activitiesList))

        try {
            file.writeText(jsonObject.toString())
//            Toast.makeText(requireContext(), "Activities saved to $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save activities", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProgressIndicator() {
        binding.progressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        binding.progressIndicator.visibility = View.GONE
    }

    private fun checkIfDataLoaded() {
        if (homeViewModel.quoteText.value != null &&
            homeViewModel.quoteAuthor.value != null &&
            homeViewModel.moodHistory.value != null) {
            hideProgressIndicator()
        }
    }

    private fun setupEmojiClickListeners() {
        val emojiButtons = listOf(
            binding.emojiButton1,
            binding.emojiButton2,
            binding.emojiButton3,
            binding.emojiButton4,
            binding.emojiButton5
        )
        val moodNames = listOf("bliss", "bright", "neutral", "low", "crumble")
        emojiButtons.forEachIndexed { index, emojiButton ->
            emojiButton.setOnClickListener {
                val intent = Intent(requireContext(), AddMoodActivity::class.java)
                intent.putExtra("moodName", moodNames[index])
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
