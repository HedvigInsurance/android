package com.hedvig.app.feature.marketing.ui

import androidx.lifecycle.Observer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.marketing.service.MarketingTracker
import com.hedvig.app.util.extensions.view.performOnTapHapticFeedback
import com.hedvig.app.util.extensions.view.show
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class StoryFragment : androidx.fragment.app.Fragment() {

    private val cache: SimpleCache by inject()
    private val tracker: MarketingTracker by inject()

    private val marketingStoriesViewModel: MarketingStoriesViewModel by sharedViewModel()

    private var player: SimpleExoPlayer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val position = arguments?.getInt(POSITION_KEY) ?: return View(context)

        val view = inflater.inflate(R.layout.page_marketing_story, container, false) as LinearLayout

        // FIXME Show something prettier. Zak will deliver a design for this
        val story = marketingStoriesViewModel.marketingStories.value?.get(position) ?: return View(context)

        val asset = story.asset
        val mimeType = asset?.mimeType
        val url = asset?.url ?: ""
        if (mimeType == "image/jpeg") {
            val playerView = view.findViewById<PlayerView>(R.id.story_video)
            view.removeView(playerView)
            val imageView = setupImageView(view, url)
            imageView.tag = position
        } else if (mimeType == "video/mp4" || mimeType == "video/quicktime") {
            val imageView = view.findViewById<ImageView>(R.id.story_image)
            view.removeView(imageView)
            val playerView = setupPlayerView(view, url, position)
            playerView.tag = position
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        player?.release()
    }

    private fun setupPlayerView(parentView: LinearLayout, url: String, position: Int): PlayerView {
        val playerView = parentView.findViewById<PlayerView>(R.id.story_video)
        player = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
        playerView.player = player

        val dataSourceFactory =
            DefaultHttpDataSourceFactory(
                Util.getUserAgent(
                    context,
                    BuildConfig.APPLICATION_ID
                )
            )
        val cacheDataSourceFactory = CacheDataSourceFactory(cache, dataSourceFactory)
        val mediaSource = ExtractorMediaSource.Factory(cacheDataSourceFactory).createMediaSource(Uri.parse(url))
        player?.prepare(mediaSource)
        player?.playWhenReady = false
        player?.volume = 0f
        playerView.show()
        setupTouchListeners(playerView)

        marketingStoriesViewModel.page.observe(this, Observer { page ->
            if (page == position) {
                player?.seekTo(0)
                player?.playWhenReady = true
            } else {
                player?.playWhenReady = false
            }
        })

        marketingStoriesViewModel.paused.observe(this, Observer { paused ->
            if (marketingStoriesViewModel.page.value != position) {
                return@Observer
            }

            paused?.let {
                player?.playWhenReady = !paused
            }
        })

        return playerView
    }

    private fun setupImageView(parentView: LinearLayout, url: String): ImageView {
        val imageView = parentView.findViewById<ImageView>(R.id.story_image)

        Glide
            .with(requireContext())
            .load(Uri.parse(url))
            .fitCenter()
            .into(imageView)

        imageView.show()
        setupTouchListeners(imageView)

        return imageView
    }

    private fun setupTouchListeners(view: View) {
        var isHolding = false
        val handler = Handler()
        val holding = Runnable {
            isHolding = true
            marketingStoriesViewModel.pauseStory()
            tracker.pause(arguments?.getInt(POSITION_KEY))
        }
        view.setOnTouchListener { _, event ->
            if (marketingStoriesViewModel.blurred.value != null && marketingStoriesViewModel.blurred.value == true) {
                return@setOnTouchListener false
            }
            if (event.action == MotionEvent.ACTION_DOWN) {
                handler.postDelayed(holding, 150)
            }
            if (event.action != MotionEvent.ACTION_UP) {
                return@setOnTouchListener true
            }
            handler.removeCallbacks(holding)
            if (isHolding) {
                isHolding = false
                marketingStoriesViewModel.resumeStory()
                return@setOnTouchListener true
            }
            val viewCoords = intArrayOf(0, 0)
            view.getLocationOnScreen(viewCoords)
            val x = event.x - viewCoords[0]
            val oneFourth = view.measuredWidth * 0.25
            if (x > oneFourth) {
                if (marketingStoriesViewModel.nextScreen()) {
                    tracker.nextScreen(arguments?.getInt(POSITION_KEY))
                    view.performOnTapHapticFeedback()
                }
            } else {
                if (marketingStoriesViewModel.previousScreen()) {
                    tracker.previousSreen(arguments?.getInt(POSITION_KEY))
                    view.performOnTapHapticFeedback()
                }
            }
            true
        }
    }

    companion object {
        const val POSITION_KEY = "POSITION"

        fun newInstance(position: Int): StoryFragment {
            val fragment = StoryFragment()
            val args = Bundle()
            args.putInt(POSITION_KEY, position)
            fragment.arguments = args
            return fragment
        }
    }
}
