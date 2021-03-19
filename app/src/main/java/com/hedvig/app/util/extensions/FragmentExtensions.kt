package com.hedvig.app.util.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope

val Fragment.screenWidth: Int
    get() = requireActivity().screenWidth

val Fragment.viewLifecycleScope
    get() = viewLifecycleOwner.lifecycle.coroutineScope
