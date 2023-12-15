package com.masterlibya.app.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.masterlibya.app.databinding.ActivityTermsAndPrivacyBinding
import com.masterlibya.app.util.Constant

class TermsAndPrivacy : AppCompatActivity() {

    private lateinit var binding: ActivityTermsAndPrivacyBinding
    var urlToOpen = "https://avicennaenterprise.com/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsAndPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra("title")?.let{ binding.toolbar.title = it }
        urlToOpen = if (intent.getStringExtra("key") == "terms") {
            Constant.k_TERMS_OF_USE
        } else { Constant.k_PRIVACY_POLICY }

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        val settings = binding.webView.settings
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.minimumFontSize = 1
        settings.minimumLogicalFontSize = 1
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.GONE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }

        binding.webView.loadUrl(urlToOpen)

    }

}