package com.hedvig.android.feature.inapppurchase.ui.form

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Test

class ExtraBuildingValueTest {
  @Test
  fun `encode writes the backend's json shape`() {
    assertThat(ExtraBuildingValue(type = "GARAGE", area = 25, hasWaterConnected = false).encode())
      .isEqualTo("""{"type":"GARAGE","area":25,"hasWaterConnected":false}""")
  }

  @Test
  fun `decode reads what encode wrote`() {
    val building = ExtraBuildingValue(type = "SAUNA", area = 8, hasWaterConnected = true)

    assertThat(ExtraBuildingValue.decode(building.encode())).isEqualTo(building)
  }

  @Test
  fun `decode returns null for malformed json`() {
    assertThat(ExtraBuildingValue.decode("""{"type":"GARAGE","""")).isNull()
  }
}
