package com.hedvig.app.testdata.feature.embark.builders

import giraffe.fragment.GraphQLVariablesFragment
import giraffe.type.EmbarkAPIGraphQLGeneratedVariable
import giraffe.type.EmbarkAPIGraphQLMultiActionVariable
import giraffe.type.EmbarkAPIGraphQLSingleVariable
import giraffe.type.EmbarkAPIGraphQLSingleVariableCasting
import giraffe.type.EmbarkAPIGraphQLVariableGeneratedType

data class GraphQLVariableBuilder(
  private val kind: VariableKind,
  private val key: String,
  private val from: String = "",
  private val singleType: EmbarkAPIGraphQLSingleVariableCasting = EmbarkAPIGraphQLSingleVariableCasting.string,
  private val storeAs: String = "",
  private val generatedType: EmbarkAPIGraphQLVariableGeneratedType = EmbarkAPIGraphQLVariableGeneratedType.uuid,
) {

  fun build() = GraphQLVariablesFragment(
    __typename = kind.typename,
    asEmbarkAPIGraphQLSingleVariable = if (kind == VariableKind.SINGLE) {
      GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
        __typename = kind.typename,
        key = key,
        from = from.ifEmpty {
          throw Error("Programmer error: attempted to build SingleVariable without providing `from`")
        },
        `as` = singleType,
      )
    } else {
      null
    },
    asEmbarkAPIGraphQLGeneratedVariable = if (kind == VariableKind.GENERATED) {
      GraphQLVariablesFragment.AsEmbarkAPIGraphQLGeneratedVariable(
        __typename = kind.typename,
        key = key,
        storeAs = storeAs.ifEmpty {
          throw Error("Programmer error: attempted to build GeneratedVariable without providing `storeAs`")
        },
        type = generatedType,
      )
    } else {
      null
    },
    asEmbarkAPIGraphQLMultiActionVariable = if (kind == VariableKind.MULTI_ACTION) {
      GraphQLVariablesFragment.AsEmbarkAPIGraphQLMultiActionVariable(
        __typename = kind.typename,
        key = key,
        from = from,
        variables = listOf(
          GraphQLVariablesFragment.Variable(
            __typename = EmbarkAPIGraphQLGeneratedVariable.type.name,
            asEmbarkAPIGraphQLGeneratedVariable1 = GraphQLVariablesFragment
              .AsEmbarkAPIGraphQLGeneratedVariable1(
                __typename = EmbarkAPIGraphQLGeneratedVariable.type.name,
                key = key,
                storeAs = storeAs.ifEmpty {
                  throw Error(
                    "Programmer error: attempted to build" +
                      " GeneratedVariable without providing `storeAs`",
                  )
                },
                type = generatedType,
              ),
            asEmbarkAPIGraphQLSingleVariable1 = null,
          ),
        ),
      )
    } else {
      null
    },
    asEmbarkAPIGraphQLConstantVariable = null,
  )

  enum class VariableKind {
    SINGLE,
    GENERATED,
    MULTI_ACTION,
    ;

    val typename: String
      get() = when (this) {
        SINGLE -> EmbarkAPIGraphQLSingleVariable.type.name
        GENERATED -> EmbarkAPIGraphQLGeneratedVariable.type.name
        MULTI_ACTION -> EmbarkAPIGraphQLMultiActionVariable.type.name
      }
  }
}
