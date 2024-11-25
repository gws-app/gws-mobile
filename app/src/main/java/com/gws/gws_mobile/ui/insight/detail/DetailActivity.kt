package com.gws.gws_mobile.ui.insight.detail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.gws.gws_mobile.databinding.ActivityDetailBinding
import com.gws.gws_mobile.ui.insight.InsightViewModel

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var insightViewModel: InsightViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoading(true)

        val itemId = intent.getIntExtra("ITEM_ID", 0)
        val type = intent.getStringExtra("TYPE")

        insightViewModel = ViewModelProvider(this).get(InsightViewModel::class.java)

        when (type) {
            "news" -> {
                insightViewModel.fetchNewsById(itemId)
            }
            "recommendations" -> {
                insightViewModel.fetchRecommendationById(itemId)
            }
        }

        insightViewModel.newsResponse.observe(this, { newsResponse ->
            newsResponse?.data?.let {
                binding.titleTextView.text = it.title
                binding.descriptionTextView.text = it.description
                Glide.with(this)
                    .load(it.image)
                    .into(binding.imageView)
                showLoading(false)
            }
        })

        insightViewModel.recommendationResponse.observe(this, { recommendationResponse ->
            recommendationResponse?.data?.let {
                binding.titleTextView.text = it.title
                binding.descriptionTextView.text = it.description
                Glide.with(this)
                    .load(it.image)
                    .into(binding.imageView)
                showLoading(false)
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
