package com.hedvig.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.debug.activity_poc_pie_chart.*

class PocPieChartActivity : AppCompatActivity(R.layout.activity_poc_pie_chart) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val segments = listOf(
            PieChartSegment(75f),
            PieChartSegment(25f)
        )

        pieChart.segments = segments
    }
}

data class PieChartSegment(
    val percentage: Float
)
