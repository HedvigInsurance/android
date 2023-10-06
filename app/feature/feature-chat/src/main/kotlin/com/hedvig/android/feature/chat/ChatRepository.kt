package com.hedvig.android.feature.chat

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.toUpload
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.CacheHeaders
import com.apollographql.apollo3.cache.normalized.api.CacheKey
import com.apollographql.apollo3.cache.normalized.api.Record
import com.apollographql.apollo3.cache.normalized.api.TypePolicyCacheKeyGenerator
import com.apollographql.apollo3.cache.normalized.api.normalize
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import giraffe.ChatMessageIdQuery
import giraffe.ChatMessageSubscription
import giraffe.ChatMessagesQuery
import giraffe.GifQuery
import giraffe.SendChatFileResponseMutation
import giraffe.SendChatSingleSelectResponseMutation
import giraffe.SendChatTextResponseMutation
import giraffe.UploadFileMutation
import giraffe.fragment.ChatMessageFragment
import giraffe.type.ChatResponseBodyFileInput
import giraffe.type.ChatResponseBodySingleSelectInput
import giraffe.type.ChatResponseBodyTextInput
import giraffe.type.ChatResponseFileInput
import giraffe.type.ChatResponseSingleSelectInput
import giraffe.type.ChatResponseTextInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ChatRepository(
  val apolloClient: ApolloClient,
  private val fileService: FileService,
  private val context: Context,
) {
  private val messagesQuery: ChatMessagesQuery = ChatMessagesQuery()

  fun fetchChatMessages(): Flow<ApolloResponse<ChatMessagesQuery.Data>> {
    return apolloClient
      .query(messagesQuery)
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .watch(fetchThrows = true)
  }

  suspend fun messageIds(): ApolloResponse<ChatMessageIdQuery.Data> =
    apolloClient
      .query(ChatMessageIdQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .execute()

  fun subscribeToChatMessages(): Flow<ApolloResponse<ChatMessageSubscription.Data>> =
    apolloClient.subscription(ChatMessageSubscription()).toFlow()

  suspend fun sendChatMessage(
    id: String,
    message: String,
  ): Either<OperationResult.Error, SendChatTextResponseMutation.Data> {
    return apolloClient
      .mutation(
        SendChatTextResponseMutation(
          ChatResponseTextInput(
            id,
            ChatResponseBodyTextInput(message),
          ),
        ),
      )
      .safeExecute()
      .toEither()
      .onLeft { error ->
        logcat(LogPriority.ERROR, error.throwable) {
          "Chat: Replying through ChatViewModel (chat message) failed. Message:${error.message}"
        }
      }
  }

  suspend fun sendSingleSelect(
    id: String,
    value: String,
  ) = apolloClient.mutation(
    SendChatSingleSelectResponseMutation(
      ChatResponseSingleSelectInput(id, ChatResponseBodySingleSelectInput(value)),
    ),
  ).execute()

  @SuppressLint("Recycle")
  suspend fun uploadFileFromProvider(uri: Uri): Either<OperationResult.Error, UploadFileMutation.Data> {
    val mimeType = fileService.getMimeType(uri)
    val file = File(
      context.cacheDir,
      fileService.getFileName(uri)
        ?: "${UUID.randomUUID()}.${fileService.getFileExtension(uri.toString())}",
    ) // I hate this but it seems there's no other way
    return withContext(Dispatchers.IO) {
      val openInputStream = context.contentResolver.openInputStream(uri)
      if (openInputStream != null) {
        openInputStream.use { inputStream ->
          file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
          }
        }
      }
      return@withContext uploadFile(file, mimeType).onLeft { error ->
        logcat(LogPriority.ERROR, error.throwable) {
          "Chat: uploadFileFromProvider (image/file chosen) failed. Message:${error.message}"
        }
      }
    }
  }

  suspend fun uploadFile(uri: Uri): Either<OperationResult.Error, UploadFileMutation.Data> {
    return uploadFile(File(uri.path!!), fileService.getMimeType(uri))
      .onRight {
        logcat { "Chat: uploadFileInner (picture taken) succeeded." }
      }
      .onLeft { error ->
        logcat(LogPriority.ERROR, error.throwable) {
          "Chat: uploadFileInner (picture taken) failed. Message:${error.message}"
        }
      }
  }

  private suspend fun uploadFile(
    file: File,
    mimeType: String,
  ): Either<OperationResult.Error, UploadFileMutation.Data> {
    return apolloClient.mutation(UploadFileMutation(file.toUpload(mimeType))).safeExecute().toEither()
  }

  suspend fun sendFileResponse(
    id: String,
    key: String,
    uri: Uri,
  ): ApolloResponse<SendChatFileResponseMutation.Data> {
    val mimeType = fileService.getMimeType(uri)

    val input = ChatResponseFileInput(
      body = ChatResponseBodyFileInput(
        key = key,
        mimeType = mimeType,
      ),
      globalId = id,
    )

    val chatFileResponse = SendChatFileResponseMutation(input)

    return apolloClient.mutation(chatFileResponse).execute()
  }

  suspend fun searchGifs(query: String): ApolloResponse<GifQuery.Data> =
    apolloClient.query(GifQuery(query)).execute()

  suspend fun writeNewMessageToApolloCache(message: ChatMessageFragment) {
    /**
     * Using [com.apollographql.apollo3.cache.normalized.ApolloStore.accessCache] here to use the
     * [java.util.concurrent.locks.ReentrantReadWriteLock] which resides inside the
     * [com.apollographql.apollo3.cache.normalized.internal.DefaultApolloStore] to respect in the read/write lock as we
     * want to touch the cache internals. This should make it so that we can't make a modification which would override
     * a cache entry which was written in-between us fetching the previous cache and appending our new message to it.
     */
    val changedKeys = apolloClient.apolloStore.accessCache { cache ->
      /**
       * [nomalize] here acts as a way to go from a query response into the map of key to records that we would've
       * gotten back. We construct our own fake `ChatMessagesQuery.Data` object with the [message] fragment to get the
       * exact record we would've gotten if the query came in normally from the backend.
       */
      val records: Map<String, Record> = messagesQuery.normalize(
        data = ChatMessagesQuery.Data(
          listOf(
            ChatMessagesQuery.Message(
              __typename = message.__typename,
              globalId = message.globalId,
              fragments = ChatMessagesQuery.Message.Fragments(message),
            ),
          ),
        ),
        customScalarAdapters = apolloClient.customScalarAdapters,
        cacheKeyGenerator = TypePolicyCacheKeyGenerator,
      )
      // These were the old cache entries for the MESSAGES_QUERY_NAME query.
      val oldCachedMessageCacheKeys: Set<CacheKey> = cache
        .loadRecord(CacheKey.rootKey().key, CacheHeaders.NONE)
        ?.get(MESSAGES_QUERY_NAME)
        ?.cast<List<CacheKey>>()
        ?.toSet() ?: emptySet()
      // This inlcudes the one new message which we want to write to the cache
      val newCachedMessageCacheKeys: Set<CacheKey> = records
        .get(CacheKey.rootKey().key)
        ?.get(MESSAGES_QUERY_NAME)
        ?.cast<List<CacheKey>>()
        ?.toSet() ?: emptySet()

      // This includes all the existing messages + the new one. In needs to be *first* to show as the last message in
      // the chat which is inverted, goes from bottom to top.
      val newMessageKeys: Set<CacheKey> = newCachedMessageCacheKeys + oldCachedMessageCacheKeys

      /**
       * We create a new Record for the "QUERY_ROOT" entry in the cache. This will look something like:
       * ```
       * "QUERY_ROOT" : {
       *   "messages" : [
       *     CacheKey(Message:123123123)
       *     CacheKey(Message:234234234)
       *     CacheKey(Message:345345345)
       *   ]
       * }
       * ```
       */
      val queryRootRecordWithAllMessages: Record = Record(
        key = CacheKey.rootKey().key,
        fields = mapOf(MESSAGES_QUERY_NAME to newMessageKeys.toList()),
        mutationId = null,
      )

      // We take the original [records] which was going to be written to the cache, and we change what was going to be
      // written to the "QUERY_ROOT" entry by entering our own Record which we've enriched to include all the chat
      // messages.
      // We keep the original [records] since in there it also includes information about where the message itself will
      // be stored, along with the message body and the message header which have their own key. This will mean that
      // the final [alteredRecords] map will look something like:
      // ```
      // mapOf(
      //   "QUERY_ROOT" : {
      //     "messages" : [
      //       CacheKey(Message:123123123) // This is the new entry which we're now wring
      //       CacheKey(Message:234234234) // This and the one below were already in the cache
      //       CacheKey(Message:345345345) // Already was in the cache
      //     ]
      //   },
      //   "Message:123123123" : { // The new message we're storing
      //     "__typename" : Message
      //     "globalId" : 123123123
      //     "id" : free.chat.message
      //     "header" : CacheKey(Message:123123123.header) // reference to the header entry below
      //     "body" : CacheKey(Message:123123123.body) // reference to the body entry below
      //   },
      //   "Message:123123123.header" : { // The header of the new message we're storing
      //     "fromMyself" : true
      //     "statusMessage" : Tack för ditt meddelande. Vi svarar så snart som möjligt.
      //     "pollingInterval" : 1000
      //     "richTextChatCompatible" : true
      //   },
      //   "Message:123123123.body" : { // The body of the new message we're storing
      //     "__typename" : MessageBodyText
      //     "type" : text
      //     "text" : Hello, I would like some help with this.
      //     "keyboard" : DEFAULT
      //     "placeholder" : Aa
      //   }
      // )
      // ```
      val alteredRecords: Map<String, Record> = records.toMutableMap().apply {
        put(CacheKey.rootKey().key, queryRootRecordWithAllMessages)
      }

      // This should merge everything together nicely. The new message entries will get stored, and the entries for
      // QUERY_ROOT will be retained, and the "messages" one will be updated with the old messages + the new one.
      cache.merge(alteredRecords.values.toList(), CacheHeaders.NONE)
    }
    apolloClient.apolloStore.publish(changedKeys)
  }

  companion object {
    /**
     * The name of the query in the schema is "messages" so it's used as the key for the record saved inside QUERY_ROOT
     * for caching purposes.
     */
    const val MESSAGES_QUERY_NAME = "messages"
  }
}

private inline fun <reified T> Any?.cast() = this as T
