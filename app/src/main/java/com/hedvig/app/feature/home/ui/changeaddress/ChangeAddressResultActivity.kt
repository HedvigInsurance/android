package com.hedvig.app.feature.home.ui.changeaddress

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ChangeAddressResultActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChangeAddressResultScreen(
                result = intent.getParcelableExtra(RESULT)!!,
                navigateToHome = { startActivity(LoggedInActivity.newInstance(this, withoutHistory = true)) },
                navigateToChat = { startActivity(ChatActivity.newInstance(this)) }
            )
        }
    }

    sealed class Result : Parcelable {
        @Parcelize
        data class Success(
            val date: LocalDate,
        ) : Result()

        @Parcelize
        object UnderwritingLimitsHit : Result()
    }

    companion object {
        private const val RESULT = "RESULT"

        fun newInstance(context: Context, result: Result) = Intent(
            context,
            ChangeAddressResultActivity::class.java
        ).apply {
            putExtra(RESULT, result)
        }
    }
}

@Composable
fun ChangeAddressResultScreen(
    result: ChangeAddressResultActivity.Result,
    navigateToHome: () -> Unit,
    navigateToChat: () -> Unit,
) {
    MdcTheme {
        Surface(color = MaterialTheme.colors.background) {
            when (result) {
                is ChangeAddressResultActivity.Result.Success -> {
                    ITPB(
                        illustration = painterResource(R.drawable.illustration_move_success),
                        title = stringResource(R.string.moving_confirmation_success_title),
                        paragraph = stringResource(
                            R.string.moving_confirmation_success_paragraph,
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(result.date)
                        ),
                        onClick = navigateToHome,
                        buttonContent = {
                            Text(text = stringResource(R.string.moving_confirmation_success_button_text))
                        }
                    )
                }
                ChangeAddressResultActivity.Result.UnderwritingLimitsHit -> {
                    ITPB(
                        illustration = painterResource(R.drawable.illustration_helicopter),
                        title = stringResource(R.string.moving_uw_failure_title),
                        paragraph = stringResource(R.string.moving_uw_failure_paragraph),
                        onClick = navigateToChat,
                        buttonContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_chat_white),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.moving_uw_failure_button_text))
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChangeAddressSuccessPreview() {
    ChangeAddressResultScreen(
        ChangeAddressResultActivity.Result.Success(
            LocalDate.of(
                2021, 2, 23
            )
        ),
        {}, {}
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChangeAddressUnderwritingLimitsPreview() {
    ChangeAddressResultScreen(ChangeAddressResultActivity.Result.UnderwritingLimitsHit, {}, {})
}
