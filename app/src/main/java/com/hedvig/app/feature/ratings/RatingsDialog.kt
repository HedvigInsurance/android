package com.hedvig.app.feature.ratings

import android.app.Dialog
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
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.dialog_ratings.*
import org.koin.android.ext.android.inject

class RatingsDialog : DialogFragment() {
    private val tracker: RatingsTracker by inject()

    private var choice: RatingsChoice? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
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
            dialog?.dismiss()
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
                    dialog?.dismiss()
                }
                RatingsChoice.NO -> {
                    tracker.yesToFeedback()
                    startActivity(Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:android@hedvig.com?subject=Feedback")
                    })
                    dialog?.dismiss()
                }
            }
        }
    }

    companion object {
        const val TAG = "RatingsDialog"
        fun newInstance() = RatingsDialog()
    }

    enum class RatingsChoice {
        YES,
        NO
    }
}
