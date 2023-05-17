package com.hedvig.app.testdata.feature.embark.data

import com.hedvig.app.testdata.feature.embark.builders.EmbarkStoryDataBuilder
import com.hedvig.app.testdata.feature.embark.builders.ExpressionBuilder
import com.hedvig.app.testdata.feature.embark.builders.GraphQLApiBuilder
import com.hedvig.app.testdata.feature.embark.builders.GraphQLVariableBuilder
import com.hedvig.app.testdata.feature.embark.builders.MessageBuilder
import com.hedvig.app.testdata.feature.embark.builders.NumberActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.PassageBuilder
import com.hedvig.app.testdata.feature.embark.builders.PreviousInsurerActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.RedirectBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectOptionBuilder
import com.hedvig.app.testdata.feature.embark.builders.TextActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.TextActionSetBuilder
import giraffe.EmbarkStoryQuery
import giraffe.fragment.EmbarkLinkFragment
import giraffe.fragment.GraphQLErrorsFragment
import giraffe.fragment.GraphQLResultsFragment
import giraffe.fragment.MessageFragment
import giraffe.type.EmbarkAPIGraphQLSingleVariableCasting
import giraffe.type.EmbarkAPIGraphQLVariableGeneratedType

val STANDARD_FIRST_MESSAGE = MessageBuilder(
  text = "test message",
).build()
val STANDARD_SECOND_MESSAGE = MessageBuilder(
  text = "another test message",
).build()
val STANDARD_THIRD_MESSAGE = MessageBuilder(
  text = "a third message",
).build()
val STANDARD_FOURTH_MESSAGE = MessageBuilder(
  text = "a fourth message",
).build()

val STANDARD_FIRST_LINK = EmbarkLinkFragment(
  name = "TestPassage2",
  label = "Another test passage",
  hidden = false,
)

val STANDARD_SECOND_LINK = EmbarkLinkFragment(
  name = "TestPassage",
  label = "Yet another test passage",
  hidden = false,
)

val LINK_TO_THIRD_PASSAGE = EmbarkLinkFragment(
  name = "TestPassage3",
  label = "A third test passage",
  hidden = false,
)

val LINK_TO_FOURTH_PASSAGE = EmbarkLinkFragment(
  name = "TestPassage4",
  label = "A fourth test passage",
  hidden = false,
)

val STANDARD_FIRST_PASSAGE_BUILDER =
  PassageBuilder(
    name = "TestPassage",
    id = "1",
    response = MessageBuilder(text = "{TestPassageResult}").buildMessageResponse(),
    messages = listOf(
      STANDARD_FIRST_MESSAGE,
    ),
    action = SelectActionBuilder(
      listOf(
        SelectOptionBuilder(
          link = STANDARD_FIRST_LINK,
          badge = "Badge #1",
        ).build(),
        SelectOptionBuilder(
          link = STANDARD_SECOND_LINK,
          badge = "Badge #2",
        ).build(),
        SelectOptionBuilder(
          link = STANDARD_FIRST_LINK,
        ).build(),
      ),
    ).build(),
  )

val STANDARD_SECOND_PASSAGE_BUILDER =
  PassageBuilder(
    name = "TestPassage2",
    id = "2",
    messages = listOf(STANDARD_SECOND_MESSAGE),
    action = SelectActionBuilder(
      listOf(
        SelectOptionBuilder(
          link = STANDARD_SECOND_LINK,
        ).build(),
        SelectOptionBuilder(
          link = STANDARD_SECOND_LINK,
        ).build(),
      ),
    ).build(),
  )

val STANDARD_THIRD_PASSAGE_BUILDER =
  PassageBuilder(
    name = "TestPassage3",
    id = "3",
    messages = listOf(STANDARD_THIRD_MESSAGE),
    action = SelectActionBuilder(
      listOf(
        SelectOptionBuilder(
          link = STANDARD_SECOND_LINK,
        ).build(),
      ),
    ).build(),
  )

val STANDARD_FOURTH_PASSAGE_BUILDER = PassageBuilder(
  name = "TestPassage4",
  id = "4",
  messages = listOf(STANDARD_FOURTH_MESSAGE),
  action = SelectActionBuilder(
    listOf(
      SelectOptionBuilder(
        link = STANDARD_SECOND_LINK,
      ).build(),
    ),
  ).build(),
)

val STANDARD_STORY = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER.build(),
    STANDARD_SECOND_PASSAGE_BUILDER.build(),
  ),
).build()

val STORY_WITH_TEXT_ACTION = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER.copy(
      action = TextActionBuilder(
        key = "BAR",
        link = STANDARD_FIRST_LINK,
        placeholder = "Test hint",
      ).build(),
    ).build(),
    STANDARD_SECOND_PASSAGE_BUILDER.copy(
      messages = listOf(
        MessageBuilder("{BAR} was entered")
          .build(),
      ),
    ).build(),
  ),
).build()

val STORY_WITH_TEXT_ACTION_EMAIL_VALIDATION = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER.copy(
      action = TextActionBuilder(
        key = "BAR",
        link = STANDARD_FIRST_LINK,
        placeholder = "Test hint",
        mask = TextActionBuilder.EMAIL,
      ).build(),
    ).build(),
    STANDARD_SECOND_PASSAGE_BUILDER.copy(
      messages = listOf(
        MessageBuilder("{BAR} was entered")
          .build(),
      ),
    ).build(),
  ),
).build()

val STORY_WITH_TEXT_ACTION_PERSONAL_NUMBER = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER.copy(
      action = TextActionBuilder(
        key = "BAR",
        link = STANDARD_FIRST_LINK,
        placeholder = "970407-1234",
        mask = TextActionBuilder.PERSONAL_NUMBER,
      ).build(),
    ).build(),
    STANDARD_SECOND_PASSAGE_BUILDER.copy(
      messages = listOf(
        MessageBuilder("{BAR} was entered")
          .build(),
      ),
    ).build(),
  ),
).build()

val PREVIOUS_INSURER_STORY = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER.copy(
      action = PreviousInsurerActionBuilder(
        storeKey = "BAR",
        next = STANDARD_FIRST_LINK,
      ).build(),
    ).build(),
    STANDARD_SECOND_PASSAGE_BUILDER.copy(
      messages = listOf(
        MessageBuilder("{BAR} was entered")
          .build(),
      ),
    ).build(),
  ),
).build()

val STORY_WITH_TEXT_ACTION_SET = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER.copy(
      action = TextActionSetBuilder(
        link = STANDARD_FIRST_LINK,
        textActions = listOf(
          TextActionBuilder(
            placeholder = "Placeholder",
            title = "Hint",
            key = "FOO",
            mask = null,
          ).buildTextActionSetAction(),
          TextActionBuilder(
            placeholder = "Second Placeholder",
            title = "Second Hint",
            key = "BAR",
            mask = null,
          ).buildTextActionSetAction(),
        ),
      ).build(),
    ).build(),
    STANDARD_SECOND_PASSAGE_BUILDER.copy(
      messages = listOf(
        MessageBuilder("{FOO} {BAR} was entered")
          .build(),
      ),
    ).build(),
  ),
).build()

val STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER.copy(
      action = TextActionSetBuilder(
        link = STANDARD_FIRST_LINK,
        textActions = listOf(
          TextActionBuilder(
            placeholder = "901124-1234",
            title = "Personal number",
            key = "FOO",
            mask = TextActionBuilder.PERSONAL_NUMBER,
          ).buildTextActionSetAction(),
          TextActionBuilder(
            placeholder = "example@email.com",
            title = "Email",
            key = "BAR",
            mask = TextActionBuilder.EMAIL,
          ).buildTextActionSetAction(),
        ),
      ).build(),
    ).build(),
    STANDARD_SECOND_PASSAGE_BUILDER.copy(
      messages = listOf(
        MessageBuilder("{FOO} {BAR} was entered")
          .build(),
      ),
    ).build(),
  ),
).build()

val STORY_WITH_INCOMPATIBLE_ACTION = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        action = EmbarkStoryQuery.Action(
          __typename = "",
          asEmbarkSelectAction = null,
          asEmbarkTextAction = null,
          asEmbarkTextActionSet = null,
          asEmbarkPreviousInsuranceProviderAction = null,
          asEmbarkNumberAction = null,
          asEmbarkNumberActionSet = null,
          asEmbarkDatePickerAction = null,
          asEmbarkMultiAction = null,
          asEmbarkAudioRecorderAction = null,
          asEmbarkExternalInsuranceProviderAction = null,
          asEmbarkAddressAutocompleteAction = null,
        ),
      )
      .build(),
  ),
).build()

val STORY_WITH_UNARY_EXPRESSIONS = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        messages = listOf(
          MessageBuilder(
            text = "Unary true test",
            expressions = listOf(
              ExpressionBuilder(
                type = ExpressionBuilder.ExpressionType.ALWAYS,
                text = "Unary true test",
              ).build(),
            ),
          ).build(),
          MessageBuilder(
            text = "Unary false test",
            expressions = listOf(
              ExpressionBuilder(
                type = ExpressionBuilder.ExpressionType.NEVER,
                text = "Unary false test",
              ).build(),
            ),
          ).build(),
        ),
      )
      .build(),
  ),
).build()

val STORY_WITH_TEMPLATE_MESSAGE = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        action = SelectActionBuilder(
          options = listOf(
            SelectOptionBuilder(
              link = STANDARD_FIRST_LINK,
              keyValues = listOf("FOO" to "BAR"),
            ).build(),
          ),
        ).build(),
      )
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .copy(
        messages = listOf(
          MessageBuilder(text = "{FOO} test").build(),
        ),
      ).build(),
  ),
).build()

val STORY_WITH_PASSED_KEY_VALUE = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy()
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .copy(
        redirects = listOf(
          RedirectBuilder(
            to = "TestPassage3",
            expression = ExpressionBuilder(type = ExpressionBuilder.ExpressionType.ALWAYS).build(),
            passedExpressionKey = "FOO",
            passedExpressionValue = "BAR",
          ).build(),
        ),
      )
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy(
        messages = listOf(
          MessageBuilder(
            text = "conditionally shown message",
            expressions = listOf(
              ExpressionBuilder(
                type = ExpressionBuilder.ExpressionType.EQUALS,
                key = "FOO",
                value = "BAR",
                text = "conditionally shown message",
              ).build(),
            ),
          ).build(),
        ),
      )
      .build(),
  ),
).build()

const val HELLO_QUERY = """
{
    hello
}
"""

val STORY_WITH_GRAPHQL_QUERY_API = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER.build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .copy(
        api = GraphQLApiBuilder(
          type = GraphQLApiBuilder.Type.QUERY,
          query = HELLO_QUERY,
          results = listOf(
            GraphQLResultsFragment(
              key = "hello",
              `as` = "HELLO",
            ),
          ),
          errors = listOf(
            GraphQLErrorsFragment(
              contains = null,
              next = GraphQLErrorsFragment.Next(
                __typename = "",
                fragments = GraphQLErrorsFragment.Next.Fragments(
                  LINK_TO_FOURTH_PASSAGE,
                ),
              ),
            ),
          ),
          next = LINK_TO_THIRD_PASSAGE,
        ).build(),
      )
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy(
        messages = listOf(
          MessageBuilder("api result: {HELLO}").build(),
        ),
      )
      .build(),
    STANDARD_FOURTH_PASSAGE_BUILDER
      .build(),
  ),
).build()

const val VARIABLE_QUERY = """
query VariableQuery(${'$'}variable: String!) {
    hello(variable: ${'$'}variable)
}
"""

val STORY_WITH_GRAPHQL_QUERY_API_AND_SINGLE_VARIABLE = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        action = TextActionBuilder(
          key = "input",
          link = STANDARD_FIRST_LINK,
        ).build(),
      )
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .copy(
        api = GraphQLApiBuilder(
          type = GraphQLApiBuilder.Type.QUERY,
          query = VARIABLE_QUERY,
          results = listOf(
            GraphQLResultsFragment(key = "hello", `as` = "VARIABLE"),
          ),
          variables = listOf(
            GraphQLVariableBuilder(
              kind = GraphQLVariableBuilder.VariableKind.SINGLE,
              key = "variable",
              from = "input",
              singleType = EmbarkAPIGraphQLSingleVariableCasting.string,
            ).build(),
          ),
          next = LINK_TO_THIRD_PASSAGE,
        ).build(),
      )
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy(
        messages = listOf(
          MessageBuilder("api result: {VARIABLE}").build(),
        ),
      )
      .build(),
    STANDARD_FOURTH_PASSAGE_BUILDER
      .build(),
  ),
).build()

val STORY_WITH_GRAPHQL_QUERY_API_AND_GENERATED_VARIABLE: EmbarkStoryQuery.Data = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .copy(
        api = GraphQLApiBuilder(
          type = GraphQLApiBuilder.Type.QUERY,
          query = VARIABLE_QUERY,
          results = listOf(
            GraphQLResultsFragment(key = "hello", `as` = "VARIABLE"),
          ),
          variables = listOf(
            GraphQLVariableBuilder(
              kind = GraphQLVariableBuilder.VariableKind.GENERATED,
              key = "variable",
              storeAs = "STORED",
              generatedType = EmbarkAPIGraphQLVariableGeneratedType.uuid,
            ).build(),
          ),
          next = LINK_TO_THIRD_PASSAGE,
        ).build(),
      )
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy(
        messages = listOf(
          MessageBuilder(text = "api result: {VARIABLE}").build(),
          MessageBuilder(text = "stored: {STORED}").build(),
        ),
      )
      .build(),
    STANDARD_FOURTH_PASSAGE_BUILDER
      .build(),
  ),
).build()

val HELLO_MUTATION = """
mutation {
    hello
}
""".trimIndent()

val STORY_WITH_GRAPHQL_MUTATION = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER.build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .copy(
        api = GraphQLApiBuilder(
          type = GraphQLApiBuilder.Type.MUTATION,
          query = HELLO_MUTATION,
          results = listOf(
            GraphQLResultsFragment(
              key = "hello",
              `as` = "HELLO",
            ),
          ),
          errors = listOf(
            GraphQLErrorsFragment(
              contains = null,
              next = GraphQLErrorsFragment.Next(
                __typename = "",
                fragments = GraphQLErrorsFragment.Next.Fragments(
                  LINK_TO_FOURTH_PASSAGE,
                ),
              ),
            ),
          ),
          next = LINK_TO_THIRD_PASSAGE,
        ).build(),
      )
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy(
        messages = listOf(
          MessageBuilder("api result: {HELLO}").build(),
        ),
      )
      .build(),
    STANDARD_FOURTH_PASSAGE_BUILDER
      .build(),
  ),
).build()

val VARIABLE_MUTATION = """
mutation VariableMutation(${'$'}variable: String!) {
    hello(variable: ${'$'}variable)
}
""".trimIndent()

val STORY_WITH_GRAPHQL_MUTATION_AND_SINGLE_VARIABLE = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        action = TextActionBuilder(
          key = "input",
          link = STANDARD_FIRST_LINK,
        ).build(),
      )
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .copy(
        api = GraphQLApiBuilder(
          type = GraphQLApiBuilder.Type.MUTATION,
          query = VARIABLE_MUTATION,
          results = listOf(
            GraphQLResultsFragment(
              key = "hello",
              `as` = "VARIABLE",
            ),
          ),
          errors = listOf(
            GraphQLErrorsFragment(
              contains = null,
              next = GraphQLErrorsFragment.Next(
                __typename = "",
                fragments = GraphQLErrorsFragment.Next.Fragments(
                  LINK_TO_FOURTH_PASSAGE,
                ),
              ),
            ),
          ),
          variables = listOf(
            GraphQLVariableBuilder(
              kind = GraphQLVariableBuilder.VariableKind.SINGLE,
              key = "variable",
              from = "input",
              singleType = EmbarkAPIGraphQLSingleVariableCasting.string,
            ).build(),
          ),
          next = LINK_TO_THIRD_PASSAGE,
        ).build(),
      )
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy(
        messages = listOf(
          MessageBuilder("api result: {VARIABLE}").build(),
        ),
      )
      .build(),
    STANDARD_FOURTH_PASSAGE_BUILDER
      .build(),
  ),
).build()

val STORY_WITH_NUMBER_ACTION = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        action = NumberActionBuilder(
          unit = "other people",
          placeholder = "1",
          label = "Co-insured",
          maxValue = 75,
          minValue = 1,
          link = STANDARD_FIRST_LINK,
        ).build(),
      )
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER.copy(
      messages = listOf(
        MessageBuilder("{BAR} was entered")
          .build(),
      ),
    ).build(),
  ),
).build()

val STORY_WITH_COMPUTED_VALUE = EmbarkStoryDataBuilder(
  computedStoreValues = listOf(
    EmbarkStoryQuery.ComputedStoreValue(key = "BAR", value = "FOO + 3"),
  ),
  passages = listOf(
    PassageBuilder(
      name = "TestPassage",
      id = "1",
      response = MessageBuilder(
        text = "{TestPassageResult}",
      ).buildMessageResponse(),
      messages = listOf(
        MessageFragment(
          text = "Text on input in next passage will have added 3 to your input",
          expressions = emptyList(),
        ),
      ),
      action = NumberActionBuilder(
        "FOO",
        link = STANDARD_FIRST_LINK,
      ).build(),
    ).build(),
    PassageBuilder(
      name = "TestPassage2",
      id = "2",
      response = MessageBuilder(
        text = "{TestPassageResult}",
      ).buildMessageResponse(),
      messages = listOf(
        MessageBuilder(
          text = "Computed value is previous input + 3 = {BAR}",
        ).build(),
      ),
      action = TextActionBuilder(
        link = STANDARD_SECOND_LINK,
        key = "BAR",
      ).build(),
    ).build(),
  ),
).build()

val STORY_WITH_TEXT_ACTION_SET_API = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        action = TextActionSetBuilder(
          api = GraphQLApiBuilder(
            type = GraphQLApiBuilder.Type.QUERY,
            query = HELLO_QUERY,
            results = listOf(
              GraphQLResultsFragment(
                key = "hello",
                `as` = "HELLO",
              ),
            ),
            errors = listOf(
              GraphQLErrorsFragment(
                contains = null,
                next = GraphQLErrorsFragment.Next(
                  __typename = "",
                  fragments = GraphQLErrorsFragment.Next.Fragments(
                    LINK_TO_FOURTH_PASSAGE,
                  ),
                ),
              ),
            ),
            next = LINK_TO_THIRD_PASSAGE,
          ).build(),
          link = STANDARD_SECOND_LINK,
          textActions = listOf(
            TextActionBuilder(
              key = "",
            ).buildTextActionSetAction(),
          ),
        ).build(),
      )
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy()
      .build(),
  ),
).build()

val STORY_WITH_TEXT_ACTION_API = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        action = TextActionBuilder(
          api = GraphQLApiBuilder(
            type = GraphQLApiBuilder.Type.QUERY,
            query = HELLO_QUERY,
            results = listOf(
              GraphQLResultsFragment(
                key = "hello",
                `as` = "HELLO",
              ),
            ),
            errors = listOf(
              GraphQLErrorsFragment(
                contains = null,
                next = GraphQLErrorsFragment.Next(
                  __typename = "",
                  fragments = GraphQLErrorsFragment.Next.Fragments(
                    LINK_TO_FOURTH_PASSAGE,
                  ),
                ),
              ),
            ),
            next = LINK_TO_THIRD_PASSAGE,
          ).build(),
          link = STANDARD_SECOND_LINK,
          key = "",
        ).build(),
      )
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy()
      .build(),
  ),
).build()

val STORY_WITH_SELECT_ACTION_API_SINGLE_OPTION = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        action = SelectActionBuilder(
          options = listOf(
            SelectOptionBuilder(
              api = GraphQLApiBuilder(
                type = GraphQLApiBuilder.Type.QUERY,
                query = HELLO_QUERY,
                results = listOf(
                  GraphQLResultsFragment(
                    key = "hello",
                    `as` = "HELLO",
                  ),
                ),
                errors = listOf(
                  GraphQLErrorsFragment(
                    contains = null,
                    next = GraphQLErrorsFragment.Next(
                      __typename = "",
                      fragments = GraphQLErrorsFragment.Next.Fragments(
                        LINK_TO_FOURTH_PASSAGE,
                      ),
                    ),
                  ),
                ),
                next = LINK_TO_THIRD_PASSAGE,
              ).build(),
              link = STANDARD_SECOND_LINK,
            ).build(),
          ),
        ).build(),
      )
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy()
      .build(),
  ),
).build()

val STORY_WITH_SELECT_ACTION_API_MULTIPLE_OPTIONS = EmbarkStoryDataBuilder(
  passages = listOf(
    STANDARD_FIRST_PASSAGE_BUILDER
      .copy(
        action = SelectActionBuilder(
          options = listOf(
            SelectOptionBuilder(
              api = GraphQLApiBuilder(
                type = GraphQLApiBuilder.Type.QUERY,
                query = HELLO_QUERY,
                results = listOf(
                  GraphQLResultsFragment(
                    key = "hello",
                    `as` = "HELLO",
                  ),
                ),
                errors = listOf(
                  GraphQLErrorsFragment(
                    contains = null,
                    next = GraphQLErrorsFragment.Next(
                      __typename = "",
                      fragments = GraphQLErrorsFragment.Next.Fragments(
                        LINK_TO_FOURTH_PASSAGE,
                      ),
                    ),
                  ),
                ),
                next = LINK_TO_FOURTH_PASSAGE,
              ).build(),
              link = STANDARD_SECOND_LINK,
            ).build(),
            SelectOptionBuilder(
              api = GraphQLApiBuilder(
                type = GraphQLApiBuilder.Type.QUERY,
                query = HELLO_QUERY,
                results = listOf(
                  GraphQLResultsFragment(
                    key = "hello",
                    `as` = "HELLO",
                  ),
                ),
                errors = listOf(
                  GraphQLErrorsFragment(
                    contains = null,
                    next = GraphQLErrorsFragment.Next(
                      __typename = "",
                      fragments = GraphQLErrorsFragment.Next.Fragments(
                        LINK_TO_FOURTH_PASSAGE,
                      ),
                    ),
                  ),
                ),
                next = LINK_TO_THIRD_PASSAGE,
              ).build(),
              link = STANDARD_SECOND_LINK,
            ).build(),
          ),
        ).build(),
      )
      .build(),
    STANDARD_SECOND_PASSAGE_BUILDER
      .build(),
    STANDARD_THIRD_PASSAGE_BUILDER
      .copy()
      .build(),
  ),
).build()
