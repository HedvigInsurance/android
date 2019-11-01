package com.hedvig.app.feature.travel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.type.CreateLuggageClaimInput
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_luggage_claim.*
import org.koin.android.viewmodel.ext.android.viewModel

class LuggageClaimActivity : BaseActivity(R.layout.activity_luggage_claim) {
    private val travelViewModel: TravelViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        travelViewModel.claimCreationStatus.observe(lifecycleOwner = this) { status ->
            if (status == true) {
                makeToast("Successfully created luggage claim!")
            } else if (status == false) {
                makeToast("Failed to create luggage claim :(")
            }
        }

        createClaim.setHapticClickListener {
            travelViewModel.createLuggageClaim(
                CreateLuggageClaimInput
                    .builder()
                    .from(from.text.toString())
                    .to(to.text.toString())
                    .hoursDelayed(hoursDelayed.text.toString().toInt())
                    .build()
            )
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, LuggageClaimActivity::class.java)
    }
}
