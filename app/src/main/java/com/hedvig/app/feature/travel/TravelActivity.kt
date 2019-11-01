package com.hedvig.app.feature.travel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_travel.*

class TravelActivity : BaseActivity(R.layout.activity_travel) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        makeLuggageClaim.setHapticClickListener {
            startActivity(LuggageClaimActivity.newInstance(this))
        }
    }
    companion object {
        fun newInstance(context: Context) = Intent(context, TravelActivity::class.java)
    }
}
