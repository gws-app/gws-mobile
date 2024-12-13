package com.gws.gws_mobile.ui.insight.detail

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gws.gws_mobile.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var detailViewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoading(true)

        detailViewModel = ViewModelProvider(this).get(DetailViewModel::class.java)

        val newsTitle = intent.getStringExtra("NEWS_TITLE") ?: ""
        val newsDescription = intent.getStringExtra("NEWS_DESCRIPTION") ?: ""

        detailViewModel.setInsightData(newsTitle, newsDescription)

        detailViewModel.insightData.observe(this) { data ->
            data?.let {
                binding.title.text = "News Detail"
                binding.detailTittle.text = it.title
                binding.description.text = Html.fromHtml(it.description, Html.FROM_HTML_MODE_LEGACY)

                binding.backButton.setOnClickListener {
                    finish()
                }

                binding.shareButton.setOnClickListener {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, Html.fromHtml(it.toString(), Html.FROM_HTML_MODE_LEGACY).toString())
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share via"))
                }

                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
