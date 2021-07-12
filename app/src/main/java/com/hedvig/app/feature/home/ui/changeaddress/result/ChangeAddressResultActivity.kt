package com.hedvig.app.feature.home.ui.changeaddress.result

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.view.isVisible
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ChangeAddressResultActivityBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.apollo.CacheManager
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject

class ChangeAddressResultActivity : BaseActivity(R.layout.change_address_result_activity) {
    private val binding by viewBinding(ChangeAddressResultActivityBinding::bind)
    private val cacheManager: CacheManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val result = intent.getParcelableExtra<Result>(RESULT) ?: run {
            e { "Programmer error: Missing argument RESULT in ${this.javaClass.name}" }
            return
        }

        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }
            when (result) {
                is Result.Success -> {
                    toolbar.isVisible = false
                    image.setImageResource(R.drawable.illustration_move_success)
                    title.setText(R.string.moving_confirmation_success_title)
                    subtitle.text = if (result.date == null) {
                        getString(R.string.moving_confirmation_success_no_date_paragraph_copy)
                    } else {
                        getString(
                            R.string.moving_confirmation_success_paragraph,
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(result.date)
                        )
                    }

                    continueButton.setText(R.string.moving_confirmation_success_button_text)
                    continueButton.setHapticClickListener {
                        clearCacheAndShowLoggedIn()
                    }
                }
                Result.Error -> {
                    toolbar.isVisible = true
                    image.setImageResource(R.drawable.illustration_helicopter)
                    title.setText(R.string.moving_uw_failure_title)
                    subtitle.setText(R.string.moving_uw_failure_paragraph)
                    continueButton.setText(R.string.moving_uw_failure_button_text)
                    continueButton.setIconResource(R.drawable.ic_chat_white)
                    continueButton.setHapticClickListener {
                        startActivity(
                            ChatActivity.newInstance(
                                this@ChangeAddressResultActivity,
                                showClose = true
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        clearCacheAndShowLoggedIn()
    }

    private fun clearCacheAndShowLoggedIn() {
        cacheManager.clearCache()
        startActivity(
            LoggedInActivity.newInstance(
                this@ChangeAddressResultActivity,
                withoutHistory = true
            )
        )
    }

    sealed class Result : Parcelable {
        @Parcelize
        data class Success(
            val date: LocalDate?,
        ) : Result()

        @Parcelize
        object Error : Result()
    }

    companion object {
        private const val RESULT = "RESULT"
        fun newInstance(context: Context, result: Result) =
            Intent(context, ChangeAddressResultActivity::class.java).apply {
                putExtra(RESULT, result)
            }
    }
}
