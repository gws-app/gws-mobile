package com.gws.gws_mobile.ui.insight

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.gws.gws_mobile.api.response.NewsItem
import com.gws.gws_mobile.api.response.RecommendationsItem
import com.gws.gws_mobile.databinding.FragmentInsightBinding

class InsightFragment : Fragment() {

    private var _binding: FragmentInsightBinding? = null
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recommendationsAdapter: RecommendationsAdapter

    private val binding get() = _binding!!

    private val newsList = mutableListOf<NewsItem>()
    private val recommendationsList = mutableListOf<RecommendationsItem>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightBinding.inflate(inflater, container, false)
        val root: View = binding.root

        newsAdapter = NewsAdapter(newsList)
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.newsRecyclerView.adapter = newsAdapter

        recommendationsAdapter = RecommendationsAdapter(recommendationsList)
        binding.rekomendasiRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.rekomendasiRecyclerView.adapter = recommendationsAdapter

        val insightViewModel = ViewModelProvider(this).get(InsightViewModel::class.java)

        val requestBody = mapOf(
            "bliss" to 1,
            "bright" to null,
            "neutral" to 1,
            "low" to null,
            "crumble" to 1
        )

        insightViewModel.fetchDataIfNeeded(requestBody)

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
                val validNews = it.news?.filterNotNull() ?: emptyList()
                val validRecommendations = it.recommendations?.filterNotNull() ?: emptyList()

                newsList.clear()
                newsList.addAll(validNews)
                newsAdapter.notifyDataSetChanged()

                recommendationsList.clear()
                recommendationsList.addAll(validRecommendations)
                recommendationsAdapter.notifyDataSetChanged()
            }
        }

        setupMoodChart()
        return root
    }

    fun setupMoodChart() {
        val moodData = listOf("bliss", "bright", "neutral", "bliss", "crumble", "neutral", "bright") // Example data
        val categories = arrayOf("12/1", "12/2", "12/3", "12/4", "12/5", "12/6", "12/7")

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

            // Determine color based on the target mood
            val segmentColor = when (moodData[i + 1]) {
                "bliss" -> "#845ec2"
                "bright" -> "#d65db1"
                "neutral" -> "#00bfff"
                "low" -> "#ffc75f"
                "crumble" -> "#ff9671"
                else -> "#cccccc" // Default color
            }

            // Create a dataset for the segment
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
                        1f -> "😞 Crumble"
                        2f -> "😔 Low"
                        3f -> "🙂 Neutral"
                        4f -> "😊 Bright"
                        5f -> "😍 Bliss"
                        else -> ""
                    }
                }
            }
            axisMinimum = 1f // Start from 1
            axisMaximum = 5f // End at
            granularity = 1f
            textSize = 12f
        }
        lineChart.axisRight.isEnabled = false // Disable right y-axis

        // Additional chart settings
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
