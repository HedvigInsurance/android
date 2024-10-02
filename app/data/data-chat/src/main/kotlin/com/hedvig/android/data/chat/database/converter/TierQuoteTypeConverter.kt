package com.hedvig.android.data.chat.database.converter

import androidx.room.TypeConverter
import com.hedvig.android.data.chat.database.ChangeTierDeductibleDisplayItemDbModel
import com.hedvig.android.data.chat.database.DeductibleDbModel
import com.hedvig.android.data.chat.database.ProductVariantDbModel
import com.hedvig.android.data.chat.database.TierDbModel
import com.hedvig.android.data.chat.database.UiMoneyDbModel
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariantPeril
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TierQuoteTypeConverter {
  @TypeConverter
  fun fromTierDbModel(model: TierDbModel): String {
    return Json.encodeToString(model)
  }

  @TypeConverter
  fun toTierDbModel(string: String): TierDbModel {
    return Json.decodeFromString(string)
  }

  @TypeConverter
  fun fromDeductibleDbModel(model: DeductibleDbModel): String {
    return Json.encodeToString(model)
  }

  @TypeConverter
  fun toDeductibleDbModel(string: String): DeductibleDbModel {
    return Json.decodeFromString(string)
  }

  @TypeConverter
  fun fromUiMoneyDbModel(model: UiMoneyDbModel): String {
    return Json.encodeToString(model)
  }

  @TypeConverter
  fun toUiMoneyDbModel(string: String): UiMoneyDbModel {
    return Json.decodeFromString(string)
  }

  @TypeConverter
  fun fromChangeTierDeductibleDisplayItemDbModel(models: List<ChangeTierDeductibleDisplayItemDbModel>): String {
    return Json.encodeToString(models)
  }

  @TypeConverter
  fun toChangeTierDeductibleDisplayItemDbModel(string: String): List<ChangeTierDeductibleDisplayItemDbModel> {
    return Json.decodeFromString(string)
  }

  @TypeConverter
  fun fromProductVariantDbModel(model: ProductVariantDbModel): String {
    return Json.encodeToString(model)
  }

  @TypeConverter
  fun toProductVariantDbModel(string: String): ProductVariantDbModel {
    return Json.decodeFromString(string)
  }

  @TypeConverter
  fun fromProductVariantPeril(model: ProductVariantPeril): String {
    return Json.encodeToString(model)
  }

  @TypeConverter
  fun toProductVariantPeril(string: String): ProductVariantPeril {
    return Json.decodeFromString(string)
  }

  @TypeConverter
  fun fromInsurableLimit(model: InsurableLimit): String {
    return Json.encodeToString(model)
  }

  @TypeConverter
  fun toInsurableLimit(string: String): InsurableLimit {
    return Json.decodeFromString(string)
  }

  @TypeConverter
  fun fromInsuranceVariantDocument(model: InsuranceVariantDocument): String {
    return Json.encodeToString(model)
  }

  @TypeConverter
  fun toInsuranceVariantDocument(string: String): InsuranceVariantDocument {
    return Json.decodeFromString(string)
  }
}
