package com.hedvig.app

import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.debug.activity_poc_pie_chart.*

class PocPieChartActivity : AppCompatActivity(R.layout.activity_poc_pie_chart) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val segments = listOf(
            PieChartSegment(80f, colorAttr(R.attr.colorSecondary)),
            PieChartSegment(10f, colorAttr(R.attr.colorSecondaryVariant))
        )

        pieChart.reveal(segments)
        pieChart.setHapticClickListener {
            pieChart.reveal(segments)
        }
    }
}

data class PieChartSegment(
    val percentage: Float,
    @ColorInt val color: Int
)
