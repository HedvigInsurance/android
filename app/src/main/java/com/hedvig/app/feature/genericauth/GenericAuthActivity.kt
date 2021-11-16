package com.hedvig.app.feature.genericauth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import org.koin.androidx.viewmodel.ext.android.viewModel

class GenericAuthActivity : BaseActivity() {
    val model: GenericAuthViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.compatSetDecorFitsSystemWindows(false)

        setContent {
            val viewState by model.viewState.collectAsState()
            HedvigTheme {
                EmailInputScreen(
                    onUpClick = ::finish,
                    onInputChanged = model::setInput,
                    onSubmitEmail = model::submitEmail,
                    onClear = model::clear,
                    onBlur = model::blur,
                    inputValue = viewState.input,
                    error = viewState.error?.let { errorMessage(it) },
                )
            }
        }
    }

    @Composable
    private fun errorMessage(error: GenericAuthViewModel.ViewState.TextFieldError) = stringResource(
        when (error) {
            GenericAuthViewModel.ViewState.TextFieldError.EMPTY ->
                R.string.login_text_input_email_error_enter_email
            GenericAuthViewModel.ViewState.TextFieldError.INVALID_EMAIL ->
                R.string.login_text_input_email_error_not_valid
        }
    )

    companion object {
        fun newInstance(context: Context) = Intent(context, GenericAuthActivity::class.java)
    }
}
