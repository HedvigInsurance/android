package com.hedvig.app.feature.embark

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.owldroid.fragment.GraphQLVariablesFragment
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.app.feature.embark.util.VariableExtractor
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

class VariableExtractorTest {

    @Test
    fun `should extract multi action variables`() {
        val variables = createTestVariables()
        val valueStore = createTestValueStore()
        val json = VariableExtractor.extractVariables(variables, valueStore)

        assertThat(json.getString("street")).isEqualTo("Est")
        assertThat(json.getInt("yearOfConstruction")).isEqualTo(1991)
        assertThat(json.getBoolean("isSubleted")).isEqualTo(false)

        val firstExtraBuilding = (json.get("extraBuildings") as JSONArray)[0] as JSONObject
        val secondExtraBuilding = (json.get("extraBuildings") as JSONArray)[1] as JSONObject

        assertThat(firstExtraBuilding.getString("type")).isEqualTo("Carport")
        assertThat(firstExtraBuilding.getBoolean("hasWaterConnected")).isEqualTo(true)
        assertThat(firstExtraBuilding.getInt("area")).isEqualTo(13)

        assertThat(secondExtraBuilding.getString("type")).isEqualTo("Guest house")
        assertThat(secondExtraBuilding.getBoolean("hasWaterConnected")).isEqualTo(false)
        assertThat(secondExtraBuilding.getInt("area")).isEqualTo(5)
    }

    private fun createTestValueStore(): ValueStore {
        val valueStore = ValueStoreImpl()

        valueStore.put("houseInformationResult", "30")
        valueStore.put("houseBathroomsResult", "1 bathroom")
        valueStore.put("householdSize", "2")
        valueStore.put("yearOfConstruction", "1991")
        valueStore.put("houseAddressResult", "Est 123 45")
        valueStore.put("ancillaryArea", "15")
        valueStore.put("numberOfBathrooms", "1")
        valueStore.put("extraBuildingsResult", "2")
        valueStore.put("housePeopleResult", "2")
        valueStore.put("postalNumber", "123 45")
        valueStore.put("houseSublettingResult", "No, I don't sublet")
        valueStore.put("livingSpace", "30")
        valueStore.put("houseOwnershipResult", "I own it")
        valueStore.put("houseFloorsResult", "4 floors or less")
        valueStore.put("isSubleted", "false")
        valueStore.put("accessDateResult", "13-06-2021")
        valueStore.put("homeTypeResult", "House")
        valueStore.put("streetAddress", "Est")
        valueStore.put("movingDate", "13-06-2021")
        valueStore.put("homeType", "house")

        valueStore.put("extraBuildings[0]type", "Carport")
        valueStore.put("extraBuildings[0]area", "13")
        valueStore.put("extraBuildings[0]hasWaterConnected", "true")

        valueStore.put("extraBuildings[1]type", "Guest house")
        valueStore.put("extraBuildings[1]area", "5")
        valueStore.put("extraBuildings[1]hasWaterConnected", "false")
        valueStore.commitVersion()
        return valueStore
    }

    private fun createTestVariables(): List<GraphQLVariablesFragment> {
        return listOf(
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "contractBundleId",
                    from = "contractBundleId",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.STRING
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "type",
                    from = "homeType",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.STRING
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "street",
                    from = "streetAddress",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.STRING
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "zip",
                    from = "postalNumber",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.STRING
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "livingSpace",
                    from = "livingSpace",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.INT
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "numberCoInsured",
                    from = "householdSize",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.INT
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "ownerShip",
                    from = "apartmentType",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.STRING
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "startDate",
                    from = "movingDate",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.STRING
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "isStudent",
                    from = "isStudent",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.BOOLEAN
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "ancillaryArea",
                    from = "ancillaryArea",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.INT
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "yearOfConstruction",
                    from = "yearOfConstruction",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.INT
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "numberOfBathrooms",
                    from = "numberOfBathrooms",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.INT
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLSingleVariable",
                asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                    __typename = "EmbarkAPIGraphQLSingleVariable",
                    key = "isSubleted",
                    from = "isSubleted",
                    as_ = EmbarkAPIGraphQLSingleVariableCasting.BOOLEAN
                ),
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = null
            ),
            GraphQLVariablesFragment(
                __typename = "EmbarkAPIGraphQLMultiActionVariable",
                asEmbarkAPIGraphQLSingleVariable = null,
                asEmbarkAPIGraphQLGeneratedVariable = null,
                asEmbarkAPIGraphQLMultiActionVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLMultiActionVariable(
                    __typename = "EmbarkAPIGraphQLMultiActionVariable",
                    key = "extraBuildings",
                    variables = listOf(
                        GraphQLVariablesFragment.Variable(
                            __typename = "EmbarkAPIGraphQLSingleVariable",
                            asEmbarkAPIGraphQLSingleVariable1 = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable1(
                                __typename = "EmbarkAPIGraphQLSingleVariable",
                                key = "type",
                                from = "type",
                                as_ = EmbarkAPIGraphQLSingleVariableCasting.STRING
                            ),
                            asEmbarkAPIGraphQLGeneratedVariable1 = null
                        ),
                        GraphQLVariablesFragment.Variable(
                            __typename = "EmbarkAPIGraphQLSingleVariable",
                            asEmbarkAPIGraphQLSingleVariable1 = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable1(
                                __typename = "EmbarkAPIGraphQLSingleVariable",
                                key = "area",
                                from = "area",
                                as_ = EmbarkAPIGraphQLSingleVariableCasting.INT
                            ),
                            asEmbarkAPIGraphQLGeneratedVariable1 = null
                        ),
                        GraphQLVariablesFragment.Variable(
                            __typename = "EmbarkAPIGraphQLSingleVariable",
                            asEmbarkAPIGraphQLSingleVariable1 = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable1(
                                __typename = "EmbarkAPIGraphQLSingleVariable",
                                key = "hasWaterConnected",
                                from = "hasWaterConnected",
                                as_ = EmbarkAPIGraphQLSingleVariableCasting.BOOLEAN
                            ),
                            asEmbarkAPIGraphQLGeneratedVariable1 = null
                        )
                    )
                )
            )
        )
    }
}
