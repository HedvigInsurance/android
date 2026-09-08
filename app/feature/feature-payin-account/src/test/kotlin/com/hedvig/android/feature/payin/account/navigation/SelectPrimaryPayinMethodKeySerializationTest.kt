package com.hedvig.android.feature.payin.account.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.payin.account.data.InvoiceDelivery
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test

internal class SelectPrimaryPayinMethodKeySerializationTest {
  private val json = Json {
    serializersModule = SerializersModule {
      polymorphic(HedvigNavKey::class) {
        subclass(SelectPrimaryPayinMethodKey::class)
        subclass(PayinMethodDetailsKey::class)
      }
    }
  }

  @Test
  fun `key carrying every payin account type survives a serialization round trip`() {
    val key = SelectPrimaryPayinMethodKey(
      currentMethods = listOf(
        PayinAccount.Trustly("8327", "91234124", "Swedbank", isPending = false, isDefault = true),
        PayinAccount.SwishPayin("0709901232", isPending = true, isDefault = false),
        PayinAccount.Invoice(InvoiceDelivery.Kivra, "a@b.com", isPending = false, isDefault = false),
        PayinAccount.Invoice(null, null, isPending = false, isDefault = false),
      ),
    )

    val encoded = json.encodeToString(PolymorphicSerializer(HedvigNavKey::class), key)

    assertThat(json.decodeFromString(PolymorphicSerializer(HedvigNavKey::class), encoded)).isEqualTo(key)
  }

  @Test
  fun `payin method details key survives a serialization round trip for every account type`() {
    val methods = listOf(
      PayinAccount.Trustly("8327", "91234124", "Swedbank", isPending = false, isDefault = true),
      PayinAccount.SwishPayin("0709901232", isPending = true, isDefault = false),
      PayinAccount.Invoice(InvoiceDelivery.Mail, "a@b.com", isPending = false, isDefault = false),
    )

    for (method in methods) {
      val key = PayinMethodDetailsKey(method)

      val encoded = json.encodeToString(PolymorphicSerializer(HedvigNavKey::class), key)

      assertThat(json.decodeFromString(PolymorphicSerializer(HedvigNavKey::class), encoded)).isEqualTo(key)
    }
  }
}
