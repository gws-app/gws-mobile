package com.gws.gws_mobile.ui.insight.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gws.gws_mobile.databinding.ActivityDetailBinding
import com.gws.gws_mobile.ui.insight.InsightViewModel

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var insightViewModel: InsightViewModel
    private lateinit var detailViewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoading(true)

        val itemId = intent.getIntExtra("ITEM_ID", 0)
        val type = intent.getStringExtra("TYPE") ?: ""

        insightViewModel = ViewModelProvider(this).get(InsightViewModel::class.java)
        detailViewModel = ViewModelProvider(this).get(DetailViewModel::class.java)

        detailViewModel.insightData.observe(this, { data ->
            data?.let {
                binding.backButton.setOnClickListener{
                    finish()
                }
                binding.title.text = type
                binding.detailTittle.text = it.title
                binding.description.text = it.description
                Glide.with(this)
                    .load(it.image)
                    .apply(RequestOptions().transform(RoundedCorners(20)))
                    .into(binding.image)
                showLoading(false)

                binding.shareButton.setOnClickListener{
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "${binding.detailTittle.text}\n\n${binding.description.text}")
                    startActivity(Intent.createChooser(shareIntent, "Share via"))
                }
            }
        })

        if (detailViewModel.insightData.value == null) {
            if (type.isNotEmpty()) {
                when (type) {
                    "news" -> {
                        insightViewModel.fetchNewsById(itemId)
                    }
                    "recommendations" -> {
                        insightViewModel.fetchRecommendationById(itemId)
                    }
                    else -> {
                    }
                }
            }
        }

        insightViewModel.newsResponse.observe(this, { newsResponse ->
            newsResponse?.data?.let {
                detailViewModel.setInsightData(it.title.toString(), it.description.toString(),
                    it.image.toString()
                )
                showLoading(false)
            }
        })

        insightViewModel.recommendationResponse.observe(this, { recommendationResponse ->
            recommendationResponse?.data?.let {
                detailViewModel.setInsightData(it.title.toString(), it.description.toString(),
                    it.image.toString()
                )
                showLoading(false)
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
