package com.hedvig.app.feature.profile.ui.aboutapp

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Uses the generated licenses from the `jaredsburrows/gradle-license-plugin`
 */
class OpenSourceLicensesDialog : DialogFragment() {

  fun showLicenses(activity: AppCompatActivity) {
    val fragmentManager = activity.supportFragmentManager
    val fragmentTransaction = fragmentManager.beginTransaction()
    val previousFragment = fragmentManager.findFragmentByTag("dialog_licenses")
    if (previousFragment != null) {
      fragmentTransaction.remove(previousFragment)
    }
    fragmentTransaction.addToBackStack(null)

    show(fragmentManager, "dialog_licenses")
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val webView = WebView(requireActivity())
    webView.loadUrl("file:///android_asset/open_source_licenses.html")

    return MaterialAlertDialogBuilder(requireActivity())
      .setTitle(hedvig.resources.R.string.PROFILE_LICENSE_ATTRIBUTIONS_TITLE)
      .setView(webView)
      .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
      .create()
  }
}
