package com.hedvig.app.feature.chat.ui

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.databinding.ActivityChatBinding
import com.hedvig.app.feature.chat.ChatInputType
import com.hedvig.app.feature.chat.ParagraphInput
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.extensions.calculateNonFullscreenHeightDiff
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.composeContactSupportEmail
import com.hedvig.app.util.extensions.handleSingleSelectLink
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.triggerRestartActivity
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.applyInsetter
import giraffe.ChatMessagesQuery
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import slimber.log.d
import slimber.log.e
import java.io.File

class ChatActivity : AppCompatActivity(R.layout.activity_chat) {
  private val chatViewModel: ChatViewModel by viewModel()
  private val binding by viewBinding(ActivityChatBinding::bind)

  private val imageLoader: ImageLoader by inject()
  private val logoutUseCase: LogoutUseCase by inject()

  private var keyboardHeight = 0
  private var systemNavHeight = 0
  private var navHeightDiff = 0
  private var isKeyboardBreakPoint = 0

  private var isKeyboardShown = false
  private var preventOpenAttachFile = false

  private var attachPickerDialog: AttachPickerDialog? = null
  private var forceScrollToBottom = true

  private var currentPhotoPath: String? = null

  val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { didSucceed ->
    d { "Take piture launcher result, didSucceed:$didSucceed, currentPhotoPath:$currentPhotoPath" }
    if (didSucceed) {
      currentPhotoPath?.let { tempFile ->
        attachPickerDialog?.uploadingTakenPicture(true)
        chatViewModel.uploadTakenPicture(Uri.fromFile(File(tempFile)))
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString("photo", currentPhotoPath)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState != null) {
      currentPhotoPath = savedInstanceState.getString("photo")
    }
    lifecycle.addObserver(AuthenticatedObserver())

    keyboardHeight = resources.getDimensionPixelSize(R.dimen.default_attach_file_height)
    isKeyboardBreakPoint =
      resources.getDimensionPixelSize(R.dimen.is_keyboard_brake_point_height)
    navHeightDiff = resources.getDimensionPixelSize(R.dimen.nav_height_div)

    chatViewModel.events
      .flowWithLifecycle(lifecycle)
      .onEach { event ->
        when (event) {
          ChatViewModel.Event.Restart -> {
            triggerRestartActivity(ChatActivity::class.java)
          }
          is ChatViewModel.Event.Error -> showAlert(
            title = hedvig.resources.R.string.something_went_wrong,
            message = hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE,
            positiveAction = {
              composeContactSupportEmail()
            },
            positiveLabel = hedvig.resources.R.string.GENERAL_EMAIL_US,
            negativeLabel = hedvig.resources.R.string.general_cancel_button,
          )
        }
      }
      .launchIn(lifecycleScope)

    binding.apply {
      window.compatSetDecorFitsSystemWindows(false)
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
    initializeKeyboardVisibilityHandler()
    observeData()
  }

  override fun onResume() {
    super.onResume()
    storeBoolean(ACTIVITY_IS_IN_FOREGROUND, true)
    forceScrollToBottom = true
  }

  override fun onPause() {
    storeBoolean(ACTIVITY_IS_IN_FOREGROUND, false)
    super.onPause()
  }

  override fun finish() {
    super.finish()
    chatViewModel.onChatClosed()
    overridePendingTransition(R.anim.stay_in_place, R.anim.chat_slide_down_out)
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
            logoutUseCase.invoke()
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
      this,
      onPressEdit = {
        showAlert(
          hedvig.resources.R.string.CHAT_EDIT_MESSAGE_TITLE,
          positiveLabel = hedvig.resources.R.string.CHAT_EDIT_MESSAGE_SUBMIT,
          negativeLabel = hedvig.resources.R.string.CHAT_EDIT_MESSAGE_CANCEL,
          positiveAction = {
            chatViewModel.editLastResponse()
          },
        )
      },
      imageLoader = imageLoader,
    )
    binding.messages.adapter = adapter
  }

  private fun initializeToolbarButtons() {
    binding.settings.setHapticClickListener {
      startActivity(SettingsActivity.newInstance(this))
    }
    binding.close.setOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }
    binding.close.contentDescription = getString(hedvig.resources.R.string.CHAT_CLOSE_DESCRIPTION)
    binding.close.show()
  }

  private fun initializeKeyboardVisibilityHandler() {
    binding.chatRoot.viewTreeObserver.addOnGlobalLayoutListener {
      val heightDiff = binding.chatRoot.calculateNonFullscreenHeightDiff()
      if (heightDiff > isKeyboardBreakPoint) {
        if (systemNavHeight > 0) systemNavHeight -= navHeightDiff
        this.keyboardHeight = heightDiff - systemNavHeight
        isKeyboardShown = true
        scrollToBottom(true)
      } else {
        systemNavHeight = heightDiff
        isKeyboardShown = false
      }
    }
  }

  private fun observeData() {
    chatViewModel.messages.observe(this) { data ->
      data?.let { bindData(it, forceScrollToBottom) }
    }
    // Maybe we should move the loading into the chatViewModel instead
    chatViewModel.sendMessageResponse.observe(this) { response ->
      if (response == true) {
        binding.input.clearInput()
      }
    }
    chatViewModel.takePictureUploadFinished.observe(this) {
      attachPickerDialog?.uploadingTakenPicture(false)
      currentPhotoPath?.let { File(it).delete() }
      currentPhotoPath = null
    }

    chatViewModel.networkError.observe(this) { networkError ->
      if (networkError) {
        showAlert(
          hedvig.resources.R.string.NETWORK_ERROR_ALERT_TITLE,
          hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE,
          hedvig.resources.R.string.NETWORK_ERROR_ALERT_TRY_AGAIN_ACTION,
          hedvig.resources.R.string.NETWORK_ERROR_ALERT_CANCEL_ACTION,
          positiveAction = {
            chatViewModel.load()
          },
        )
      }
    }

    chatViewModel.subscribe()
    chatViewModel.load()
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

  private fun bindData(data: ChatMessagesQuery.Data, forceScrollToBottom: Boolean) {
    var triggerScrollToBottom = false
    val firstMessage = data.messages.firstOrNull()?.let {
      ChatInputType.from(
        it,
      )
    }
    binding.input.message = firstMessage
    if (firstMessage is ParagraphInput) {
      triggerScrollToBottom = true
    }
    (binding.messages.adapter as? ChatAdapter)?.let {
      it.messages = data.messages.filterNotNull()
      val layoutManager = binding.messages.layoutManager as LinearLayoutManager
      val pos = layoutManager.findFirstCompletelyVisibleItemPosition()
      if (pos == 0) {
        triggerScrollToBottom = true
      }
    }
    if (triggerScrollToBottom || forceScrollToBottom) {
      scrollToBottom(false)
    }
  }

  private fun openAttachPicker() {
    val attachPickerDialog = AttachPickerDialog(this)
    attachPickerDialog.initialize(
      takePhotoCallback = {
        startTakePicture()
      },
      showUploadBottomSheetCallback = {
        ChatFileUploadBottomSheet
          .newInstance()
          .show(
            supportFragmentManager,
            ChatFileUploadBottomSheet.TAG,
          )
      },
      dismissCallback = { motionEvent ->
        motionEvent?.let {
          preventOpenAttachFile = true
          this.dispatchTouchEvent(motionEvent)
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
    GifPickerBottomSheet
      .newInstance(isKeyboardShown)
      .show(
        supportFragmentManager,
        GifPickerBottomSheet.TAG,
      )
  }

  private fun startTakePicture() {
    val externalPhotosDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: run {
      e { "Could not getExternalFilesDir(Environment.DIRECTORY_PICTURES)" }
      showAlert(
        title = hedvig.resources.R.string.something_went_wrong,
        positiveLabel = hedvig.resources.R.string.GENERAL_EMAIL_US,
        positiveAction = { composeContactSupportEmail() },
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
      this,
      "${BuildConfig.APPLICATION_ID}.provider",
      newPhotoFile,
    )
    takePictureLauncher.launch(newPhotoUri)
  }

  companion object {
    const val ACTIVITY_IS_IN_FOREGROUND = "chat_activity_is_in_foreground"
  }
}
