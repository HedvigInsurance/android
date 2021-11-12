package com.hedvig.app.feature.genericauth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hedvig.app.BaseActivity
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
                    error = viewState.error,
                )
            }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, GenericAuthActivity::class.java)
    }
}
