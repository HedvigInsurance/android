package com.hedvig.app.feature.chat.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.hedvig.app.R
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.makeKeyboardAware
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.onChange
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

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    private val chatViewModel: ChatViewModel by sharedViewModel()

    private lateinit var onSelectGif: (String) -> Unit

    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(LayoutInflater.from(requireContext()).inflate(R.layout.send_gif_dialog, null))
        dialog.makeKeyboardAware()

        val emptyText = "\uD83D\uDC4B\n${getString(R.string.CHAT_GIPHY_PICKER_TEXT)}"
        dialog.emptyGifLabel.text = emptyText
        val noGifsText = "\uD83D\uDE45\u200D♀\n${getString(R.string.CHAT_GIPHY_PICKER_NO_SEARCH_TEXT)}"

        disposables += Observable.create<String> { emitter ->
            dialog.gifSearchField.onChange { emitter.onNext(it) }
        }
            .debounce(500, TimeUnit.MILLISECONDS, Schedulers.computation())
            .subscribe({ query ->
                if (query.isBlank()) {
                    return@subscribe
                }
                chatViewModel.searchGifs(query)
            }, { Timber.e(it) })
        val adapter = GifAdapter(requireContext(), sendGif = { url ->
            onSelectGif(url)
            dismiss()
        })
        dialog.gifRecyclerView.addOnScrollListener(adapter.recyclerViewPreloader)
        dialog.gifRecyclerView.adapter = adapter

        chatViewModel.gifs.observe(lifecycleOwner = this) { data ->
            data?.gifs?.let { gifs ->
                (dialog.gifRecyclerView.adapter as? GifAdapter)?.items = gifs.filterNotNull()
                if (gifs.isEmpty()) {
                    dialog.gifRecyclerView.remove()
                    dialog.emptyGifLabel.show()
                    dialog.emptyGifLabel.text = noGifsText
                    return@observe
                }

                dialog.gifRecyclerView.show()
                dialog.emptyGifLabel.remove()
            }
        }

        if (requireArguments().getBoolean(IS_KEYBOARD_SHOWN)) {
            dialog.gifSearchField.requestFocus()
        }

        return dialog
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
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
