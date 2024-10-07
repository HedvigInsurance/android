package com.hedvig.android.data.chat.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.data.chat.database.converter.TierQuoteTypeConverter
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.InsurableLimit.InsurableLimitType
import com.hedvig.android.data.productvariant.InsuranceVariantDocument.InsuranceDocumentType
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
  val tierDisplayName: String?,
)

@Serializable
@Entity
data class DeductibleDbModel(
  @TypeConverters(TierQuoteTypeConverter::class)
  val deductibleAmount: UiMoneyDbModel?,
  val deductiblePercentage: Int?,
  val description: String,
)

@Serializable
@Entity
data class ProductVariantDbModel(
  val displayName: String,
  val contractGroup: ContractGroup,
  val contractType: ContractType,
  val partner: String?,
  @TypeConverters(TierQuoteTypeConverter::class)
  val perils: List<ProductVariantPerilDBM>,
  @TypeConverters(TierQuoteTypeConverter::class)
  val insurableLimits: List<InsurableLimitDBM>,
  @TypeConverters(TierQuoteTypeConverter::class)
  val documents: List<InsuranceVariantDocumentDBM>,
  val tierName: String?,
  val tierNameLong: String?,
)

@Serializable
@Entity
data class ProductVariantPerilDBM(
  val id: String,
  val title: String,
  val description: String,
  val info: String,
  val covered: List<String>,
  val exceptions: List<String>,
  val colorCode: String?,
)

@Serializable
@Entity
data class InsurableLimitDBM(
  val label: String,
  val limit: String,
  val description: String,
  val type: InsurableLimitType,
)

@Serializable
@Entity
data class InsuranceVariantDocumentDBM(
  val displayName: String,
  val url: String,
  val type: InsuranceDocumentType,
)
