package com.hedvig.app.feature.adyen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.observe
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.ConnectPaymentExplainerFragmentBinding
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PostSignExplainerFragment : Fragment(R.layout.connect_payment_explainer_fragment) {
    private val model: ConnectPaymentViewModel by sharedViewModel()
    private val binding by viewBinding(ConnectPaymentExplainerFragmentBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.explainerButton.setHapticClickListener {
            model.navigateTo(ConnectPaymentScreenState.Connect)
        }

        model.readyToStart.observe(viewLifecycleOwner) { binding.explainerButton.isEnabled = it }
    }
}

class ConnectPaymentViewModel : ViewModel() {
    private val _navigationState = MutableLiveData<ConnectPaymentScreenState>()
    val navigationState: LiveData<ConnectPaymentScreenState> = _navigationState

    private val _readyToStart = MutableLiveData<Boolean>()
    val readyToStart: LiveData<Boolean> = _readyToStart

    val shouldClose = LiveEvent<Boolean>()

    fun navigateTo(screen: ConnectPaymentScreenState) {
        _navigationState.postValue(screen)
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
    object Connect : ConnectPaymentScreenState()
    data class Result(val success: Boolean) : ConnectPaymentScreenState()
}
