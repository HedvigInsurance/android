package com.hedvig.testutil

import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions

object CustomViewActions {
    fun swipeDownInCenter(): ViewAction = ViewActions.actionWithAssertions(
        GeneralSwipeAction(
            Swipe.FAST,
            GeneralLocation.CENTER,
            GeneralLocation.BOTTOM_CENTER,
            Press.FINGER
        )
    )
}
