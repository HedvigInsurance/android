package com.hedvig.app.feature.home.ui.changeaddress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ChangeAddressResultActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChangeAddressResultScreen(intent.getParcelableExtra(RESULT)!!) }
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
fun ChangeAddressResultScreen(result: ChangeAddressResultActivity.Result) {
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
                    )
                }
                ChangeAddressResultActivity.Result.UnderwritingLimitsHit -> {
                    ITPB(
                        illustration = painterResource(R.drawable.illustration_forever),
                        title = "TODO",
                        paragraph = "TODO",
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
        )
    )
}

@Preview
@Composable
fun ChangeAddressUnderwritingLimitsPreview() {
    ChangeAddressResultScreen(ChangeAddressResultActivity.Result.UnderwritingLimitsHit)
}
