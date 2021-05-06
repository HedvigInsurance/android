package com.hedvig.app.feature.home.ui.changeaddress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.google.android.material.composethemeadapter.MdcTheme
import com.hedvig.app.BaseActivity

class ChangeAddressResultActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { ChangeAddressScreen() }
}

    companion object {
        fun newInstance(context: Context) = Intent(context, ChangeAddressResultActivity::class.java)
    }
}

@Composable
fun ChangeAddressScreen() {
    MdcTheme {
        Text("Hello world")
    }
}
