package com.gws.gws_mobile.ui.insight

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartModel
import com.github.AAChartModel.AAChartCore.AAChartCreator.AASeriesElement
import com.github.AAChartModel.AAChartCore.AAChartEnum.AAChartType
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

        val moodChart = binding.moodChart
        val isNightMode = (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val backgroundColor = if (isNightMode) "#4D4D4D" else "#FFFFFF"
        val aaChartModel = AAChartModel()
            .chartType(AAChartType.Spline)
            .title("Mood Chart")
            .subtitle("Weekly Mood")
            .backgroundColor(backgroundColor)
            .dataLabelsEnabled(true)
            .categories(arrayOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"))
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Bliss")
                        .data(arrayOf(5, 4, 4, 5, 5, 4, 5))
                        .color("#845ec2"),
                    AASeriesElement()
                        .name("Bright")
                        .data(arrayOf(4, 4, 3, 4, 3, 4, 4))
                        .color("#d65db1"),
                    AASeriesElement()
                        .name("Neutral")
                        .data(arrayOf(3, 3, 3, 3, 3, 3, 3))
                        .color("#ff6f91"),
                    AASeriesElement()
                        .name("Low")
                        .data(arrayOf(2, 2, 3, 2, 2, 3, 2))
                        .color("#ff9671"),
                    AASeriesElement()
                        .name("Crumble")
                        .data(arrayOf(1, 2, 1, 2, 1, 2, 1))
                        .color("#ffc75f")
                )
            )

        moodChart.aa_drawChartWithChartModel(aaChartModel)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
