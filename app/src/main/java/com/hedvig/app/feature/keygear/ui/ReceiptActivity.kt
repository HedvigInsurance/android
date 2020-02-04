package com.hedvig.app.feature.keygear.ui

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import kotlinx.android.synthetic.main.activity_receipt.*

class ReceiptActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        val url = "https://upload.wikimedia.org/wikipedia/commons/0/0b/ReceiptSwiss.jpg"

        close.setOnClickListener {
            onBackPressed()
        }

        share.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, url)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        Glide.with(this)
            .load(url)
            .into(receipt)
    }
}
