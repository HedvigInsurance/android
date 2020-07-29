package com.hedvig.app.feature.embark

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.GenericDevelopmentAdapter
import com.hedvig.app.R
import com.hedvig.app.embarkModule
import com.hedvig.app.testdata.feature.embark.STANDARD_STORY
import com.hedvig.app.testdata.feature.embark.STORY_WITH_BINARY_REDIRECT
import com.hedvig.app.testdata.feature.embark.STORY_WITH_EQUALS_EXPRESSION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_GRAPHQL_QUERY_API
import com.hedvig.app.testdata.feature.embark.STORY_WITH_GREATER_THAN_EXPRESSION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_GREATER_THAN_OR_EQUALS_EXPRESSION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_INCOMPATIBLE_ACTION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_LESS_THAN_EXPRESSION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_LESS_THAN_OR_EQUALS_EXPRESSION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_MULTIPLE_REDIRECTS
import com.hedvig.app.testdata.feature.embark.STORY_WITH_NOT_EQUALS_EXPRESSION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_OR_EXPRESSION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_PASSED_KEY_VALUE
import com.hedvig.app.testdata.feature.embark.STORY_WITH_SELECT_ACTION_AND_CUSTOM_RESPONSE
import com.hedvig.app.testdata.feature.embark.STORY_WITH_TEMPLATE_MESSAGE
import com.hedvig.app.testdata.feature.embark.STORY_WITH_TEXT_ACTION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE
import com.hedvig.app.testdata.feature.embark.STORY_WITH_UNARY_EXPRESSIONS
import com.hedvig.app.testdata.feature.embark.STORY_WITH_UNARY_REDIRECT
import com.hedvig.app.util.jsonObjectOf
import kotlinx.android.synthetic.debug.activity_generic_development.*
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class EmbarkMockActivity : AppCompatActivity(R.layout.activity_generic_development) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unloadKoinModules(embarkModule)
        loadKoinModules(MOCK_MODULE)

        root.adapter = GenericDevelopmentAdapter(
            listOf(
                GenericDevelopmentAdapter.Item.Header("Embark Screen"),
                GenericDevelopmentAdapter.Item.ClickableItem("Loading") {
                    MockEmbarkViewModel.shouldLoad = false
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.Header("Select Action"),
                GenericDevelopmentAdapter.Item.ClickableItem("Regular") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STANDARD_STORY
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Custom Response") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_SELECT_ACTION_AND_CUSTOM_RESPONSE
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.Header("Text Action"),
                GenericDevelopmentAdapter.Item.ClickableItem("Regular") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_TEXT_ACTION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Custom Response") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.Header("Incompatible Action"),
                GenericDevelopmentAdapter.Item.ClickableItem("Open") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_INCOMPATIBLE_ACTION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.Header("Template Values"),
                GenericDevelopmentAdapter.Item.ClickableItem("Open") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_TEMPLATE_MESSAGE
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.Header("Message Expressions"),
                GenericDevelopmentAdapter.Item.ClickableItem("Unary (true/false)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_UNARY_EXPRESSIONS
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Equals (==)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_EQUALS_EXPRESSION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Not Equals (!=)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_NOT_EQUALS_EXPRESSION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Greater Than (>)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_GREATER_THAN_EXPRESSION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Greater Than or Equals (>=)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_GREATER_THAN_OR_EQUALS_EXPRESSION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Less Than (<)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_LESS_THAN_EXPRESSION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Less Than or Equals (<=)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_LESS_THAN_OR_EQUALS_EXPRESSION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Or (||)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_OR_EXPRESSION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("And (&&)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_OR_EXPRESSION
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.Header("Redirects"),
                GenericDevelopmentAdapter.Item.ClickableItem("Unary") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_UNARY_REDIRECT
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Binary") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_BINARY_REDIRECT
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Multiple") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_MULTIPLE_REDIRECTS
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Passed Expression (store a value in a key when a redirect triggers)") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_PASSED_KEY_VALUE
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                },
                GenericDevelopmentAdapter.Item.Header("Api"),
                GenericDevelopmentAdapter.Item.ClickableItem("GraphQL Query") {
                    MockEmbarkViewModel.apply {
                        shouldLoad = true
                        mockedData = STORY_WITH_GRAPHQL_QUERY_API
                        graphQLQueryResponse = jsonObjectOf("hello" to "world")
                    }
                    startActivity(EmbarkActivity.newInstance(this, this.javaClass.name))
                }
            )
        )
    }

    companion object {
        private val MOCK_MODULE = module {
            viewModel<EmbarkViewModel> { MockEmbarkViewModel() }
        }
    }
}
