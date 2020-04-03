package com.hedvig.app.feature.keygear.data

import android.content.Context
import android.net.Uri
import com.apollographql.apollo.api.FileUpload
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.AddReceiptToKeyGearItemMutation
import com.hedvig.android.owldroid.graphql.CreateKeyGearItemMutation
import com.hedvig.android.owldroid.graphql.DeleteKeyGearItemMutation
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.graphql.UpdateKeyGearItemNameMutation
import com.hedvig.android.owldroid.graphql.UpdateKeyGearPriceAndDateMutation
import com.hedvig.android.owldroid.graphql.UploadFileMutation
import com.hedvig.android.owldroid.graphql.UploadFilesMutation
import com.hedvig.android.owldroid.type.AddReceiptToKeyGearItemInput
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.android.owldroid.type.S3FileInput
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.service.FileService
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toLocaleString
import com.hedvig.app.util.extensions.into
import e
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate
import java.io.File
import java.util.*

class KeyGearItemsRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val fileService: FileService,
    private val context: Context
) {
    private lateinit var keyGearItemsQuery: KeyGearItemsQuery
    private lateinit var keyGearItemQuery: KeyGearItemQuery

    fun keyGearItems(): Flow<Response<KeyGearItemsQuery.Data>> {
        keyGearItemsQuery = KeyGearItemsQuery(defaultLocale(context).toLocaleString())

        return apolloClientWrapper
            .apolloClient
            .query(keyGearItemsQuery)
            .watcher()
            .toFlow()
    }

    fun keyGearItem(id: String): Flow<Response<KeyGearItemQuery.Data>> {
        keyGearItemQuery = KeyGearItemQuery(id, defaultLocale(context).toLocaleString())

        return apolloClientWrapper
            .apolloClient
            .query(keyGearItemQuery)
            .watcher()
            .toFlow()
    }

    suspend fun updatePurchasePriceAndDateAsync(
        id: String,
        date: LocalDate,
        price: MonetaryAmountV2Input
    ): KeyGearItemQuery.Data? {
        val response = apolloClientWrapper
            .apolloClient
            .mutate(UpdateKeyGearPriceAndDateMutation(id, date, price))
            .toDeferred()
            .await()

        val newPrice =
            response.data()?.updatePurchasePriceForKeyGearItem?.purchasePrice?.amount
                ?: return null
        val newDate =
            response.data()?.updateTimeOfPurchaseForKeyGearItem?.timeOfPurchase
                ?: return null
        val newValuation =
            response.data()?.updateTimeOfPurchaseForKeyGearItem?.fragments?.keyGearItemValuationFragment?.valuation
                ?: response.data()?.updatePurchasePriceForKeyGearItem?.fragments?.keyGearItemValuationFragment?.valuation
                ?: return null

        val cachedData =
            apolloClientWrapper
                .apolloClient
                .apolloStore
                .read(keyGearItemQuery)
                .execute()

        cachedData.keyGearItem?.let { keyGearItem ->
            val newData = cachedData
                .copy(
                    keyGearItem = keyGearItem
                        .copy(
                            fragments = KeyGearItemQuery.KeyGearItem.Fragments(
                                keyGearItem.fragments.keyGearItemFragment.copy(
                                    purchasePrice = KeyGearItemFragment.PurchasePrice(amount = newPrice),
                                    timeOfPurchase = newDate,
                                    fragments = keyGearItem
                                        .fragments
                                        .keyGearItemFragment
                                        .fragments
                                        .copy(
                                            keyGearItemValuationFragment = keyGearItem
                                                .fragments
                                                .keyGearItemFragment
                                                .fragments
                                                .keyGearItemValuationFragment
                                                .copy(
                                                    valuation = newValuation
                                                )
                                        )
                                )
                            )
                        )
                )

            apolloClientWrapper
                .apolloClient
                .apolloStore
                .writeAndPublish(keyGearItemQuery, newData)
                .execute()

            return newData
        }
        return null
    }

    fun uploadPhotosForNewKeyGearItemAsync(photos: List<Uri>): Deferred<Response<UploadFilesMutation.Data>> {
        val files = photos.map { photo ->
            val mimeType = fileService.getMimeType(photo)
            val file = File(
                context.cacheDir,
                fileService.getFileName(photo)
                    ?: "${UUID.randomUUID()}.${fileService.getFileExtension(photo.toString())}"
            ) // I hate this but it seems there's no other way
            context.contentResolver.openInputStream(photo)?.into(file)
            FileUpload(mimeType, file)
        }

        return apolloClientWrapper.apolloClient.mutate(UploadFilesMutation(files)).toDeferred()
    }

    suspend fun createKeyGearItemAsync(
        category: KeyGearItemCategory,
        files: List<S3FileInput>,
        physicalReferenceHash: String? = null,
        name: String? = null
    ): Response<CreateKeyGearItemMutation.Data> {
        val mutation = CreateKeyGearItemMutation(
            category = category,
            photos = files,
            languageCode = defaultLocale(context).toLocaleString(),
            physicalReferenceHash = Input.fromNullable(physicalReferenceHash),
            name = Input.fromNullable(name)
        )

        val result = apolloClientWrapper
            .apolloClient
            .mutate(mutation)
            .toDeferred()
            .await()

        val data = result.data()
        if (data == null) {
            e { "Failed to create new key gear item" }
            return result
        }

        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore
            .read(keyGearItemsQuery)
            .execute()

        val newKeyGearItems = cachedData.keyGearItems.toMutableList()
        if (
            !newKeyGearItems.any { it.fragments.keyGearItemFragment.id == data.createKeyGearItem.fragments.keyGearItemFragment.id }
            && !data.createKeyGearItem.fragments.keyGearItemFragment.deleted
        ) {
            newKeyGearItems.add(
                KeyGearItemsQuery.KeyGearItem(
                    "KeyGearItem",
                    KeyGearItemsQuery.KeyGearItem.Fragments(data.createKeyGearItem.fragments.keyGearItemFragment)
                )
            )
        }
        val newData = cachedData
            .copy(
                keyGearItems = newKeyGearItems
            )

        apolloClientWrapper
            .apolloClient
            .apolloStore
            .writeAndPublish(keyGearItemsQuery, newData)
            .execute()

        return result
    }

    suspend fun uploadReceipt(itemId: String, file: Uri) {
        val mimeType = fileService.getMimeType(file)
        val uploadFile = File(
            context.cacheDir,
            fileService.getFileName(file)
                ?: "${UUID.randomUUID()}.${fileService.getFileExtension(file.toString())}"
        )
        context.contentResolver.openInputStream(file)?.into(uploadFile)
        val uploadResult = apolloClientWrapper
            .apolloClient
            .mutate(UploadFileMutation(FileUpload(mimeType, uploadFile)))
            .toDeferred()
            .await()

        val uploadData = uploadResult.data()
        if (uploadData == null) {
            e { "Failed to upload photo" }
            return
        }

        val s3file = S3FileInput(
            bucket = uploadData.uploadFile.bucket,
            key = uploadData.uploadFile.key
        )

        val addReceiptResult = apolloClientWrapper
            .apolloClient
            .mutate(
                AddReceiptToKeyGearItemMutation(
                    AddReceiptToKeyGearItemInput(
                        itemId = itemId,
                        file = s3file
                    ),
                    defaultLocale(context).toLocaleString()
                )
            )
            .toDeferred()
            .await()

        val addReceiptData = addReceiptResult.data()
        if (addReceiptData == null) {
            e { "Failed to add receipt" }
            return
        }

        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore
            .read(keyGearItemQuery)
            .execute()

        cachedData?.keyGearItem?.let { keyGearItem ->
            val newData = cachedData
                .copy(
                    keyGearItem = keyGearItem
                        .copy(
                            fragments = KeyGearItemQuery.KeyGearItem.Fragments(addReceiptData.addReceiptToKeyGearItem.fragments.keyGearItemFragment)
                        )
                )

            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .writeAndPublish(keyGearItemQuery, newData)
                .execute()
        }
    }

    suspend fun updateItemName(itemId: String, name: String) {
        val mutation =
            UpdateKeyGearItemNameMutation(
                id = itemId,
                updatedName = Input.fromNullable(name)
            )
        val response = apolloClientWrapper.apolloClient.mutate(mutation).toDeferred().await()

        val newName = response.data()?.updateKeyGearItemName?.name

        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore
            .read(keyGearItemQuery)
            .execute()

        cachedData?.keyGearItem?.let { keyGearItem ->
            val newData = cachedData
                .copy(
                    keyGearItem = keyGearItem
                        .copy(
                            fragments = KeyGearItemQuery.KeyGearItem.Fragments(
                                keyGearItem.fragments.keyGearItemFragment.copy(
                                    name = newName
                                )
                            )
                        )
                )

            apolloClientWrapper
                .apolloClient
                .apolloStore
                .writeAndPublish(keyGearItemQuery, newData)
                .execute()
        }
    }

    suspend fun deleteItem(id: String) {
        val response = apolloClientWrapper
            .apolloClient
            .mutate(DeleteKeyGearItemMutation(id))
            .toDeferred()
            .await()

        if (response.hasErrors() || response.data()?.deleteKeyGearItem?.deleted == false) {
            e { "Failed to delete item" }
            return
        }

        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore
            .read(keyGearItemsQuery)
            .execute()

        val newKeyGearItems = cachedData.keyGearItems
            .filter { it.fragments.keyGearItemFragment.id != id }

        val newData = cachedData
            .copy(keyGearItems = newKeyGearItems)

        apolloClientWrapper
            .apolloClient
            .apolloStore
            .writeAndPublish(keyGearItemsQuery, newData)
            .execute()
    }
}
