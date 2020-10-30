package com.hedvig.app.feature.chat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.SendGifDialogBinding
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.util.extensions.makeKeyboardAware
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import e
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit

class GifPickerBottomSheet : BottomSheetDialogFragment() {
    private val model: ChatViewModel by sharedViewModel()
    private val binding by viewBinding(SendGifDialogBinding::bind)

    private lateinit var onSelectGif: (String) -> Unit

    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.send_gif_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            val emptyText = "\uD83D\uDC4B\n${getString(R.string.CHAT_GIPHY_PICKER_TEXT)}"
            emptyGifLabel.text = emptyText
            val noGifsText =
                "\uD83D\uDE45\u200D♀\n${getString(R.string.CHAT_GIPHY_PICKER_NO_SEARCH_TEXT)}"

            disposables += Observable.create<String> { emitter ->
                gifSearchField.onChange { emitter.onNext(it) }
            }
                .debounce(500, TimeUnit.MILLISECONDS, Schedulers.computation())
                .subscribe({ query ->
                    if (query.isBlank()) {
                        return@subscribe
                    }
                    model.searchGifs(query)
                }, { e(it) })
            val adapter = GifAdapter(requireContext(), sendGif = { url ->
                onSelectGif(url)
                dismiss()
            })
            gifRecyclerView.addOnScrollListener(adapter.recyclerViewPreloader)
            gifRecyclerView.adapter = adapter

            model.gifs.observe(viewLifecycleOwner) { data ->
                data?.gifs?.let { gifs ->
                    (gifRecyclerView.adapter as? GifAdapter)?.items = gifs.filterNotNull()
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
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        super.onCreateDialog(savedInstanceState).apply { makeKeyboardAware() }

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
