package com.hedvig.android.feature.chat.ui

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.chat.ChatEnabledStatus
import com.hedvig.android.feature.chat.ChatEvent
import com.hedvig.android.feature.chat.ChatInputType
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.R
import com.hedvig.android.feature.chat.databinding.FragmentChatBinding
import com.hedvig.android.feature.chat.legacy.applyStatusBarInsets
import com.hedvig.android.feature.chat.legacy.calculateNonFullscreenHeightDiff
import com.hedvig.android.feature.chat.legacy.composeContactSupportEmail
import com.hedvig.android.feature.chat.legacy.show
import com.hedvig.android.feature.chat.legacy.showAlert
import com.hedvig.android.feature.chat.legacy.storeBoolean
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dev.chrisbanes.insetter.applyInsetter
import giraffe.ChatMessagesQuery
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class ChatFragment : Fragment(R.layout.fragment_chat) {
  private val chatViewModel: ChatViewModel by viewModel()
  private val binding by viewBinding(FragmentChatBinding::bind)

  private val imageLoader: ImageLoader by inject()
  private val hedvigBuildConstants: HedvigBuildConstants by inject()

  private var keyboardHeight = 0
  private var systemNavHeight = 0
  private var navHeightDiff = 0
  private var isKeyboardBreakPoint = 0

  private var isKeyboardShown = false
  private var preventOpenAttachFile = false

  private var attachPickerDialog: AttachPickerDialog? = null

  private var currentPhotoPath: String? = null

  val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { didSucceed ->
    logcat { "Take piture launcher result, didSucceed:$didSucceed, currentPhotoPath:$currentPhotoPath" }
    if (didSucceed) {
      currentPhotoPath?.let { tempFile ->
        attachPickerDialog?.uploadingTakenPicture(true)
        chatViewModel.uploadTakenPicture(Uri.fromFile(File(tempFile)))
      }
    }
  }

  private var keyboardHeightListener: (ViewTreeObserver.OnGlobalLayoutListener)? = null

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString("photo", currentPhotoPath)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (savedInstanceState != null) {
      currentPhotoPath = savedInstanceState.getString("photo")
    }
    lifecycle.addObserver(AuthenticatedObserver())

    keyboardHeightListener = ViewTreeObserver.OnGlobalLayoutListener {
      val binding = try {
        binding
      } catch (_: IllegalStateException) {
        // Ignore case when the fragment was already gone. This entire logic should be replaces by insets eventually
        return@OnGlobalLayoutListener
      }
      val heightDiff = binding.chatRoot.calculateNonFullscreenHeightDiff()
      if (heightDiff > isKeyboardBreakPoint) {
        if (systemNavHeight > 0) systemNavHeight -= navHeightDiff
        keyboardHeight = heightDiff - systemNavHeight
        isKeyboardShown = true
        scrollToBottom(true)
      } else {
        systemNavHeight = heightDiff
        isKeyboardShown = false
      }
    }
    binding.chatRoot.viewTreeObserver.addOnGlobalLayoutListener(keyboardHeightListener)

    keyboardHeight = resources.getDimensionPixelSize(R.dimen.default_attach_file_height)
    isKeyboardBreakPoint =
      resources.getDimensionPixelSize(R.dimen.is_keyboard_brake_point_height)
    navHeightDiff = resources.getDimensionPixelSize(R.dimen.nav_height_div)

    chatViewModel.events
      .flowWithLifecycle(lifecycle)
      .onEach { event ->
        when (event) {
          is ChatEvent.Error -> requireContext().showAlert(
            title = hedvig.resources.R.string.something_went_wrong,
            message = hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE,
            positiveAction = {
              requireContext().composeContactSupportEmail()
            },
            positiveLabel = hedvig.resources.R.string.GENERAL_EMAIL_US,
            negativeLabel = hedvig.resources.R.string.general_cancel_button,
          )
          is ChatEvent.RetryableNonDismissibleNetworkError -> {
            MaterialAlertDialogBuilder(requireContext()).apply {
              setTitle(resources.getString(hedvig.resources.R.string.NETWORK_ERROR_ALERT_TITLE))
              setPositiveButton(
                resources.getString(hedvig.resources.R.string.NETWORK_ERROR_ALERT_TRY_AGAIN_ACTION),
              ) { _, _ ->
                chatViewModel.retry()
              }
              setNegativeButton(
                resources.getString(android.R.string.cancel),
              ) { _, _ ->
                requireActivity().onBackPressedDispatcher.onBackPressed()
              }
              setMessage(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE)
              setCancelable(false)
            }.show()
          }
          ChatEvent.ClearTextFieldInput -> {
            binding.input.clearInput()
          }
        }
      }
      .launchIn(lifecycleScope)

    binding.apply {
      toolbar.applyStatusBarInsets()
      messages.applyStatusBarInsets()
      input.applyInsetter {
        type(navigationBars = true, ime = true) {
          padding(animated = true)
        }
        syncTranslationTo(binding.messages)
      }
    }

    initializeToolbarButtons()
    initializeMessages()
    initializeInput()
    observeData()

    lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        chatViewModel.chatEnabledStatus.collect { chatEnabledStatus ->
          if (chatEnabledStatus is ChatEnabledStatus.Enabled) {
            binding.disabledChatView.isGone = true
          }
        }
      }
    }
    binding.disabledChatView.setContent {
      val chatEnabledStatus = chatViewModel.chatEnabledStatus.collectAsStateWithLifecycle().value
      HedvigTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          if (chatEnabledStatus is ChatEnabledStatus.Disabled) {
            HedvigErrorSection(
              title = stringResource(
                when (chatEnabledStatus) {
                  ChatEnabledStatus.Disabled.FromFeatureFlag -> hedvig.resources.R.string.CHAT_DISABLED_MESSAGE
                  ChatEnabledStatus.Disabled.IsInDemoMode -> hedvig.resources.R.string.FEATURE_DISABLED_BY_DEMO_MODE
                },
              ),
              subTitle = null,
              buttonText = stringResource(hedvig.resources.R.string.general_close_button),
              retry = { requireActivity().onBackPressedDispatcher.onBackPressed() },
            )
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    requireContext().storeBoolean(ACTIVITY_IS_IN_FOREGROUND, true)
  }

  override fun onPause() {
    requireContext().storeBoolean(ACTIVITY_IS_IN_FOREGROUND, false)
    super.onPause()
  }

  private var navigateUp: (() -> Unit)? = null

  override fun onDestroyView() {
    chatViewModel.onChatClosed()
    navigateUp = null
    binding.chatRoot.viewTreeObserver.removeOnGlobalLayoutListener(keyboardHeightListener)
    keyboardHeightListener = null
    super.onDestroyView()
  }

  fun setNavigateUp(onNavigateUp: () -> Unit) {
    navigateUp = onNavigateUp
  }

  private fun initializeInput() {
    binding.input.initialize(
      sendTextMessage = { message ->
        scrollToBottom(true)
        chatViewModel.respondWithTextMessage(message)
      },
      sendSingleSelect = { value ->
        scrollToBottom(true)
        chatViewModel.respondWithSingleSelect(value)
      },
      sendSingleSelectLink = { value ->
        scrollToBottom(true)
        handleSingleSelectLink(
          value = value,
          onLinkHandleFailure = {
            logcat(LogPriority.ERROR) { "onLinkHandleFailure" }
          },
        )
      },
      openAttachFile = {
        scrollToBottom(true)
        if (!preventOpenAttachFile) {
          openAttachPicker()
        }
      },
      openSendGif = {
        scrollToBottom(true)
        openGifPicker()
      },
      chatRecyclerView = binding.messages,
    )
  }

  private fun initializeMessages() {
    val adapter = ChatAdapter(
      imageLoader = imageLoader,
    )
    binding.messages.adapter = adapter
  }

  private fun initializeToolbarButtons() {
    binding.close.setOnClickListener {
      if (navigateUp != null) {
        logcat { "Chat: navigateUp " }
        navigateUp!!.invoke()
      } else {
        logcat { "Chat: navigateUp not hooked up correctly" }
        requireActivity().onBackPressedDispatcher.onBackPressed()
      }
    }
    binding.close.contentDescription = getString(hedvig.resources.R.string.CHAT_CLOSE_DESCRIPTION)
    binding.close.show()
  }

  private fun observeData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        chatViewModel.messages.collect { data ->
          logcat(LogPriority.VERBOSE) { "ChatActivity, new messages" }
          data?.let { bindData(it) }
        }
      }
    }
    chatViewModel.takePictureUploadFinished.observe(viewLifecycleOwner) {
      attachPickerDialog?.uploadingTakenPicture(false)
      currentPhotoPath?.let { File(it).delete() }
      currentPhotoPath = null
    }
  }

  private fun scrollToBottom(smooth: Boolean) {
    if (smooth) {
      (binding.messages.layoutManager as LinearLayoutManager).smoothScrollToPosition(
        binding.messages,
        null,
        0,
      )
    } else {
      (binding.messages.layoutManager as LinearLayoutManager).scrollToPosition(0)
    }
  }

  private fun bindData(data: ChatMessagesQuery.Data) {
    val firstMessage = data.messages.firstOrNull()?.let {
      ChatInputType.from(it)
    }
    binding.input.message = firstMessage
    (binding.messages.adapter as? ChatAdapter)?.let {
      it.messages = data.messages.filterNotNull()
    }
    scrollToBottom(true)
  }

  private fun openAttachPicker() {
    val attachPickerDialog = AttachPickerDialog(requireContext())
    attachPickerDialog.initialize(
      takePhotoCallback = {
        startTakePicture()
      },
      showUploadBottomSheetCallback = {
        ChatFileUploadBottomSheet.newInstance()
          .show(
            parentFragmentManager,
            ChatFileUploadBottomSheet.TAG,
          )
      },
      dismissCallback = { motionEvent ->
        motionEvent?.let {
          preventOpenAttachFile = true
          requireActivity().dispatchTouchEvent(motionEvent)
        }

        binding.input.rotateFileUploadIcon(false)
        this.attachPickerDialog = null
      },
    )
    attachPickerDialog.pickerHeight = keyboardHeight
    attachPickerDialog.show()

    attachPickerDialog.setImages()

    binding.input.rotateFileUploadIcon(true)
    this.attachPickerDialog = attachPickerDialog
  }

  private fun openGifPicker() {
    GifPickerBottomSheet.newInstance(isKeyboardShown)
      .show(
        parentFragmentManager,
        GifPickerBottomSheet.TAG,
      )
  }

  private fun startTakePicture() {
    val externalPhotosDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: run {
      logcat(LogPriority.ERROR) { "Could not getExternalFilesDir(Environment.DIRECTORY_PICTURES)" }
      requireContext().showAlert(
        title = hedvig.resources.R.string.something_went_wrong,
        positiveLabel = hedvig.resources.R.string.GENERAL_EMAIL_US,
        positiveAction = { requireContext().composeContactSupportEmail() },
      )
      return
    }
    val newPhotoFile: File = File(
      externalPhotosDir,
      "JPEG_${System.currentTimeMillis()}.jpg",
    ).apply {
      currentPhotoPath = absolutePath
    }

    val newPhotoUri: Uri = FileProvider.getUriForFile(
      requireContext(),
      "${hedvigBuildConstants.appId}.provider",
      newPhotoFile,
    )
    takePictureLauncher.launch(newPhotoUri)
  }

  companion object {
    const val ACTIVITY_IS_IN_FOREGROUND = "chat_activity_is_in_foreground"
  }
}

private fun Fragment.handleSingleSelectLink(
  value: String,
  onLinkHandleFailure: () -> Unit,
) = when (value) {
  "message.forslag.dashboard" -> {
    logcat(LogPriority.ERROR) { "Can't handle going to the offer page without a QuoteCartId from link: `$value`" }
    AlertDialog.Builder(requireContext())
      .setTitle(com.adyen.checkout.dropin.R.string.error_dialog_title)
      .setMessage(getString(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE))
      .setPositiveButton(com.adyen.checkout.dropin.R.string.error_dialog_button) { _, _ ->
        // no-op. Action handled by `setOnDismissListener`
      }
      .setOnDismissListener {
        onLinkHandleFailure()
      }
      .create()
      .show()
  }
  "message.bankid.start", "message.bankid.autostart.respond", "message.bankid.autostart.respond.two" -> {
    logcat(LogPriority.ERROR) { "This used to open bankID, but we should never use the chat logged out anyway" }
    requireActivity().onBackPressedDispatcher.onBackPressed()
  }
  // bot-service is weird. it sends this when the user gets the option to go to `Hem`.
  // We simply dismiss the activity for now in this case
  "hedvig.com",
  "claim.done", "callme.phone.dashboard",
  -> {
    requireActivity().onBackPressedDispatcher.onBackPressed()
  }
  else -> {
    logcat(LogPriority.ERROR) { "Can't handle the link $value" }
  }
}
