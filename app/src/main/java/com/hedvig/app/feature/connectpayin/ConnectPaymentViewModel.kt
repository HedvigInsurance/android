package com.hedvig.app.feature.connectpayin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.profile.ui.payment.PaymentRepository
import com.hedvig.app.util.LiveEvent
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

class ConnectPaymentViewModel(
    private val payinStatusRepository: PayinStatusRepository,
    private val paymentRepository: PaymentRepository,
    private val hAnalytics: HAnalytics,
) : ViewModel() {
    private val _navigationState = MutableLiveData<ConnectPaymentScreenState>()
    val navigationState: LiveData<ConnectPaymentScreenState> = _navigationState

    private val _readyToStart = MutableLiveData<Boolean>()
    val readyToStart: LiveData<Boolean> = _readyToStart

    val shouldClose = LiveEvent<Boolean>()

    fun navigateTo(screen: ConnectPaymentScreenState) {
        _navigationState.postValue(screen)
        if (screen is ConnectPaymentScreenState.Result) {
            if (screen.success) {
                hAnalytics.screenView(AppScreen.CONNECT_PAYMENT_SUCCESS)
                viewModelScope.launch {
                    runCatching {
                        payinStatusRepository.refreshPayinStatus()
                        paymentRepository.refresh()
                    }
                }
            } else {
                hAnalytics.screenView(AppScreen.CONNECT_PAYMENT_FAILED)
            }
        }
    }

    fun isReadyToStart() {
        _readyToStart.postValue(true)
    }

    fun setInitialNavigationDestination(screen: ConnectPaymentScreenState) {
        if (_navigationState.value == null) {
            _navigationState.postValue(screen)
        }
    }

    fun close() {
        shouldClose.postValue(true)
    }
}

sealed class ConnectPaymentScreenState {
    object Explainer : ConnectPaymentScreenState()
    data class Connect(
        val transitionType: TransitionType
    ) : ConnectPaymentScreenState()

    data class Result(val success: Boolean) : ConnectPaymentScreenState()
}

enum class TransitionType {
    NO_ENTER_EXIT_RIGHT,
    ENTER_LEFT_EXIT_RIGHT,
    ENTER_RIGHT_EXIT_RIGHT
}
