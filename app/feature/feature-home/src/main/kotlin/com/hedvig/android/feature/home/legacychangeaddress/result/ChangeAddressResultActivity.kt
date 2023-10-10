package com.hedvig.android.feature.home.legacychangeaddress.result

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.android.feature.home.R
import com.hedvig.android.feature.home.databinding.ChangeAddressResultActivityBinding
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ChangeAddressResultActivity : AppCompatActivity(R.layout.change_address_result_activity) {

  private val activityNavigator: ActivityNavigator by inject()
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    onBackPressedDispatcher.addCallback(this) {
      startLoggedIn()
    }

    val binding = ChangeAddressResultActivityBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    val result = intent.parcelableExtra<Result>(RESULT)
      ?: error("Programmer error: Missing argument RESULT in ${this.javaClass.name}")

    with(binding) {
      toolbar.setNavigationOnClickListener { finish() }
      when (result) {
        is Result.Success -> {
          toolbar.isVisible = false
          image.setImageResource(hedvig.resources.R.drawable.illustration_move_success)
          title.setText(hedvig.resources.R.string.moving_confirmation_success_title)
          subtitle.text = if (result.date == null) {
            getString(hedvig.resources.R.string.moving_confirmation_success_no_date_paragraph_copy)
          } else {
            getString(
              hedvig.resources.R.string.moving_confirmation_success_paragraph,
              DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(result.date),
            )
          }

          continueButton.setText(hedvig.resources.R.string.moving_confirmation_success_button_text)
          continueButton.setOnClickListener {
            startLoggedIn()
          }
        }
        Result.Error -> {
          toolbar.isVisible = true
          image.setImageResource(hedvig.resources.R.drawable.illustration_helicopter)
          title.setText(hedvig.resources.R.string.moving_uw_failure_title)
          subtitle.setText(hedvig.resources.R.string.moving_uw_failure_paragraph)
          continueButton.setText(hedvig.resources.R.string.moving_uw_failure_button_text)
          continueButton.setIconResource(hedvig.resources.R.drawable.ic_chat_white)
          continueButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(hedvigDeepLinkContainer.chat)))
          }
        }
      }
    }
  }

  private fun startLoggedIn() {
    activityNavigator.navigateToLoggedInScreen(this)
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
