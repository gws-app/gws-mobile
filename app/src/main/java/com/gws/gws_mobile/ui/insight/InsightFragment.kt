package com.gws.gws_mobile.ui.insight

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.gws.gws_mobile.databinding.FragmentInsightBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class InsightFragment : Fragment() {

    private var _binding: FragmentInsightBinding? = null
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recommendationTagAdapter: RecommendationTagAdapter

    private val binding get() = _binding!!

    private val insightViewModel by lazy {
        ViewModelProvider(this).get(InsightViewModel::class.java)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightBinding.inflate(inflater, container, false)
        val root: View = binding.root

        newsAdapter = NewsAdapter(emptyList())
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.newsRecyclerView.adapter = newsAdapter

        recommendationTagAdapter = RecommendationTagAdapter(emptyList())
        binding.recommendationTagsRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recommendationTagsRecyclerView.adapter = recommendationTagAdapter

        insightViewModel.tags.observe(viewLifecycleOwner) { tagList ->
            if (tagList.isNullOrEmpty()) {
                binding.tvRecommendationsSubtitle.visibility = View.GONE
            } else {
                binding.tvRecommendationsSubtitle.visibility = View.VISIBLE
                recommendationTagAdapter.updateTags(tagList)
            }
        }


        setupObservers()
        insightViewModel.fetchNews()

        val activities = readActivitiesFromJsonFile()
        if (activities != null) {
            insightViewModel.fetchRecommendationTag(activities)
        } else {
            Toast.makeText(context, "Failed to read activities", Toast.LENGTH_LONG).show()
        }

        setupMoodChart()

        return root
    }

    private fun readActivitiesFromJsonFile(): JsonObject? {
        val fileName = "activities.json"
        val file = File(requireContext().filesDir, fileName)
        return if (file.exists()) {
            try {
                val jsonString = file.readText()
                val jsonObject = JsonParser.parseString(jsonString).asJsonObject

                val activitiesArray = jsonObject.getAsJsonArray("activities")
                val activitiesString = activitiesArray.joinToString(" | ") { it.asString }
                val activities = JsonObject().apply {
                    addProperty("activities", activitiesString)
                }
                activities
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }



    private fun setupObservers() {
        insightViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressIndicator.visibility = View.VISIBLE
                binding.contentContainer.visibility = View.GONE
            } else {
                binding.progressIndicator.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
            }
        }

        insightViewModel.response.observe(viewLifecycleOwner) { apiResponse ->
            apiResponse?.let {
                val validContents = it.contents?.filterNotNull() ?: emptyList()
                newsAdapter = NewsAdapter(validContents)
                binding.newsRecyclerView.adapter = newsAdapter
            }
        }

        insightViewModel.tags.observe(viewLifecycleOwner) { tags ->
            recommendationTagAdapter.updateTags(tags)
        }
    }

    private fun setupMoodChart() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val mostFrequentEmotions = insightViewModel.getMostFrequentEmotionPerDayLast7Days()
                val moodData = mostFrequentEmotions.map { it.emotion }
                val categories = mostFrequentEmotions.map {
                    val date = it.day

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val localDate = LocalDate.parse(date)
                        localDate.format(DateTimeFormatter.ofPattern("MM/dd"))
                    } else {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateObj = sdf.parse(date)
                        val outputFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                        outputFormat.format(dateObj)
                    }
                }

                val moodValues = mapOf(
                    "bliss" to 5f,
                    "bright" to 4f,
                    "neutral" to 3f,
                    "low" to 2f,
                    "crumble" to 1f
                )

                val dataSets = mutableListOf<ILineDataSet>()

                for (i in 0 until moodData.size - 1) {
                    val currentValue = moodValues[moodData[i]] ?: 0f
                    val nextValue = moodValues[moodData[i + 1]] ?: 0f

                    val segmentColor = when (moodData[i + 1]) {
                        "bliss" -> "#845ec2"
                        "bright" -> "#d65db1"
                        "neutral" -> "#00bfff"
                        "low" -> "#ffc75f"
                        "crumble" -> "#ff9671"
                        else -> "#cccccc"
                    }

                    val segmentEntries = listOf(
                        Entry(i.toFloat(), currentValue),
                        Entry((i + 1).toFloat(), nextValue)
                    )
                    val lineDataSet = LineDataSet(segmentEntries, null).apply {
                        color = Color.parseColor(segmentColor)
                        setDrawCircles(false)
                        setDrawValues(false)
                        lineWidth = 4f
                        mode = LineDataSet.Mode.LINEAR
                    }
                    dataSets.add(lineDataSet)
                }

                val lineChart = binding.moodChart
                val lineData = LineData(dataSets)
                lineChart.data = lineData
                lineChart.invalidate()

                lineChart.xAxis.apply {
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return categories.getOrElse(value.toInt()) { "" }
                        }
                    }
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textSize = 12f
                }

                lineChart.axisLeft.apply {
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when (value) {
                                1f -> "😞"
                                2f -> "😔"
                                3f -> "🙂"
                                4f -> "😊"
                                5f -> "😍"
                                else -> ""
                            }
                        }
                    }
                    axisMinimum = 1f
                    axisMaximum = 5f
                    granularity = 1f
                    textSize = 12f
                }
                lineChart.axisRight.isEnabled = false

                lineChart.setTouchEnabled(true)
                lineChart.setPinchZoom(true)
                lineChart.description.isEnabled = false
                lineChart.legend.isEnabled = false

            } catch (e: Exception) {
                Toast.makeText(context, "Error setting up mood chart", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
