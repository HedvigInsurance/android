package com.hedvig.app.feature.chat.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import com.hedvig.android.apollo.graphql.ChatMessagesQuery
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.app.R
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.databinding.ActivityChatBinding
import com.hedvig.app.feature.chat.ChatInputType
import com.hedvig.app.feature.chat.ParagraphInput
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.extensions.calculateNonFullscreenHeightDiff
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.handleSingleSelectLink
import com.hedvig.app.util.extensions.hasPermissions
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.showPermissionExplanationDialog
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.triggerRestartActivity
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import slimber.log.e
import java.io.File
import java.io.IOException

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

  private var currentPhotoPath: String? = null

  private var forceScrollToBottom = true

  private val cameraPermission = Manifest.permission.CAMERA

  private val cameraPermissionResultLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { permissionGranted ->
    if (permissionGranted) {
      startTakePicture()
    } else {
      showPermissionExplanationDialog(cameraPermission)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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
            title = com.adyen.checkout.dropin.R.string.error_dialog_title,
            message = com.adyen.checkout.dropin.R.string.component_error,
            positiveAction = {},
            negativeLabel = null,
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
      onBackPressed()
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
    chatViewModel.takePictureUploadOutcome.observe(this) {
      attachPickerDialog?.uploadingTakenPicture(false)
      currentPhotoPath?.let { File(it).delete() }
    }

    chatViewModel.networkError.observe(this) { networkError ->
      if (networkError == true) {
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
        if (hasPermissions(cameraPermission)) {
          startTakePicture()
        } else {
          cameraPermissionResultLauncher.launch(cameraPermission)
        }
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
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
      ?: run {
        e { "Could not getExternalFilesDir" }
        return
      }

    val tempTakenPhotoFile = try {
      File.createTempFile(
        "JPEG_${System.currentTimeMillis()}_",
        ".jpg",
        storageDir,
      ).apply {
        currentPhotoPath = absolutePath
      }
    } catch (ex: IOException) {
      e(ex) { "Error occurred while creating the photo file" }
      null
    }

    tempTakenPhotoFile?.also { file ->
      val photoURI: Uri = FileProvider.getUriForFile(
        this,
        getString(R.string.file_provider_authority),
        file,
      )
      takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
      startActivityForResult(
        takePictureIntent,
        TAKE_PICTURE_REQUEST_CODE,
      )
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      TAKE_PICTURE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
        currentPhotoPath?.let { tempFile ->
          attachPickerDialog?.uploadingTakenPicture(true)

          chatViewModel.uploadTakenPicture(Uri.fromFile(File(tempFile)))
        }
      }
    }
  }

  override fun finish() {
    super.finish()
    chatViewModel.onChatClosed()
    overridePendingTransition(R.anim.stay_in_place, R.anim.chat_slide_down_out)
  }

  companion object {

    private const val TAKE_PICTURE_REQUEST_CODE = 2371

    const val ACTIVITY_IS_IN_FOREGROUND = "chat_activity_is_in_foreground"
  }
}
