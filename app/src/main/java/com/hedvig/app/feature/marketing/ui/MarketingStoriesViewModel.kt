package com.hedvig.app.feature.marketing.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.MarketingStoriesQuery
import com.hedvig.app.feature.marketing.data.MarketingStoriesRepository
import com.hedvig.app.util.LiveEvent

abstract class MarketingStoriesViewModel : ViewModel() {
    abstract val marketingStories: LiveData<List<MarketingStoriesQuery.MarketingStory>>
    val page = LiveEvent<Int>()
    val paused = LiveEvent<Boolean>()
    val blurred = LiveEvent<Boolean>()
    fun startFirstStory() {
        page.value = 0
    }


    fun nextScreen(): Boolean {
        val currentStoryIndex = page.value ?: 0
        val nScreens = marketingStories.value?.size ?: return false
        if (currentStoryIndex + 1 > nScreens) {
            return false
        } else if (currentStoryIndex + 1 == nScreens) {
            blurred.value = true
        }
        page.value = currentStoryIndex + 1
        return true
    }

    fun previousScreen(): Boolean {
        val currentStoryIndex = page.value ?: 0
        if (currentStoryIndex - 1 < 0) {
            return false
        }
        page.value = currentStoryIndex - 1
        return true
    }

    fun pauseStory() {
        paused.value = true
    }

    fun resumeStory() {
        paused.value = false
    }

    fun unblur() {
        page.value = 0
        blurred.value = false
    }
}

class MarketingStoriesViewModelImpl(
    marketingStoriesRepository: MarketingStoriesRepository
) : MarketingStoriesViewModel() {
    override val marketingStories = MutableLiveData<List<MarketingStoriesQuery.MarketingStory>>()

    init {
        marketingStoriesRepository.fetchMarketingStories { stories ->
            marketingStories.postValue(stories)
        }
    }
}
