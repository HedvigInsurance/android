package com.hedvig.app.feature.marketing.data

import android.content.Context
import android.net.Uri
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.hedvig.android.owldroid.graphql.MarketingStoriesQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.BuildConfig
import com.hedvig.app.util.extensions.head
import com.hedvig.app.util.extensions.tail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MarketingStoriesRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context,
    private val cache: SimpleCache
) {

    fun fetchMarketingStories(completion: (result: List<MarketingStoriesQuery.MarketingStory>) -> Unit) {
        val marketingStoriesQuery = MarketingStoriesQuery.builder()
            .build()

        apolloClientWrapper.apolloClient
            .query(marketingStoriesQuery)
            .enqueue(object : ApolloCall.Callback<MarketingStoriesQuery.Data>() {

                override fun onStatusEvent(event: ApolloCall.StatusEvent) {
                    Timber.d("StatusEvent: %s", event.toString())
                }

                override fun onFailure(e: ApolloException) {
                    Timber.d("Failed to load marketing stories :(")
                }

                override fun onResponse(response: Response<MarketingStoriesQuery.Data>) {
                    val data = response.data()?.marketingStories
                    data?.let { cacheAssets(it, completion) } ?: handleNoMarketingStories()
                }
            })
    }

    private fun cacheAssets(
        data: List<MarketingStoriesQuery.MarketingStory>,
        completion: (result: List<MarketingStoriesQuery.MarketingStory>) -> Unit
    ) {
        data.tail.forEach { story ->
            story.asset?.let { GlobalScope.launch { cacheAsset(it) } }
        }

        data.head.asset?.let { GlobalScope.launch { cacheAsset(it) { completion(data) } } }
    }

    private fun handleNoMarketingStories() = Timber.e("No Marketing Stories")

    private suspend fun cacheAsset(asset: MarketingStoriesQuery.Asset, onEnd: (() -> Unit)? = null) =
        withContext(Dispatchers.IO) {
            try {
                val mimeType = asset.mimeType
                val url = asset.url
                when (mimeType) {
                    // TODO Figure out how to make this block the completion of the AsyncTask
                    "image/jpeg" -> Glide.with(context).load(Uri.parse(url)).preload()
                    "video/mp4", "video/quicktime" -> {
                        val dataSourceFactory = DefaultDataSourceFactory(
                            context,
                            Util.getUserAgent(
                                context,
                                BuildConfig.APPLICATION_ID
                            )
                        )
                        CacheUtil.cache(
                            DataSpec(Uri.parse(url)),
                            cache,
                            dataSourceFactory.createDataSource(),
                            null
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
            //To be sure let's launch this on main thread
            onEnd?.let { GlobalScope.launch(Dispatchers.Main) { it() } }
        }
}
