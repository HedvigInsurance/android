package com.hedvig.app.feature.embark

import com.hedvig.app.MockActivity
import com.hedvig.app.embarkModule
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.moreOptionsModule
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
import com.hedvig.app.testdata.feature.embark.STORY_WITH_FOUR_TOOLTIP
import com.hedvig.app.testdata.feature.embark.STORY_WITH_MANY_TOOLTIP
import com.hedvig.app.testdata.feature.embark.STORY_WITH_NOT_EQUALS_EXPRESSION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_OR_EXPRESSION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_PASSED_KEY_VALUE
import com.hedvig.app.testdata.feature.embark.STORY_WITH_SELECT_ACTION_AND_CUSTOM_RESPONSE
import com.hedvig.app.testdata.feature.embark.STORY_WITH_TEMPLATE_MESSAGE
import com.hedvig.app.testdata.feature.embark.STORY_WITH_TEXT_ACTION
import com.hedvig.app.testdata.feature.embark.STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE
import com.hedvig.app.testdata.feature.embark.STORY_WITH_SINGLE_TOOLTIP
import com.hedvig.app.testdata.feature.embark.STORY_WITH_TEXT_ACTION_SET
import com.hedvig.app.testdata.feature.embark.STORY_WITH_UNARY_EXPRESSIONS
import com.hedvig.app.testdata.feature.embark.STORY_WITH_UNARY_REDIRECT
import com.hedvig.app.util.jsonObjectOf
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class EmbarkMockActivity : MockActivity() {
    override val original = listOf(embarkModule, moreOptionsModule)
    override val mocks = listOf(module {
        viewModel<EmbarkViewModel> { MockEmbarkViewModel() }
        viewModel<MoreOptionsViewModel> { MockMoreOptionsViewModel() }
    })

    override fun adapter() = genericDevelopmentAdapter {
        header("Embark Screen")
        clickableItem("Loading") {
            MockEmbarkViewModel.shouldLoad = false
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        header("Select Action")
        clickableItem("Regular") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STANDARD_STORY
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Custom Response") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_SELECT_ACTION_AND_CUSTOM_RESPONSE
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        header("Text Action")
        clickableItem("Regular") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_TEXT_ACTION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Text action set") {
            MockEmbarkViewModel.mockedData = STORY_WITH_TEXT_ACTION_SET
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Custom Response") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        header("Incompatible Action")
        clickableItem("Open") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_INCOMPATIBLE_ACTION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        header("Template Values")
        clickableItem("Open") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_TEMPLATE_MESSAGE
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        header("Toolbar")
        clickableItem("Single Tooltip") {
            MockEmbarkViewModel.apply {
                mockedData = STORY_WITH_SINGLE_TOOLTIP
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("4 Tooltips") {
            MockEmbarkViewModel.apply {
                mockedData = STORY_WITH_FOUR_TOOLTIP
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Lots of Tooltips") {
            MockEmbarkViewModel.apply {
                mockedData = STORY_WITH_MANY_TOOLTIP
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        header("Message Expressions")
        clickableItem("Unary (true/false)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_UNARY_EXPRESSIONS
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Equals (==)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_EQUALS_EXPRESSION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Not Equals (!=)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_NOT_EQUALS_EXPRESSION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Greater Than (>)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_GREATER_THAN_EXPRESSION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Greater Than or Equals (>=)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_GREATER_THAN_OR_EQUALS_EXPRESSION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Less Than (<)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_LESS_THAN_EXPRESSION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Less Than or Equals (<=)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_LESS_THAN_OR_EQUALS_EXPRESSION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Or (||)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_OR_EXPRESSION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("And (&&)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_OR_EXPRESSION
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        header("Redirects")
        clickableItem("Unary") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_UNARY_REDIRECT
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Binary") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_BINARY_REDIRECT
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Multiple") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_MULTIPLE_REDIRECTS
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        clickableItem("Passed Expression (store a value in a key when a redirect triggers)") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_PASSED_KEY_VALUE
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        header("Api")
        clickableItem("GraphQL Query") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_GRAPHQL_QUERY_API
                graphQLQueryResponse = jsonObjectOf("hello" to "world")
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
        header("More Options")
        clickableItem("More Options Error") {
            MockEmbarkViewModel.mockedData = STANDARD_STORY
            MockMoreOptionsViewModel.shouldLoad = false
            startActivity(MoreOptionsActivity.newInstance(this@EmbarkMockActivity))
        }
    }
}
