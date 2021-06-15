package com.hedvig.app.util.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

val Fragment.screenWidth: Int
    get() = requireActivity().screenWidth

val Fragment.viewLifecycleScope
    get() = viewLifecycleOwner.lifecycleScope

fun Fragment.repeatOnViewLifeCycleLaunch(
    repeatOnLifeCycleState: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend () -> Unit
) {
    viewLifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(repeatOnLifeCycleState) {
            block()
        }
    }
}

fun FragmentTransaction.addToBackStack() = addToBackStack(null)
