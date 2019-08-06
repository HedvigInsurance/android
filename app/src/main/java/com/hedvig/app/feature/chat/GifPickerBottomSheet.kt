package com.hedvig.app.feature.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.openKeyboard
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.send_gif_dialog.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit

class GifPickerBottomSheet : RoundedBottomSheetDialogFragment() {

    private val chatViewModel: ChatViewModel by sharedViewModel()

    private lateinit var onSelectGif: (String) -> Unit

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.send_gif_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Magic to make the bottom sheet not get hidden by the keyboard
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog?.setOnShowListener {
            val bottomSheet = (dialog as? BottomSheetDialog)?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            bottomSheet?.let {
                BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        val emptyText = "\uD83D\uDC4B\n${getString(R.string.CHAT_GIPHY_PICKER_TEXT)}"
        emptyGifLabel.text = emptyText
        val noGifsText = "\uD83D\uDE45\u200D♀\n${getString(R.string.CHAT_GIPHY_PICKER_NO_SEARCH_TEXT)}"

        disposables += Observable.create<String> { emitter ->
            gifSearchField.onChange { emitter.onNext(it) }
        }
            .debounce(500, TimeUnit.MILLISECONDS, Schedulers.computation())
            .subscribe({ query ->
                if (query.isBlank()) {
                    return@subscribe
                }
                chatViewModel.searchGifs(query)
            }, { Timber.e(it) })
        gifRecyclerView.adapter = GifAdapter(sendGif = { url ->
            onSelectGif(url)
            dismiss()
        })

        chatViewModel.gifs.observe(lifecycleOwner = this) { data ->
            data?.gifs?.let { gifs ->
                (gifRecyclerView.adapter as? GifAdapter)?.items = gifs
                if (gifs.isEmpty()) {
                    gifRecyclerView.remove()
                    emptyGifLabel.show()
                    emptyGifLabel.text = noGifsText
                    return@observe
                }

                gifRecyclerView.show()
                emptyGifLabel.remove()
            }
        }

        if (requireArguments().getBoolean(IS_KEYBOARD_SHOWN)) {
            gifSearchField.requestFocus()
            gifSearchField.openKeyboard()
        }
    }

    fun initialize(onSelectGif: (String) -> Unit) {
        this.onSelectGif = onSelectGif
    }

    companion object {
        const val TAG = "GifPickerBottomSheet"

        private const val IS_KEYBOARD_SHOWN = "is_keyboard_shown"

        fun newInstance(isKeyboardShown: Boolean) = GifPickerBottomSheet().apply {
            arguments = Bundle().apply {
                putBoolean(IS_KEYBOARD_SHOWN, isKeyboardShown)
            }
        }
    }
}
