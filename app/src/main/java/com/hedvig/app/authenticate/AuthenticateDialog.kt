package com.hedvig.app.authenticate

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogAuthenticateBinding
import com.hedvig.app.util.QR
import com.hedvig.app.util.extensions.canOpenUri
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

abstract class AuthenticateDialog : DialogFragment() {

    val binding by viewBinding(DialogAuthenticateBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.dialog_authenticate, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
        }

    fun handleAutoStartToken(autoStartToken: String) {
        val autoStartUrl = "bankid:///?autostarttoken=$autoStartToken"
        val bankIdUri = Uri.parse("$autoStartUrl&redirect=null")
        if (requireContext().canOpenUri(bankIdUri)) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    bankIdUri
                )
            )
        } else {
            QR
                .with(requireContext())
                .load(autoStartUrl)
                .into(binding.qrCode)
        }
    }

    fun redirect() {
        val bankIdUri = Uri.parse("bankid://?redirectUrl=hedvig://")
        if (requireContext().canOpenUri(bankIdUri)) {
            val intent = Intent(Intent.ACTION_VIEW, bankIdUri)
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    companion object {
        const val TAG = "AuthenticateDialog"
    }
}
