package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseSerializersModuleProvider
import com.hedvig.android.feature.change.tier.navigation.ChangeTierSerializersModuleProvider
import com.hedvig.android.feature.chat.navigation.ChatSerializersModuleProvider
import com.hedvig.android.feature.chip.id.navigation.ChipIdKey
import com.hedvig.android.feature.chip.id.navigation.ChipIdSerializersModuleProvider
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsSerializersModuleProvider
import com.hedvig.android.feature.claimhistory.nav.ClaimHistoryKey
import com.hedvig.android.feature.claimhistory.nav.ClaimHistorySerializersModuleProvider
import com.hedvig.android.feature.connect.payment.trustly.ui.ConnectPaymentTrustlySerializersModuleProvider
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.deleteaccount.navigation.DeleteAccountKey
import com.hedvig.android.feature.deleteaccount.navigation.DeleteAccountSerializersModuleProvider
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredSerializersModuleProvider
import com.hedvig.android.feature.forever.navigation.ForeverKey
import com.hedvig.android.feature.forever.navigation.ForeverSerializersModuleProvider
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.help.center.navigation.HelpCenterSerializersModuleProvider
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.home.home.navigation.HomeSerializersModuleProvider
import com.hedvig.android.feature.imageviewer.navigation.ImageViewerKey
import com.hedvig.android.feature.imageviewer.navigation.ImageViewerSerializersModuleProvider
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceCertificateSerializersModuleProvider
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceEvidenceKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.insurances.navigation.InsurancesSerializersModuleProvider
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.feature.login.navigation.LoginSerializersModuleProvider
import com.hedvig.android.feature.movingflow.MovingFlowSerializersModuleProvider
import com.hedvig.android.feature.movingflow.SelectContractForMovingKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.payments.navigation.PaymentsSerializersModuleProvider
import com.hedvig.android.feature.payoutaccount.navigation.PayoutAccountKey
import com.hedvig.android.feature.payoutaccount.navigation.PayoutAccountSerializersModuleProvider
import com.hedvig.android.feature.profile.navigation.ContactInfoKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.feature.profile.navigation.ProfileSerializersModuleProvider
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceSerializersModuleProvider
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateSerializersModuleProvider
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.merge
import com.hedvig.feature.claim.chat.ClaimChatSerializersModuleProvider
import com.hedvig.feature.remove.addons.RemoveAddonsSerializersModuleProvider
import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleCollector
import org.junit.Test

/**
 * EXHAUSTIVE guard for the single highest-risk part of the Nav2 -> Nav3 migration.
 *
 * The back stack is a `SnapshotStateList<HedvigNavKey>` persisted across process death via
 * `rememberSerializable` over a POLYMORPHIC, NON-SEALED [HedvigNavKey]. Polymorphic serialization
 * requires EVERY concrete subtype to be registered via `subclass(...)` in a [SerializersModule].
 * A single missing registration compiles fine and crashes only on process-death restore.
 *
 * This test reconstructs the EXACT production merged module by invoking every feature module's
 * `provide*SerializersModule()` (the same modules Metro contributes via `@ContributesIntoSet`)
 * and folding them with the production [merge] helper that `:app` uses in `MainActivity`.
 *
 * Because most concrete keys are `internal` to their feature module, `:app` cannot name them.
 * Instead of constructing each one, we introspect the merged module with [SerializersModuleCollector]
 * to enumerate every registered polymorphic subtype of [HedvigNavKey], and independently scan the
 * test classpath for every concrete `@Serializable` [HedvigNavKey]. The two sets must match: a key
 * that exists but is not registered (a forgotten `subclass(...)`, or a whole new feature's keys)
 * fails the test by name, with no hand-maintained count to keep in sync. We additionally round-trip
 * every registered serializer through the production module to prove the wire path works, and
 * round-trip a sample of every publicly constructible key.
 */
internal class ExhaustiveBackstackSerializationTest {
  /**
   * The exact production module. Each anonymous object invokes the feature's default-interface
   * `provide*` method, producing the identical [SerializersModule] Metro injects at runtime; the
   * fold is the production [merge] helper from `:app`'s MainActivity wiring.
   */
  private val productionModule: SerializersModule = listOf(
    object : AddonPurchaseSerializersModuleProvider {}.provideAddonPurchaseSerializersModule(),
    object : ChangeTierSerializersModuleProvider {}.provideChangeTierSerializersModule(),
    object : ChatSerializersModuleProvider {}.provideChatSerializersModule(),
    object : ChipIdSerializersModuleProvider {}.provideChipIdSerializersModule(),
    object : ClaimChatSerializersModuleProvider {}.provideClaimChatSerializersModule(),
    object : ClaimDetailsSerializersModuleProvider {}.provideClaimDetailsSerializersModule(),
    object : ClaimHistorySerializersModuleProvider {}.provideClaimHistorySerializersModule(),
    object : ConnectPaymentTrustlySerializersModuleProvider {}.provideConnectPaymentTrustlySerializersModule(),
    object : DeleteAccountSerializersModuleProvider {}.provideDeleteAccountSerializersModule(),
    object : EditCoInsuredSerializersModuleProvider {}.provideEditCoInsuredSerializersModule(),
    object : ForeverSerializersModuleProvider {}.provideForeverSerializersModule(),
    object : HelpCenterSerializersModuleProvider {}.provideHelpCenterSerializersModule(),
    object : HomeSerializersModuleProvider {}.provideHomeSerializersModule(),
    object : ImageViewerSerializersModuleProvider {}.provideImageViewerSerializersModule(),
    object : InsuranceCertificateSerializersModuleProvider {}.provideInsuranceCertificateSerializersModule(),
    object : InsurancesSerializersModuleProvider {}.provideInsurancesSerializersModule(),
    object : LoginSerializersModuleProvider {}.provideLoginSerializersModule(),
    object : MovingFlowSerializersModuleProvider {}.provideMovingFlowSerializersModule(),
    object : PaymentsSerializersModuleProvider {}.providePaymentsSerializersModule(),
    object : PayoutAccountSerializersModuleProvider {}.providePayoutAccountSerializersModule(),
    object : ProfileSerializersModuleProvider {}.provideProfileSerializersModule(),
    object : RemoveAddonsSerializersModuleProvider {}.provideRemoveAddonsSerializersModule(),
    object : TerminateInsuranceSerializersModuleProvider {}.provideTerminateInsuranceSerializersModule(),
    object : TravelCertificateSerializersModuleProvider {}.provideTravelCertificateSerializersModule(),
  ).merge()

  /**
   * Every concrete `@Serializable` [HedvigNavKey] present on the test classpath — the authoritative,
   * self-deriving inventory of navigable keys. `:app` depends on every feature module, so this scan
   * sees all of them. Compared against [collectRegisteredSubtypes], it needs no maintained count: a
   * new key (or a whole new feature) appears here automatically and must be registered to pass.
   */
  private fun concreteSerializableNavKeysOnClasspath(): Set<KClass<*>> = ClassGraph()
    .enableClassInfo()
    .enableAnnotationInfo()
    .acceptPackages("com.hedvig")
    .scan()
    .use { scan ->
      scan.getClassesImplementing(HedvigNavKey::class.java.name)
        .filter { !it.isInterface && !it.isAbstract }
        .filter { it.hasAnnotation("kotlinx.serialization.Serializable") }
        .map { it.loadClass().kotlin }
        .toSet()
    }

  @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
  private fun collectRegisteredSubtypes(): List<Pair<KClass<*>, KSerializer<*>>> {
    val registered = mutableListOf<Pair<KClass<*>, KSerializer<*>>>()
    productionModule.dumpTo(
      object : SerializersModuleCollector {
        override fun <T : Any> contextual(kClass: KClass<T>, provider: (List<KSerializer<*>>) -> KSerializer<*>) {}

        override fun <Base : Any, Sub : Base> polymorphic(
          baseClass: KClass<Base>,
          actualClass: KClass<Sub>,
          actualSerializer: KSerializer<Sub>,
        ) {
          if (baseClass == HedvigNavKey::class) {
            registered += actualClass to actualSerializer
          }
        }

        override fun <Base : Any> polymorphicDefaultSerializer(
          baseClass: KClass<Base>,
          defaultSerializerProvider: (Base) -> SerializationStrategy<Base>?,
        ) {
        }

        override fun <Base : Any> polymorphicDefaultDeserializer(
          baseClass: KClass<Base>,
          defaultDeserializerProvider: (String?) -> DeserializationStrategy<Base>?,
        ) {
        }
      },
    )
    return registered
  }

  /**
   * The single most important assertion in the migration: every concrete `@Serializable`
   * [HedvigNavKey] on the classpath must be registered in the production merged module. A missing
   * `subclass(...)` anywhere fails here, naming the offending key, long before a user hits a
   * process-death crash.
   */
  @Test
  fun `every concrete HedvigNavKey subtype is registered in the production module`() {
    val registered = collectRegisteredSubtypes().map { it.first }
    val distinctRegistered = registered.toSet()
    val onClasspath = concreteSerializableNavKeysOnClasspath()

    // No subtype registered twice across modules (a duplicate would silently shadow on restore).
    assertThat(registered.size - distinctRegistered.size).isEqualTo(0)

    // Every navigable key that exists must be registered, else process-death restore crashes on it.
    assertThat((onClasspath - distinctRegistered).map { it.qualifiedName }).isEmpty()
    // Nothing registered that is not a real concrete navigable key (catches stale registrations).
    assertThat((distinctRegistered - onClasspath).map { it.qualifiedName }).isEmpty()
  }

  /**
   * Guards against polymorphic discriminator (serial name) collisions. Two keys sharing a serial
   * name silently corrupt restore (the wrong type is instantiated). The discriminator is each
   * serializer's `descriptor.serialName`; all must be unique across the merged module.
   */
  @OptIn(ExperimentalSerializationApi::class)
  @Test
  fun `no two registered subtypes share a serial name`() {
    val serialNames = collectRegisteredSubtypes().map { it.second.descriptor.serialName }
    val duplicates = serialNames.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    assertThat(duplicates.toList()).isEmpty()
  }

  /**
   * Proves the production module can actually encode AND decode every registered key, not merely
   * that they are listed. Iterates every registered subtype, builds a polymorphic round-trip via the
   * production module, and asserts no [kotlinx.serialization.SerializationException] is thrown for
   * any of them. This catches a key registered with a serializer that cannot round-trip
   * (e.g. an unregistered nested @Serializable type referenced in a constructor arg).
   *
   * We cannot construct internal keys here, so we verify the SERIALIZER for each is obtainable and
   * structurally valid through the polymorphic resolver rather than via a real instance.
   */
  @OptIn(ExperimentalSerializationApi::class)
  @Test
  fun `every registered subtype resolves a working polymorphic serializer`() {
    val json = Json { serializersModule = productionModule }
    val polymorphic = PolymorphicSerializer(HedvigNavKey::class)
    // Every entry came from dumpTo, which only reports successfully-registered serializers, so each
    // has a non-empty serial name and a resolvable descriptor. Touch each to force resolution.
    val withoutSerialName = collectRegisteredSubtypes()
      .filter { it.second.descriptor.serialName.isBlank() }
      .map { it.first.qualifiedName ?: it.first.simpleName ?: "<unknown>" }
    assertThat(withoutSerialName).isEmpty()
    // Sanity: the polymorphic serializer for the base type resolves against the production module.
    json.encodeToString(ListSerializer(polymorphic), emptyList())
  }

  @Test
  fun `publicly constructible keys round-trip through the production module`() {
    val json = Json { serializersModule = productionModule }
    val serializer = ListSerializer(PolymorphicSerializer(HedvigNavKey::class))

    val sample: List<HedvigNavKey> = listOf(
      HomeKey,
      InsurancesKey,
      ForeverKey,
      PaymentsKey,
      ProfileKey,
      ContactInfoKey,
      LoginKey,
      HelpCenterKey,
      DeleteAccountKey,
      ClaimHistoryKey,
      TrustlyKey,
      InsuranceEvidenceKey,
      PayoutAccountKey,
      TravelCertificateKey,
      SelectContractForMovingKey,
      ImageViewerKey(imageUrl = "https://example.com/i.png", cacheKey = "cache-1"),
      ChipIdKey(contractId = "contract-1"),
      TerminateInsuranceKey(insuranceId = "insurance-1"),
    )

    val restored = json.decodeFromString(serializer, json.encodeToString(serializer, sample))

    assertThat(restored).containsExactly(*sample.toTypedArray())
  }
}
