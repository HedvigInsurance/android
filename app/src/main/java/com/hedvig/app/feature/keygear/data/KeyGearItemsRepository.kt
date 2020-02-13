package com.hedvig.app.feature.keygear.data

import android.content.Context
import android.net.Uri
import com.apollographql.apollo.api.FileUpload
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.CreateKeyGearItemMutation
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.graphql.UploadFilesMutation
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.android.owldroid.type.S3FileInput
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.service.FileService
import com.hedvig.app.util.extensions.into
import kotlinx.coroutines.Deferred
import java.io.File
import java.util.UUID

class KeyGearItemsRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val fileService: FileService,
    private val context: Context
) {
    fun keyGearItems() = Rx2Apollo.from(
        apolloClientWrapper
            .apolloClient
            .query(KeyGearItemsQuery())
            .watcher()
    )

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
}
