package com.hedvig.android.feature.odyssey.di

import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.fileupload.UploadFileUseCase
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.LocationOption
import com.hedvig.android.feature.odyssey.step.audiorecording.AudioRecordingViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrence.DateOfOccurrenceViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationViewModel
import com.hedvig.android.feature.odyssey.step.fileupload.FileUploadViewModel
import com.hedvig.android.feature.odyssey.step.informdeflect.ConfirmEmergencyViewModel
import com.hedvig.android.feature.odyssey.step.location.LocationViewModel
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberViewModel
import com.hedvig.android.feature.odyssey.step.selectcontract.SelectContractViewModel
import com.hedvig.android.feature.odyssey.step.singleitem.SingleItemViewModel
import com.hedvig.android.feature.odyssey.step.singleitemcheckout.SingleItemCheckoutViewModel
import com.hedvig.android.feature.odyssey.step.singleitempayout.SingleItemPayoutViewModel
import com.hedvig.android.feature.odyssey.step.summary.ClaimSummaryViewModel
import com.hedvig.android.language.LanguageService
import kotlin.time.Clock
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val odysseyModule = module {
  viewModel<AudioRecordingViewModel> { (audioRecording: ClaimFlowDestination.AudioRecording) ->
    AudioRecordingViewModel(
      audioRecording = audioRecording,
      claimFlowRepository = get(),
    )
  }
  viewModel<PhoneNumberViewModel> { (initialPhoneNumber: String?) ->
    PhoneNumberViewModel(initialPhoneNumber, get())
  }
  viewModel<DateOfOccurrenceViewModel> { (dateOfOccurrence: ClaimFlowDestination.DateOfOccurrence) ->
    DateOfOccurrenceViewModel(
      dateOfOccurrence = dateOfOccurrence,
      claimFlowRepository = get<ClaimFlowRepository>(),
      languageService = get<LanguageService>(),
    )
  }
  viewModel<LocationViewModel> { (selectedLocation: String?, locationOptions: List<LocationOption>) ->
    LocationViewModel(selectedLocation, locationOptions, get())
  }
  viewModel<DateOfOccurrencePlusLocationViewModel> { parametersHolder ->
    val dateOfOccurrencePlusLocation: ClaimFlowDestination.DateOfOccurrencePlusLocation = parametersHolder.get()
    DateOfOccurrencePlusLocationViewModel(
      dateOfOccurrencePlusLocation = dateOfOccurrencePlusLocation,
      claimFlowRepository = get<ClaimFlowRepository>(),
      languageService = get<LanguageService>(),
    )
  }
  viewModel<SingleItemViewModel> { (singleItem: ClaimFlowDestination.SingleItem) ->
    SingleItemViewModel(
      singleItem,
      get<ClaimFlowRepository>(),
      get<Clock>(),
      get<LanguageService>(),
    )
  }
  viewModel<ClaimSummaryViewModel> { (summary: ClaimFlowDestination.Summary) ->
    ClaimSummaryViewModel(summary, get<ClaimFlowRepository>())
  }
  viewModel<SingleItemCheckoutViewModel> { (singleItemCheckout: ClaimFlowDestination.SingleItemCheckout) ->
    SingleItemCheckoutViewModel(singleItemCheckout)
  }
  viewModel<SingleItemPayoutViewModel> { (singleItemPayout: ClaimFlowDestination.SingleItemPayout) ->
    SingleItemPayoutViewModel(singleItemPayout, get<ClaimFlowRepository>())
  }
  viewModel<SelectContractViewModel> { (selectContract: ClaimFlowDestination.SelectContract) ->
    SelectContractViewModel(selectContract, get<ClaimFlowRepository>())
  }
  viewModel<ConfirmEmergencyViewModel> { (confirmEmergency: ClaimFlowDestination.ConfirmEmergency) ->
    ConfirmEmergencyViewModel(confirmEmergency, get<ClaimFlowRepository>())
  }
  viewModel<FileUploadViewModel> { (fileUpload: ClaimFlowDestination.FileUpload) ->
    FileUploadViewModel(
      uploadFileUseCase = get<UploadFileUseCase>(),
      fileService = get<FileService>(),
      targetUploadUrl = fileUpload.targetUploadUrl,
      files = fileUpload.uploads,
      claimFlowRepository = get<ClaimFlowRepository>(),
    )
  }
}
