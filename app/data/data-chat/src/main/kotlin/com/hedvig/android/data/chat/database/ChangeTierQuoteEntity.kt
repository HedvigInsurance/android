package com.hedvig.android.data.chat.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.data.chat.database.converter.TierQuoteTypeConverter
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariantPeril
import kotlinx.serialization.Serializable

@Entity(tableName = "change_tier_quotes")
data class ChangeTierQuoteEntity(
  @PrimaryKey
  val id: String,
  @TypeConverters(TierQuoteTypeConverter::class)
  val tier: TierDbModel,
  @TypeConverters(TierQuoteTypeConverter::class)
  val deductible: DeductibleDbModel?,
  @TypeConverters(TierQuoteTypeConverter::class)
  val premium: UiMoneyDbModel,
  @TypeConverters(TierQuoteTypeConverter::class)
  val displayItems: List<ChangeTierDeductibleDisplayItemDbModel>,
  @TypeConverters(TierQuoteTypeConverter::class)
  val productVariant: ProductVariantDbModel,
)

@Serializable
@Entity
data class UiMoneyDbModel(
  val amount: Double,
  val currencyCode: UiCurrencyCode,
)

@Serializable
@Entity
data class ChangeTierDeductibleDisplayItemDbModel(
  val displayTitle: String,
  val displaySubtitle: String?,
  val displayValue: String,
)

@Serializable
@Entity
data class TierDbModel(
  val tierName: String,
  val tierLevel: Int,
  val info: String?,
)

@Serializable
@Entity
data class DeductibleDbModel(
  @TypeConverters(TierQuoteTypeConverter::class)
  val deductibleAmount: UiMoneyDbModel?,
  val deductiblePercentage: Int?,
  val description: String,
)

@Entity
data class ProductVariantDbModel(
  val displayName: String,
  val contractGroup: ContractGroup,
  val contractType: ContractType,
  val partner: String?,
  @TypeConverters(TierQuoteTypeConverter::class)
  val perils: List<ProductVariantPeril>,
  @TypeConverters(TierQuoteTypeConverter::class)
  val insurableLimits: List<InsurableLimit>,
  @TypeConverters(TierQuoteTypeConverter::class)
  val documents: List<InsuranceVariantDocument>,
  val tierName: String?,
  val tierNameLong: String?,
)
