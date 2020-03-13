package com.hedvig.app.feature.profile.ui.coinsured

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import androidx.lifecycle.Observer
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.CustomTypefaceSpan
import com.hedvig.app.util.extensions.compatFont
import com.hedvig.app.util.extensions.concat
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.activity_coinsured.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.viewmodel.ext.android.viewModel

class CoinsuredActivity : BaseActivity() {

    private val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coinsured)

        setupLargeTitle(
            R.string.PROFILE_COINSURED_TITLE,
            R.drawable.ic_back
        ) {
            onBackPressed()
        }

        loadData()
    }

    private fun loadData() {
        profileViewModel.data.observe(this, Observer { profileData ->
            loadingSpinner.remove()
            sphereContainer.show()
            textContainer.show()

            loadingAnimation.show()
            loadingAnimation.playAnimation()

            profileData?.insurance?.personsInHousehold?.let { personsInHousehold ->
                val label = resources.getString(R.string.PROFILE_COINSURED_QUANTITY_LABEL)
                val partOne = SpannableString("${personsInHousehold - 1}\n")
                val partTwo = SpannableString(label)
                partOne.setSpan(
                    CustomTypefaceSpan(compatFont(R.font.favorit_book)),
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                partTwo.setSpan(
                    AbsoluteSizeSpan(16, true),
                    0,
                    label.length,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )

                sphereText.text = partOne.concat(partTwo)
            }
        })
    }
}
