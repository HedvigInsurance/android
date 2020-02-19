package com.hedvig.app.feature.keygear.data

import android.content.Context
import android.net.Uri
import com.apollographql.apollo.api.FileUpload
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toChannel
import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.AddReceiptToKeyGearItemMutation
import com.hedvig.android.owldroid.graphql.CreateKeyGearItemMutation
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.graphql.UpdateKeyGearPriceAndDateMutation
import com.hedvig.android.owldroid.graphql.UploadFileMutation
import com.hedvig.android.owldroid.graphql.UploadFilesMutation
import com.hedvig.android.owldroid.type.AddReceiptToKeyGearItemInput
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.android.owldroid.type.S3FileInput
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.service.FileService
import com.hedvig.app.util.extensions.into
import e
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel
import org.threeten.bp.LocalDate
import java.io.File
import java.util.*

class KeyGearItemsRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val fileService: FileService,
    private val context: Context
) {
    private lateinit var keyGearItemQuery: KeyGearItemQuery

    fun keyGearItems() =
        apolloClientWrapper
            .apolloClient
            .query(KeyGearItemsQuery())
            .watcher()
            .toChannel()

    fun keyGearItem(id: String): Channel<Response<KeyGearItemQuery.Data>> {
        keyGearItemQuery = KeyGearItemQuery(id)
        return apolloClientWrapper.apolloClient.query(keyGearItemQuery).watcher().toChannel()
    }

    suspend fun updatePurchasePriceAndDateAsync(
        id: String,
        date: LocalDate,
        price: MonetaryAmountV2Input
    ) {
        val mutation = UpdateKeyGearPriceAndDateMutation(id, date, price)
        val response = apolloClientWrapper.apolloClient.mutate(mutation).toDeferred().await()
        val newPrice =
            response.data()?.updatePurchasePriceForKeyGearItem?.purchasePrice?.amount ?: return
        val newDate =
            response.data()?.updateTimeOfPurchaseForKeyGearItem?.timeOfPurchase ?: return

        val cachedData =
            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .read(keyGearItemQuery)
                .execute()


        cachedData.keyGearItem?.let { keyGearItem ->
            val newData = cachedData
                .toBuilder()
                .keyGearItem(
                    keyGearItem.toBuilder().fragments(
                        KeyGearItemQuery.KeyGearItem.Fragments(
                            keyGearItem.fragments.keyGearItemFragment.toBuilder().purchasePrice(
                                KeyGearItemFragment.PurchasePrice("MonetaryAmountV2", newPrice)
                            ).timeOfPurchase(newDate).build()
                        )
                    ).build()
                ).build()

            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .writeAndPublish(keyGearItemQuery, newData)
                .execute()
        }
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

    fun createKeyGearItemAsync(category: KeyGearItemCategory, files: List<S3FileInput>) =
        apolloClientWrapper.apolloClient.mutate(
            CreateKeyGearItemMutation(
                category,
                files
            )
        ).toDeferred()

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

        val s3file = S3FileInput.builder()
            .bucket(uploadData.uploadFile.bucket)
            .key(uploadData.uploadFile.key)
            .build()

        val addReceiptResult = apolloClientWrapper
            .apolloClient
            .mutate(AddReceiptToKeyGearItemMutation(AddReceiptToKeyGearItemInput.builder().itemId(itemId).file(s3file).build()))
            .toDeferred()
            .await()

        val addReceiptData = addReceiptResult.data()
        if (addReceiptData == null) {
            e { "Failed to add receipt" }
            return
        }

        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore()
            .read(keyGearItemQuery)
            .execute()

        cachedData?.keyGearItem?.let { keyGearItem ->
            val newData = cachedData
                .toBuilder()
                .keyGearItem(
                    keyGearItem
                        .toBuilder()
                        .fragments(KeyGearItemQuery.KeyGearItem.Fragments(addReceiptData.addReceiptToKeyGearItem.fragments.keyGearItemFragment))
                        .build()).build()

            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .writeAndPublish(keyGearItemQuery, newData)
                .execute()
        }
    }
}
