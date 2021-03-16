package com.hedvig.app.feature.embark

import com.hedvig.app.MockActivity
import com.hedvig.app.embarkModule
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.onboarding.MockMoreOptionsViewModel
import com.hedvig.app.feature.onboarding.MoreOptionsViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.moreOptionsModule
import com.hedvig.app.testdata.feature.embark.data.PREVIOUS_INSURER_STORY
import com.hedvig.app.testdata.feature.embark.data.PROGRESSABLE_STORY
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import com.hedvig.app.testdata.feature.embark.data.STORY_WITCH_DATE_PICKER
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_BINARY_REDIRECT
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_COMPUTED_VALUE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_EQUALS_EXPRESSION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_FOUR_TOOLTIP
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_QUERY_API
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GREATER_THAN_EXPRESSION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GREATER_THAN_OR_EQUALS_EXPRESSION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_INCOMPATIBLE_ACTION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_LESS_THAN_EXPRESSION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_LESS_THAN_OR_EQUALS_EXPRESSION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_MANY_TOOLTIP
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_MULTIPLE_REDIRECTS
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_NOT_EQUALS_EXPRESSION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_NUMBER_ACTION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_OR_EXPRESSION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_PASSED_KEY_VALUE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_SELECT_ACTION_AND_CUSTOM_RESPONSE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_SINGLE_TOOLTIP
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEMPLATE_MESSAGE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_BIRTH_DATE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_BIRTH_DATE_REVERSE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_EMAIL_VALIDATION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_NORWEGIAN_POSTAL_CODE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_PERSONAL_NUMBER
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_SET
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_SWEDISH_POSTAL_CODE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_UNARY_EXPRESSIONS
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_UNARY_REDIRECT
import com.hedvig.app.util.jsonObjectOf
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class EmbarkMockActivity : MockActivity() {
    override val original = listOf(embarkModule, moreOptionsModule)
    override val mocks = listOf(module {
        viewModel<EmbarkViewModel> { MockEmbarkViewModel(get()) }
        viewModel<MoreOptionsViewModel> { MockMoreOptionsViewModel() }
    })

    override fun adapter() = genericDevelopmentAdapter {
        header("Date Picker Action")
        clickableItem("Regular") {
            MockEmbarkViewModel.mockedData = STORY_WITCH_DATE_PICKER
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        header("Computed Value")
        clickableItem("Computed Value") {
            MockEmbarkViewModel.mockedData = STORY_WITH_COMPUTED_VALUE
        }
        header("Previous Insurer")
        clickableItem("Previous Insurer") {
            MockEmbarkViewModel.mockedData = PREVIOUS_INSURER_STORY
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        header("Embark Screen")
        clickableItem("Loading") {
            MockEmbarkViewModel.shouldLoad = false
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        header("Select Action")
        clickableItem("Regular") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STANDARD_STORY
            }
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Custom Response") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_SELECT_ACTION_AND_CUSTOM_RESPONSE
            }
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        header("Number Action")
        clickableItem("Regular") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_NUMBER_ACTION
            }
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        header("Text Action")
        clickableItem("Regular") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_TEXT_ACTION
            }
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Text action set") {
            MockEmbarkViewModel.mockedData = STORY_WITH_TEXT_ACTION_SET
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Custom Response") {
            MockEmbarkViewModel.apply {
                shouldLoad = true
                mockedData = STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE
            }
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Text action set first text personal number validation and second text with email validation") {
            MockEmbarkViewModel.mockedData =
                STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Email text validation") {
            MockEmbarkViewModel.mockedData = STORY_WITH_TEXT_ACTION_EMAIL_VALIDATION
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Personal number text validation") {
            MockEmbarkViewModel.mockedData = STORY_WITH_TEXT_ACTION_PERSONAL_NUMBER
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Birth date validation") {
            MockEmbarkViewModel.mockedData = STORY_WITH_TEXT_ACTION_BIRTH_DATE
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Birth date reverse validation") {
            MockEmbarkViewModel.mockedData = STORY_WITH_TEXT_ACTION_BIRTH_DATE_REVERSE
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Norwegian postal code validation") {
            MockEmbarkViewModel.mockedData = STORY_WITH_TEXT_ACTION_NORWEGIAN_POSTAL_CODE
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
        }
        clickableItem("Swedish postal validation") {
            MockEmbarkViewModel.mockedData = STORY_WITH_TEXT_ACTION_SWEDISH_POSTAL_CODE
            startActivity(EmbarkActivity.newInstance(context, this.javaClass.name))
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
        header("Progress")
        clickableItem("Story with progress") {
            MockEmbarkViewModel.apply {
                mockedData = PROGRESSABLE_STORY
                shouldLoad = true
            }
            startActivity(EmbarkActivity.newInstance(this@EmbarkMockActivity, this.javaClass.name))
        }
    }
}
