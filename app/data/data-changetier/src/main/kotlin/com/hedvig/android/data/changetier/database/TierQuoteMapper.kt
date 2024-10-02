package com.hedvig.android.data.changetier.database

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleDisplayItem
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.productvariant.ProductVariant

internal class TierQuoteMapper {
  fun quoteToDbModel(quote: TierDeductibleQuote): ChangeTierQuoteEntity {
    return ChangeTierQuoteEntity(
      id = quote.id,
      tier = mapTierToTierDbModel(quote.tier),
      deductible = quote.deductible?.let { mapDeductibleToDeductibleDbModel(it) },
      premium = mapUiMoneyToUiMoneyDbModel(quote.premium),
      displayItems = quote.displayItems.map { mapDisplayItemToDbModel(it) },
      productVariant = mapProductVariantToDbModel(quote.productVariant),
    )
  }

  fun dbModelToQuote(entity: ChangeTierQuoteEntity): TierDeductibleQuote {
    return TierDeductibleQuote(
      id = entity.id,
      tier = mapTierDbModelToTier(entity.tier),
      deductible = entity.deductible?.let { mapDeductibleDbModelToDeductible(it) },
      premium = mapUiMoneyDbModelToUiMoney(entity.premium),
      displayItems = entity.displayItems.map { mapDbModelToDisplayItem(it) },
      productVariant = mapProductVariantDbModelToProductVariant(entity.productVariant),
    )
  }

  private fun mapTierToTierDbModel(tier: Tier): TierDbModel {
    return TierDbModel(
      tierName = tier.tierName,
      tierLevel = tier.tierLevel,
      info = tier.info,
    )
  }

  private fun mapTierDbModelToTier(tierDbModel: TierDbModel): Tier {
    return Tier(
      tierName = tierDbModel.tierName,
      tierLevel = tierDbModel.tierLevel,
      info = tierDbModel.info,
    )
  }

  private fun mapDeductibleToDeductibleDbModel(deductible: Deductible): DeductibleDbModel {
    return DeductibleDbModel(
      deductibleAmount = deductible.deductibleAmount?.let { mapUiMoneyToUiMoneyDbModel(it) },
      deductiblePercentage = deductible.deductiblePercentage,
      description = deductible.description,
    )
  }

  private fun mapDeductibleDbModelToDeductible(deductibleDbModel: DeductibleDbModel): Deductible {
    return Deductible(
      deductibleAmount = deductibleDbModel.deductibleAmount?.let { mapUiMoneyDbModelToUiMoney(it) },
      deductiblePercentage = deductibleDbModel.deductiblePercentage,
      description = deductibleDbModel.description,
    )
  }

  private fun mapUiMoneyToUiMoneyDbModel(uiMoney: UiMoney): UiMoneyDbModel {
    return UiMoneyDbModel(
      amount = uiMoney.amount,
      currencyCode = uiMoney.currencyCode,
    )
  }

  private fun mapUiMoneyDbModelToUiMoney(uiMoneyDbModel: UiMoneyDbModel): UiMoney {
    return UiMoney(
      amount = uiMoneyDbModel.amount,
      currencyCode = uiMoneyDbModel.currencyCode,
    )
  }

  private fun mapDisplayItemToDbModel(item: ChangeTierDeductibleDisplayItem): ChangeTierDeductibleDisplayItemDbModel {
    return ChangeTierDeductibleDisplayItemDbModel(
      displayTitle = item.displayTitle,
      displaySubtitle = item.displaySubtitle,
      displayValue = item.displayValue,
    )
  }

  private fun mapDbModelToDisplayItem(
    dbModel: ChangeTierDeductibleDisplayItemDbModel,
  ): ChangeTierDeductibleDisplayItem {
    return ChangeTierDeductibleDisplayItem(
      displayTitle = dbModel.displayTitle,
      displaySubtitle = dbModel.displaySubtitle,
      displayValue = dbModel.displayValue,
    )
  }

  private fun mapProductVariantToDbModel(variant: ProductVariant): ProductVariantDbModel {
    return ProductVariantDbModel(
      displayName = variant.displayName,
      contractGroup = variant.contractGroup,
      contractType = variant.contractType,
      partner = variant.partner,
      perils = variant.perils,
      insurableLimits = variant.insurableLimits,
      documents = variant.documents,
      tierName = variant.tierName,
      tierNameLong = variant.tierNameLong,
    )
  }

  private fun mapProductVariantDbModelToProductVariant(variantDbModel: ProductVariantDbModel): ProductVariant {
    return ProductVariant(
      displayName = variantDbModel.displayName,
      contractGroup = variantDbModel.contractGroup,
      contractType = variantDbModel.contractType,
      partner = variantDbModel.partner,
      perils = variantDbModel.perils,
      insurableLimits = variantDbModel.insurableLimits,
      documents = variantDbModel.documents,
      tierName = variantDbModel.tierName,
      tierNameLong = variantDbModel.tierNameLong,
    )
  }
}
