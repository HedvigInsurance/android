package com.hedvig.android.feature.chat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.ImageLoader
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.core.common.android.remove
import com.hedvig.android.core.common.android.show
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.R
import com.hedvig.android.feature.chat.databinding.SendGifDialogBinding
import com.hedvig.android.feature.chat.legacy.makeKeyboardAware
import com.hedvig.android.feature.chat.legacy.onChange
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.concurrent.TimeUnit

internal class GifPickerBottomSheet : BottomSheetDialogFragment() {
  private val viewModel: ChatViewModel by activityViewModel()
  private val binding by viewBinding(SendGifDialogBinding::bind)
  private val imageLoader: ImageLoader by inject()

  private val disposables = CompositeDisposable()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? = inflater.inflate(R.layout.send_gif_dialog, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.apply {
      val emptyText = "\uD83D\uDC4B\n${getString(hedvig.resources.R.string.LABEL_SEARCH_GIF)}"
      emptyGifLabel.text = emptyText
      val noGifsText =
        "\uD83D\uDE45\u200D♀\n${getString(hedvig.resources.R.string.CHAT_GIPHY_PICKER_NO_SEARCH_TEXT)}"

      disposables += Observable.create<String> { emitter ->
        gifSearchField.onChange { emitter.onNext(it) }
      }
        .debounce(500, TimeUnit.MILLISECONDS, Schedulers.computation())
        .subscribe(
          { query ->
            if (query.isBlank()) {
              return@subscribe
            }
            viewModel.searchGifs(query)
          },
          { logcat(LogPriority.ERROR, it) { "gif search threw an error" } },
        )
      val adapter = GifAdapter(imageLoader) { url ->
        viewModel.respondWithGif(url)
        dismiss()
      }
      gifRecyclerView.adapter = adapter

      viewModel.gifs.observe(viewLifecycleOwner) { data ->
        data?.gifs?.let { gifs ->
          (gifRecyclerView.adapter as? GifAdapter)?.submitList(gifs.filterNotNull())
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
