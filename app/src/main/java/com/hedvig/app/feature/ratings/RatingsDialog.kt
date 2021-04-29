package com.hedvig.app.feature.ratings

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogRatingsBinding
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject

class RatingsDialog : DialogFragment() {
    private val tracker: RatingsTracker by inject()
    private val binding by viewBinding(DialogRatingsBinding::bind)
    private var choice: RatingsChoice? = null

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.dialog_ratings,
        parent,
        false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            no.setHapticClickListener {
                if (choice == null) {
                    tracker.doNotLikeApp()
                    choice = RatingsChoice.NO
                    paragraph.text = getString(R.string.RATINGS_DIALOG_BODY_FEEDBACK)
                    return@setHapticClickListener
                }
                when (choice) {
                    RatingsChoice.YES -> tracker.doNotRate()
                    RatingsChoice.NO -> tracker.noToFeedback()
                }
                dismissAndStore()
            }
            yes.setHapticClickListener {
                if (choice == null) {
                    tracker.likeApp()
                    choice = RatingsChoice.YES
                    paragraph.text = getString(R.string.RATINGS_DIALOG_BODY_RATE)
                    yes.text = getString(R.string.RATINGS_DIALOG_BODY_RATE_YES)
                    no.text = getString(R.string.RATINGS_DIALOG_BODY_RATE_NO)
                    return@setHapticClickListener
                }
                when (choice) {
                    RatingsChoice.YES -> {
                        tracker.rate()
                        requireContext().openPlayStore()
                        dismissAndStore()
                    }
                    RatingsChoice.NO -> {
                        tracker.yesToFeedback()
                        startActivity(
                            Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:android@hedvig.com?subject=Feedback")
                            }
                        )
                        dismissAndStore()
                    }
                }
            }
        }
    }

    private fun dismissAndStore() {
        requireContext()
            .getSharedPreferences(RATINGS_PREFERENCE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(HAS_SEEN_RATINGS_DIALOG, true)
            .apply()
        dialog?.dismiss()
    }

    companion object {
        const val RATINGS_PREFERENCE = "ratings_preferences"
        const val HAS_SEEN_RATINGS_DIALOG = "has_seen_ratings_dialog"
        const val TAG = "RatingsDialog"
        fun newInstance() = RatingsDialog()
    }

    enum class RatingsChoice {
        YES,
        NO
    }
}
