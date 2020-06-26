package com.hedvig.app.feature.referrals

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.GenericDevelopmentAdapter
import com.hedvig.app.R
import kotlinx.android.synthetic.debug.activity_mock_referrals.*

class ReferralsMockActivity : AppCompatActivity(R.layout.activity_mock_referrals) {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        root.adapter = GenericDevelopmentAdapter(
            listOf(
               
            )
        )
    }
}
