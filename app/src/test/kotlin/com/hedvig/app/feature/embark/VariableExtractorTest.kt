package com.hedvig.app.feature.embark

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.apollo.graphql.fragment.GraphQLVariablesFragment
import com.hedvig.android.apollo.graphql.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.app.feature.embark.variables.CastType
import com.hedvig.app.feature.embark.variables.Variable
import com.hedvig.app.feature.embark.variables.VariableExtractor
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

class VariableExtractorTest {

  @Test
  fun `should extract multi action variables`() {
    val variables = createTestVariables()
    val valueStore = createTestValueStore()
    val json = VariableExtractor.reduceVariables(
      variables,
      valueStore::get,
      valueStore::put,
      valueStore::getMultiActionItems,
    )

    assertThat(json.getString("street")).isEqualTo("Est")
    assertThat(json.getInt("yearOfConstruction")).isEqualTo(1991)

    val payload = (json.getJSONObject("input").getJSONArray("payload").get(0) as JSONObject)
    assertThat(payload.getBoolean("isSubleted")).isEqualTo(false)

    val firstExtraBuilding = (payload.get("extraBuildings") as JSONArray)[0] as JSONObject
    val secondExtraBuilding = (payload.get("extraBuildings") as JSONArray)[1] as JSONObject

    assertThat(firstExtraBuilding.getString("type")).isEqualTo("Carport")
    assertThat(firstExtraBuilding.getBoolean("hasWaterConnected")).isEqualTo(true)
    assertThat(firstExtraBuilding.getInt("area")).isEqualTo(13)

    assertThat(secondExtraBuilding.getString("type")).isEqualTo("Guest house")
    assertThat(secondExtraBuilding.getBoolean("hasWaterConnected")).isEqualTo(false)
    assertThat(secondExtraBuilding.getInt("area")).isEqualTo(5)
  }

  /**
   * Context: When there is a multi action field defined, it needs to be there for the Offer query to work.
   * For example, if there is a "extraBuildings" field specified, if the VariableExtractor skips it entirely the
   * offer query fails as it's expecting to find it, even as an empty array.
   */
  @Test
  fun `should put an empty list if there are no multi action variables`() {
    val valueStore: ValueStore = ValueStoreImpl()
    val variables: List<Variable> = listOf(
      Variable.Multi(
        key = "input.payload[0].data.extraBuildings",
        from = "extraBuildings",
        variables = listOf(
          Variable.Single(
            key = "type",
            from = "type",
            castAs = CastType.STRING,
          ),
          Variable.Single(
            key = "area",
            from = "area",
            castAs = CastType.INT,
          ),
          Variable.Single(
            key = "hasWaterConnected",
            from = "hasWaterConnected",
            castAs = CastType.BOOLEAN,
          ),
        ),
      ),
    )

    val json = VariableExtractor.reduceVariables(
      variables,
      valueStore::get,
      valueStore::put,
      valueStore::getMultiActionItems,
    )

    val payload = (json.getJSONObject("input").getJSONArray("payload").get(0) as JSONObject)
    val extraBuilding = payload.getJSONObject("data").getJSONArray("extraBuildings")

    assertThat(extraBuilding.length()).isEqualTo(0)
  }

  @Test
  fun `should compose variables into a nested object`() {
    val variables = listOf(
      Variable.Single(
        key = "input.payload[0].firstName",
        from = "firstName",
        castAs = CastType.STRING,
      ),
      Variable.Single(
        key = "input.payload[1].firstName",
        from = "firstName",
        castAs = CastType.STRING,
      ),
      Variable.Single(
        key = "lastName",
        from = "lastName",
        castAs = CastType.STRING,
      ),
      Variable.Single(
        key = "input.payload[1].data.address",
        from = "streetAddress",
        castAs = CastType.STRING,
      ),
      Variable.Constant(
        key = "input.payload[0].data.type",
        value = "SWEDISH_APARTMENT",
        castAs = CastType.STRING,
      ),
    )

    val valueStore = ValueStoreImpl()
    valueStore.put("firstName", "John")
    valueStore.put("lastName", "Doe")
    valueStore.put("streetAddress", "Hello World")

    val expected = JSONObject(
      "{" +
        "      input: {" +
        "        payload: [" +
        "          {" +
        "            firstName: 'John'," +
        "            data: {" +
        "              type: 'SWEDISH_APARTMENT'," +
        "            }," +
        "          }," +
        "          {" +
        "            firstName: 'John'," +
        "            data: {" +
        "              address: 'Hello World'," +
        "            }," +
        "          }," +
        "        ]," +
        "      }," +
        "      lastName: 'Doe'" +
        "    }",
    )

    val extractedVariables = VariableExtractor.reduceVariables(
      variables,
      valueStore::get,
      valueStore::put,
      valueStore::getMultiActionItems,
    )
    assertThat(extractedVariables.toString()).isEqualTo(expected.toString())
  }

  @Test
  fun `should extract file variables`() {
    val variables = createTestFileVariables()
    val valueStore = createTestValueStore()
    val fileVariables = VariableExtractor.extractFileVariable(variables, valueStore)
    val fileVariable = fileVariables.find { it.key == "audioRecording" }
    assertThat(fileVariable?.path).isEqualTo("path-to-file")
  }

  // Context: https://hedviginsurance.slack.com/archives/C016KUN61U3/p1643022424096600?thread_ts=1643021296.091300&cid=C016KUN61U3
  @Test
  fun `with the store missing the key, boolean value defaults to false`() {
    val booleanKey = "boolean_key"
    val emptyStore: ValueStore = ValueStoreImpl()
    val variables = listOf(
      Variable.Single(
        key = booleanKey,
        from = booleanKey,
        castAs = CastType.BOOLEAN,
      ),
    )

    val result = VariableExtractor.reduceVariables(
      variables,
      emptyStore::get,
      emptyStore::put,
      emptyStore::getMultiActionItems,
    )

    assertThat(result.getBoolean(booleanKey)).isEqualTo(false)
  }

  @Test
  fun `boolean values are extracted correctly from the store`() {
    val trueBooleanKey = "boolean_key_true"
    val falseBooleanKey = "boolean_key_false"
    val storeWithBooleanKey: ValueStore = ValueStoreImpl().apply {
      put(trueBooleanKey, "true")
      put(falseBooleanKey, "false")
    }
    val variables = listOf(
      Variable.Single(
        key = trueBooleanKey,
        from = trueBooleanKey,
        castAs = CastType.BOOLEAN,
      ),
      Variable.Single(
        key = falseBooleanKey,
        from = falseBooleanKey,
        castAs = CastType.BOOLEAN,
      ),
    )

    val result = VariableExtractor.reduceVariables(
      variables,
      storeWithBooleanKey::get,
      storeWithBooleanKey::put,
      storeWithBooleanKey::getMultiActionItems,
    )

    assertThat(result.getBoolean(trueBooleanKey)).isEqualTo(true)
    assertThat(result.getBoolean(falseBooleanKey)).isEqualTo(false)
  }

  @Test
  fun `integer values are extracted correctly from the store`() {
    val testKey = "testKey"
    val store: ValueStore = ValueStoreImpl().apply {
      put(testKey, "5")
    }
    val variables = listOf(
      Variable.Single(
        key = testKey,
        from = testKey,
        castAs = CastType.INT,
      ),
    )

    val result = VariableExtractor.reduceVariables(
      variables,
      store::get,
      store::put,
      store::getMultiActionItems,
    )

    assertThat(result.getInt(testKey)).isEqualTo(5)
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
    valueStore.put("audioRecording", "path-to-file")

    valueStore.put("extraBuildings[0]type", "Carport")
    valueStore.put("extraBuildings[0]area", "13")
    valueStore.put("extraBuildings[0]hasWaterConnected", "true")

    valueStore.put("extraBuildings[1]type", "Guest house")
    valueStore.put("extraBuildings[1]area", "5.0")
    valueStore.put("extraBuildings[1]hasWaterConnected", "false")
    valueStore.commitVersion()
    return valueStore
  }

  private fun createTestVariables(): List<Variable> {
    return listOf(
      Variable.Single(
        key = "contractBundleId",
        from = "contractBundleId",
        castAs = CastType.STRING,
      ),
      Variable.Single(
        key = "type",
        from = "homeType",
        castAs = CastType.STRING,
      ),
      Variable.Single(
        key = "street",
        from = "streetAddress",
        castAs = CastType.STRING,
      ),
      Variable.Single(
        key = "zip",
        from = "postalNumber",
        castAs = CastType.STRING,
      ),
      Variable.Single(
        key = "livingSpace",
        from = "livingSpace",
        castAs = CastType.INT,
      ),
      Variable.Single(
        key = "numberCoInsured",
        from = "householdSize",
        castAs = CastType.INT,
      ),
      Variable.Single(
        key = "ownerShip",
        from = "apartmentType",
        castAs = CastType.STRING,
      ),
      Variable.Single(
        key = "startDate",
        from = "movingDate",
        castAs = CastType.STRING,
      ),
      Variable.Single(
        key = "isStudent",
        from = "isStudent",
        castAs = CastType.BOOLEAN,
      ),
      Variable.Single(
        key = "ancillaryArea",
        from = "ancillaryArea",
        castAs = CastType.INT,
      ),
      Variable.Single(
        key = "yearOfConstruction",
        from = "yearOfConstruction",
        castAs = CastType.INT,
      ),
      Variable.Single(
        key = "input.payload[0].numberOfBathrooms",
        from = "numberOfBathrooms",
        castAs = CastType.INT,
      ),
      Variable.Single(
        key = "input.payload[0].isSubleted",
        from = "isSubleted",
        castAs = CastType.BOOLEAN,
      ),
      Variable.Multi(
        key = "input.payload[0].extraBuildings",
        from = "extraBuildings",
        variables = listOf(
          Variable.Single(
            key = "type",
            from = "type",
            castAs = CastType.STRING,
          ),
          Variable.Single(
            key = "area",
            from = "area",
            castAs = CastType.INT,
          ),
          Variable.Single(
            key = "hasWaterConnected",
            from = "hasWaterConnected",
            castAs = CastType.BOOLEAN,
          ),
        ),
      ),
      Variable.Constant(
        key = "input.payload[0].data.type",
        value = "SWEDISH_APARTMENT",
        castAs = CastType.STRING,
      ),
    )
  }

  private fun createTestFileVariables(): List<GraphQLVariablesFragment> {
    return listOf(
      GraphQLVariablesFragment(
        __typename = "EmbarkAPIGraphQLSingleVariable",
        asEmbarkAPIGraphQLSingleVariable = GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
          __typename = "EmbarkAPIGraphQLSingleVariable",
          key = "audioRecording",
          from = "audioRecording",
          `as` = EmbarkAPIGraphQLSingleVariableCasting.`file`,
        ),
        asEmbarkAPIGraphQLGeneratedVariable = null,
        asEmbarkAPIGraphQLMultiActionVariable = null,
        asEmbarkAPIGraphQLConstantVariable = null,
      ),
    )
  }
}
