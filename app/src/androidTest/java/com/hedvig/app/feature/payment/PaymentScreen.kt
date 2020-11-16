package com.hedvig.app.feature.payment

import android.view.View
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import org.hamcrest.Matcher

class PaymentScreen : Screen<PaymentScreen>() {
    val recycler = KRecyclerView({ withId(R.id.recycler) }, {
        itemType(::FailedPayments)
        itemType(::ConnectPayin)
    })

    class FailedPayments(parent: Matcher<View>) : KRecyclerItem<FailedPayments>(parent) {
        val paragraph = KTextView(parent) { withId(R.id.failedPaymentsParagraph) }
    }

    class ConnectPayin(parent: Matcher<View>) : KRecyclerItem<ConnectPayin>(parent) {
        val connect = KButton(parent) { withId(R.id.connectBankAccount) }
    }

    val trustlyConnectPayin = KIntent { hasComponent(TrustlyConnectPayinActivity::class.java.name) }
}
