package com.hedvig.app.util.extensions

import androidx.fragment.app.Fragment

val Fragment.screenWidth: Int
    get() = requireActivity().screenWidth
