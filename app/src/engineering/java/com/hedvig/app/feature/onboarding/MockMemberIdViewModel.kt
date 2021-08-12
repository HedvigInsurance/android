package com.hedvig.app.feature.onboarding

import com.hedvig.app.testdata.feature.onboarding.MEMBER_ID_DATA

class MockMemberIdViewModel : MemberIdViewModel() {
    init {
        load()
    }

    override fun load() {
        if (!shouldLoad) {
            shouldLoad = true
            _state.value = State.Error
            return
        }
        _state.value = State.Success(mockData.member.id!!)
    }

    companion object {
        var shouldLoad = true
        val mockData = MEMBER_ID_DATA
    }
}
