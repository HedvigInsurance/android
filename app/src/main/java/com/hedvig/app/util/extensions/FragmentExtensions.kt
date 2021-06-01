package com.hedvig.app.util.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope

val Fragment.screenWidth: Int
    get() = requireActivity().screenWidth

val Fragment.viewLifecycleScope
    get() = viewLifecycleOwner.lifecycleScope

fun FragmentTransaction.addToBackStack() = addToBackStack(null)
