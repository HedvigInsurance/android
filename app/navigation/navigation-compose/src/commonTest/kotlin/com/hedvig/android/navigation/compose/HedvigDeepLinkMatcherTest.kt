package com.hedvig.android.navigation.compose

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pure-`commonMain` coverage for [HedvigDeepLinkMatcher.match].
 *
 * The real feature keys (`HomeKey`, `ChatKey`, …) live in feature modules that this module does not
 * (and must not) depend on, and the real URI patterns live in `:navigation-core`'s
 * `HedvigDeepLinkContainer`. So we mirror the high-traffic patterns and the *shape* of their target
 * keys here (same `@SerialName` argument mapping, same nullability/defaults) and assert that the
 * matcher resolves a URI to the expected key. This guards the matcher's wiring + Nav3 precedence
 * rules against regressions; per-feature pattern strings are still owned by the container.
 *
 * NOTE: not executed as part of this audit — added for coverage only.
 */
private const val HOST = "https://link.dev.hedvigit.com"

// --- Test keys mirroring real high-traffic destinations ---------------------------------------

@Serializable private data object TestHomeKey : HedvigNavKey

@Serializable private data object TestPaymentsKey : HedvigNavKey

@Serializable private data object TestInboxKey : HedvigNavKey

@Serializable private data object TestHelpCenterHomeKey : HedvigNavKey

@Serializable private data object TestForeverKey : HedvigNavKey

@Serializable private data object TestInsurancesKey : HedvigNavKey

@Serializable
private data class TestChatKey(val conversationId: String) : HedvigNavKey

@Serializable
private data class TestClaimDetailsKey(
  @SerialName("claimId") val claimId: String,
) : HedvigNavKey

@Serializable
private data class TestHelpCenterTopicKey(
  @SerialName("id") val topicId: String = "",
) : HedvigNavKey

@Serializable
private data class TestEditCoInsuredTriageKey(
  @SerialName("contractId") val contractId: String? = null,
) : HedvigNavKey

@Serializable
private data class TestTerminateInsuranceKey(
  @SerialName("contractId") val insuranceId: String? = null,
) : HedvigNavKey

@Serializable
private data class TestContractDetailKey(
  @SerialName("contractId") val contractId: String,
) : HedvigNavKey

// --- Matcher under test, wired with the same pattern set the container produces ----------------

private fun matcher(): HedvigDeepLinkMatcher = HedvigDeepLinkMatcher(
  buildList {
    addAll(uriDeepLinkMatchers(listOf("$HOST", "$HOST/submit-claim"), TestHomeKey.serializer()))
    addAll(uriDeepLinkMatchers(listOf("$HOST/payments"), TestPaymentsKey.serializer()))
    addAll(uriDeepLinkMatchers(listOf("$HOST/chat", "$HOST/inbox"), TestInboxKey.serializer()))
    addAll(uriDeepLinkMatchers(listOf("$HOST/conversation/{conversationId}"), TestChatKey.serializer()))
    addAll(uriDeepLinkMatchers(listOf("$HOST/help-center"), TestHelpCenterHomeKey.serializer()))
    addAll(uriDeepLinkMatchers(listOf("$HOST/help-center/topic?id={id}"), TestHelpCenterTopicKey.serializer()))
    addAll(uriDeepLinkMatchers(listOf("$HOST/forever"), TestForeverKey.serializer()))
    addAll(uriDeepLinkMatchers(listOf("$HOST/claim-details?claimId={claimId}"), TestClaimDetailsKey.serializer()))
    // insurances + contract-without-id both resolve to insurances; contract-with-id is separate
    addAll(uriDeepLinkMatchers(listOf("$HOST/insurances", "$HOST/contract"), TestInsurancesKey.serializer()))
    addAll(uriDeepLinkMatchers(listOf("$HOST/contract?contractId={contractId}"), TestContractDetailKey.serializer()))
    addAll(
      uriDeepLinkMatchers(
        listOf("$HOST/edit-coinsured", "$HOST/edit-coinsured?contractId={contractId}"),
        TestEditCoInsuredTriageKey.serializer(),
      ),
    )
    addAll(
      uriDeepLinkMatchers(
        listOf("$HOST/terminate-contract?contractId={contractId}"),
        TestTerminateInsuranceKey.serializer(),
      ),
    )
  },
)

class HedvigDeepLinkMatcherTest {
  @Test fun unknownUri_returnsNull() {
    assertNull(matcher().match("$HOST/this-path-does-not-exist"))
  }

  @Test fun nonDeepLinkHost_returnsNull() {
    assertNull(matcher().match("https://example.com/payments"))
  }

  @Test fun home_bareHost_matchesHome() {
    assertEquals(TestHomeKey, matcher().match(HOST))
  }

  @Test fun submitClaim_matchesHome() {
    assertEquals(TestHomeKey, matcher().match("$HOST/submit-claim"))
  }

  @Test fun payments_matchesPayments() {
    assertEquals(TestPaymentsKey, matcher().match("$HOST/payments"))
  }

  @Test fun chat_matchesInbox() {
    assertEquals(TestInboxKey, matcher().match("$HOST/chat"))
  }

  @Test fun inbox_matchesInbox() {
    assertEquals(TestInboxKey, matcher().match("$HOST/inbox"))
  }

  @Test fun conversation_extractsConversationId() {
    assertEquals(TestChatKey("abc-123"), matcher().match("$HOST/conversation/abc-123"))
  }

  @Test fun helpCenter_matchesHelpCenterHome() {
    assertEquals(TestHelpCenterHomeKey, matcher().match("$HOST/help-center"))
  }

  @Test fun helpCenterTopic_extractsId() {
    assertEquals(TestHelpCenterTopicKey("42"), matcher().match("$HOST/help-center/topic?id=42"))
  }

  @Test fun forever_matchesForever() {
    assertEquals(TestForeverKey, matcher().match("$HOST/forever"))
  }

  @Test fun claimDetails_extractsClaimId() {
    assertEquals(TestClaimDetailsKey("claim-7"), matcher().match("$HOST/claim-details?claimId=claim-7"))
  }

  @Test fun claimDetails_missingRequiredArg_doesNotMatch() {
    // ClaimDetailsKey.claimId has no default, so a bare /claim-details must not resolve to it.
    assertNull(matcher().match("$HOST/claim-details"))
  }

  @Test fun terminateInsurance_extractsContractId() {
    assertEquals(
      TestTerminateInsuranceKey(insuranceId = "c-9"),
      matcher().match("$HOST/terminate-contract?contractId=c-9"),
    )
  }

  @Test fun editCoinsured_withoutContractId_matchesTriageWithNullId() {
    assertEquals(TestEditCoInsuredTriageKey(contractId = null), matcher().match("$HOST/edit-coinsured"))
  }

  @Test fun editCoinsured_withContractId_extractsContractId() {
    assertEquals(
      TestEditCoInsuredTriageKey(contractId = "c-1"),
      matcher().match("$HOST/edit-coinsured?contractId=c-1"),
    )
  }

  @Test fun contract_withoutId_fallsBackToInsurances() {
    assertEquals(TestInsurancesKey, matcher().match("$HOST/contract"))
  }

  @Test fun contract_withId_prefersContractDetail() {
    // Both /contract (exact) and /contract?contractId= match; the arg-bearing result wins.
    assertEquals(TestContractDetailKey("c-55"), matcher().match("$HOST/contract?contractId=c-55"))
  }

  @Test fun insurances_matchesInsurances() {
    assertEquals(TestInsurancesKey, matcher().match("$HOST/insurances"))
  }
}
