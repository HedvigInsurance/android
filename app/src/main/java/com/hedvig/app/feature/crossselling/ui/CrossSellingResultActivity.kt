package com.hedvig.app.feature.crossselling.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.hedvig.app.BaseActivity
import org.koin.android.ext.android.inject
import java.time.Clock

class CrossSellingResultActivity : BaseActivity() {

    private val clock: Clock by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crossSellingResult = intent.getParcelableExtra<CrossSellingResult>(CROSS_SELLING_RESULT)
            ?: throw IllegalArgumentException(
                "Programmer error: CROSS_SELLING_RESULT not provided to ${this.javaClass.name}"
            )

        setContent {
            CrossSellingResultScreen(crossSellingResult, clock)
        }
    }

    companion object {
        fun newInstance(context: Context, crossSellingResult: CrossSellingResult): Intent =
            Intent(context, CrossSellingResultActivity::class.java).apply {
                putExtra(CROSS_SELLING_RESULT, crossSellingResult)
            }

        private const val CROSS_SELLING_RESULT = "CROSS_SELLING_RESULT"
    }
}
