@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package com.hedvig.android.umbrella

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * The Swift-visible API of the graph, its inputs, the log sink and the read failures, as the header Swift compiles
 * against declares it. A changed declaration fails the snapshot, so a boxed primitive, a renamed or removed member, or
 * a new type reached from the API shows in the pull request that causes it. Update [API_SNAPSHOT] or [HOOK_SNAPSHOT]
 * from the failure message when the change is meant.
 *
 * The test hook is in the header only when the framework is built with `-Phedvig.ios.testHook=true`; without it, its
 * test checks that none of it is there.
 */
class HeaderSnapshotTest {
  private val apiTypes = listOf(
    "HedvigSharedIosGraphHolderKt",
    "HedvigSharedIosGraphHolder",
    "HedvigSharedFlavor",
    "HedvigSharedAccessTokenFetcher",
    "HedvigSharedDeviceIdFetcher",
    "HedvigSharedIosLanguageSource",
    "HedvigSharedLanguage",
    "HedvigSharedNetworkLogPolicy",
    "HedvigSharedIosLogSink",
    "HedvigSharedIosLogcatLoggerCompanion",
    "HedvigSharedReadFailure",
    "HedvigSharedReadFailureKind",
    "HedvigSharedReadFailureReason",
    "HedvigSharedReadFailures",
  )

  private val hookTypes = listOf(
    "HedvigSharedCannedBackend",
    "HedvigSharedTestGraphs",
    "HedvigSharedTestGraph",
    "HedvigSharedExampleReads",
    "HedvigSharedExampleConversations",
  )

  /** Types the API may name besides its own: the framework's base types and the logger's existing ones. */
  private val baseTypes = setOf(
    "HedvigSharedBase",
    "HedvigSharedKotlinEnum",
    "HedvigSharedKotlinEnumCompanion",
    "HedvigSharedKotlinArray",
    "HedvigSharedKotlinException",
    "HedvigSharedKotlinThrowable",
    "HedvigSharedKotlinUnit",
    "HedvigSharedLanguageCompanion",
    "HedvigSharedLogPriority",
    "HedvigSharedLogcatLogger",
    "HedvigSharedIosLogcatLogger",
    "HedvigSharedReadFailureKindNSEnum",
    "HedvigSharedReadFailureReasonNSEnum",
  )

  /** A line of the hook, wherever it is declared, such as the example reads' accessor on the holder. */
  private val hookLine = Regex("""ExampleReads|ExampleConversations|TestGraph|CannedBackend""")

  private val header: String by lazy {
    val testBinary = NSBundle.mainBundle.executablePath ?: error("No executable path")
    val path = testBinary.substringBeforeLast("/debugTest/") +
      "/debugFramework/HedvigShared.framework/Headers/HedvigShared.h"
    NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) ?: error("No header at $path")
  }

  private val declarations: Map<String, List<String>> by lazy { headerDeclarations(header) }

  private fun apiDeclarations(): String = apiTypes
    .flatMap { declarations[it] ?: listOf("MISSING $it") }
    .filterNot { hookLine.containsMatchIn(it) || it.endsWith("(Extensions)") }
    .joinToString("\n")

  private fun hookDeclarations(): String = (
    hookTypes.flatMap { declarations[it] ?: listOf("MISSING $it") } +
      declarations.getValue("HedvigSharedIosGraphHolder").filter { hookLine.containsMatchIn(it) }
  ).joinToString("\n")

  @Test
  fun `the graph API is declared as the snapshot says`() {
    assertThat(apiDeclarations()).isEqualTo(API_SNAPSHOT.trimIndent())
  }

  @Test
  fun `the test hook is declared as the snapshot says or is absent`() {
    if (hookTypes.none { it in declarations }) {
      assertThat(header.lines().filter { hookLine.containsMatchIn(it) }).isEmpty()
    } else {
      assertThat(hookDeclarations()).isEqualTo(HOOK_SNAPSHOT.trimIndent())
    }
  }

  @Test
  fun `the graph API reaches no other framework type`() {
    val reached = Regex("""HedvigShared\w+""").findAll(apiDeclarations()).map { it.value }.toSet()

    assertThat(reached - apiTypes.toSet() - baseTypes).isEmpty()
  }

  @Test
  fun `no declaration of the graph API boxes a primitive`() {
    val boxed = Regex("""HedvigShared(Kotlin(Int|Long|Short|Byte|Boolean|Double|Float|UInt|ULong)\b|Boolean\b|Int\b)""")

    assertThat(apiDeclarations().lines().filter { boxed.containsMatchIn(it) }).isEmpty()
  }

  private companion object {
    const val API_SNAPSHOT = """
@interface HedvigSharedIosGraphHolderKt : HedvigSharedBase
+ (HedvigSharedIosGraphHolder *)createIosGraphHolderAppFlavor:(HedvigSharedFlavor *)appFlavor accessTokenFetcher:(id<HedvigSharedAccessTokenFetcher>)accessTokenFetcher deviceIdFetcher:(id<HedvigSharedDeviceIdFetcher>)deviceIdFetcher languageSource:(id<HedvigSharedIosLanguageSource>)languageSource networkLogPolicy:(HedvigSharedNetworkLogPolicy *)networkLogPolicy __attribute__((swift_name("createIosGraphHolder(appFlavor:accessTokenFetcher:deviceIdFetcher:languageSource:networkLogPolicy:)")));
@interface HedvigSharedIosGraphHolder : HedvigSharedBase
- (void)sessionEnded __attribute__((swift_name("sessionEnded()")));
- (void)sessionStarted __attribute__((swift_name("sessionStarted()")));
@interface HedvigSharedFlavor : HedvigSharedKotlinEnum<HedvigSharedFlavor *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) HedvigSharedFlavor *production __attribute__((swift_name("production")));
@property (class, readonly) HedvigSharedFlavor *staging __attribute__((swift_name("staging")));
@property (class, readonly) HedvigSharedFlavor *develop __attribute__((swift_name("develop")));
+ (HedvigSharedKotlinArray<HedvigSharedFlavor *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<HedvigSharedFlavor *> *entries __attribute__((swift_name("entries")));
@protocol HedvigSharedAccessTokenFetcher
@required
- (void)fetchWithCompletionHandler:(void (^)(NSString * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("fetch(completionHandler:)")));
@protocol HedvigSharedDeviceIdFetcher
@required
- (void)fetchWithCompletionHandler:(void (^)(NSString * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("fetch(completionHandler:)")));
@protocol HedvigSharedIosLanguageSource
@required
- (HedvigSharedLanguage *)currentLanguage __attribute__((swift_name("currentLanguage()")));
@interface HedvigSharedLanguage : HedvigSharedKotlinEnum<HedvigSharedLanguage *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) HedvigSharedLanguageCompanion *companion __attribute__((swift_name("companion")));
@property (class, readonly) HedvigSharedLanguage *svSe __attribute__((swift_name("svSe")));
@property (class, readonly) HedvigSharedLanguage *enSe __attribute__((swift_name("enSe")));
+ (HedvigSharedKotlinArray<HedvigSharedLanguage *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<HedvigSharedLanguage *> *entries __attribute__((swift_name("entries")));
- (NSString *)toBcp47Format __attribute__((swift_name("toBcp47Format()")));
- (NSString *)description __attribute__((swift_name("description()")));
- (NSString *)webPath __attribute__((swift_name("webPath()")));
@interface HedvigSharedNetworkLogPolicy : HedvigSharedBase
- (instancetype)initWithLogsHttpBodies:(BOOL)logsHttpBodies logsGraphqlData:(BOOL)logsGraphqlData logsGraphqlErrors:(BOOL)logsGraphqlErrors __attribute__((swift_name("init(logsHttpBodies:logsGraphqlData:logsGraphqlErrors:)"))) __attribute__((objc_designated_initializer));
@property (readonly) BOOL logsGraphqlData __attribute__((swift_name("logsGraphqlData")));
@property (readonly) BOOL logsGraphqlErrors __attribute__((swift_name("logsGraphqlErrors")));
@property (readonly) BOOL logsHttpBodies __attribute__((swift_name("logsHttpBodies")));
@protocol HedvigSharedIosLogSink
@required
- (void)logPriority:(HedvigSharedLogPriority *)priority tag:(NSString * _Nullable)tag message:(NSString *)message __attribute__((swift_name("log(priority:tag:message:)")));
@interface HedvigSharedIosLogcatLoggerCompanion : HedvigSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) HedvigSharedIosLogcatLoggerCompanion *shared __attribute__((swift_name("shared")));
- (void)install __attribute__((swift_name("install()")));
- (void)installSink:(id<HedvigSharedIosLogSink>)sink minimumPriority:(HedvigSharedLogPriority *)minimumPriority __attribute__((swift_name("install(sink:minimumPriority:)")));
@interface HedvigSharedReadFailure : HedvigSharedKotlinException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithCause:(HedvigSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(HedvigSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) HedvigSharedReadFailureKind *kind __attribute__((swift_name("kind")));
@property (readonly) NSString *operationName __attribute__((swift_name("operationName")));
@property (readonly) NSString *readName __attribute__((swift_name("readName")));
@property (readonly) HedvigSharedReadFailureReason *reason __attribute__((swift_name("reason")));
@interface HedvigSharedReadFailureKind : HedvigSharedKotlinEnum<HedvigSharedReadFailureKind *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (readonly) HedvigSharedReadFailureKindNSEnum nsEnum;
@property (class, readonly) HedvigSharedReadFailureKind *general __attribute__((swift_name("general")));
@property (class, readonly) HedvigSharedReadFailureKind *sessionEnded __attribute__((swift_name("sessionEnded")));
+ (HedvigSharedKotlinArray<HedvigSharedReadFailureKind *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<HedvigSharedReadFailureKind *> *entries __attribute__((swift_name("entries")));
@interface HedvigSharedReadFailureReason : HedvigSharedKotlinEnum<HedvigSharedReadFailureReason *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (readonly) HedvigSharedReadFailureReasonNSEnum nsEnum;
@property (class, readonly) HedvigSharedReadFailureReason *graphqlErrors __attribute__((swift_name("graphqlErrors")));
@property (class, readonly) HedvigSharedReadFailureReason *network __attribute__((swift_name("network")));
@property (class, readonly) HedvigSharedReadFailureReason *http __attribute__((swift_name("http")));
@property (class, readonly) HedvigSharedReadFailureReason *timeout __attribute__((swift_name("timeout")));
@property (class, readonly) HedvigSharedReadFailureReason *stuck __attribute__((swift_name("stuck")));
@property (class, readonly) HedvigSharedReadFailureReason *sessionEnded __attribute__((swift_name("sessionEnded")));
@property (class, readonly) HedvigSharedReadFailureReason *unexpected __attribute__((swift_name("unexpected")));
+ (HedvigSharedKotlinArray<HedvigSharedReadFailureReason *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<HedvigSharedReadFailureReason *> *entries __attribute__((swift_name("entries")));
@interface HedvigSharedReadFailures : HedvigSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)readFailures __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) HedvigSharedReadFailures *shared __attribute__((swift_name("shared")));
- (HedvigSharedReadFailure * _Nullable)fromError:(NSError *)error __attribute__((swift_name("from(error:)")));
"""

    const val HOOK_SNAPSHOT = """
@interface HedvigSharedCannedBackend : HedvigSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)answerOperation:(NSString *)operation json:(NSString *)json __attribute__((swift_name("answer(operation:json:)")));
- (void)failOperation:(NSString *)operation __attribute__((swift_name("fail(operation:)")));
- (void)holdOperation:(NSString *)operation __attribute__((swift_name("hold(operation:)")));
- (void)releaseOperation:(NSString *)operation __attribute__((swift_name("release(operation:)")));
- (NSArray<NSString *> *)sentAuthorizations __attribute__((swift_name("sentAuthorizations()")));
- (NSArray<NSString *> *)sentOperations __attribute__((swift_name("sentOperations()")));
@interface HedvigSharedTestGraphs : HedvigSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)testGraphs __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) HedvigSharedTestGraphs *shared __attribute__((swift_name("shared")));
- (HedvigSharedTestGraph *)createBackend:(HedvigSharedCannedBackend *)backend tokenFetchNeverAnswers:(BOOL)tokenFetchNeverAnswers __attribute__((swift_name("create(backend:tokenFetchNeverAnswers:)")));
@interface HedvigSharedTestGraph : HedvigSharedBase
- (int32_t)tokenFetchCount __attribute__((swift_name("tokenFetchCount()")));
@property (readonly) HedvigSharedIosGraphHolder *holder __attribute__((swift_name("holder")));
@interface HedvigSharedExampleReads : HedvigSharedBase
- (HedvigSharedKotlinUnit *(^(^)(HedvigSharedKotlinUnit *(^)(HedvigSharedExampleConversations *, HedvigSharedKotlinUnit *), HedvigSharedKotlinUnit *(^)(NSError *, HedvigSharedKotlinUnit *), HedvigSharedKotlinUnit *(^)(NSError *, HedvigSharedKotlinUnit *)))(void))memberConversations __attribute__((swift_name("memberConversations()")));
- (HedvigSharedKotlinUnit *(^(^)(HedvigSharedKotlinUnit *(^)(HedvigSharedExampleConversations *, HedvigSharedKotlinUnit *), HedvigSharedKotlinUnit *(^)(NSError *, HedvigSharedKotlinUnit *), HedvigSharedKotlinUnit *(^)(NSError *, HedvigSharedKotlinUnit *)))(void))publicConversations __attribute__((swift_name("publicConversations()")));
@interface HedvigSharedExampleConversations : HedvigSharedBase
- (instancetype)initWithOpenCount:(int32_t)openCount __attribute__((swift_name("init(openCount:)"))) __attribute__((objc_designated_initializer));
@property (readonly) int32_t openCount __attribute__((swift_name("openCount")));
- (HedvigSharedExampleReads *)exampleReads __attribute__((swift_name("exampleReads()")));
"""
  }
}

/**
 * Each class's and protocol's declaration lines, without comments and blank lines, by its Objective-C name. A category
 * (`@interface X (Extensions)`) is added to its class's lines, so no declaration of the class goes unread.
 */
internal fun headerDeclarations(header: String): Map<String, List<String>> {
  val blocks = mutableMapOf<String, List<String>>()
  var current: String? = null
  var lines = mutableListOf<String>()
  var inComment = false
  for (line in header.lines()) {
    val trimmed = line.trim()
    if (inComment) {
      if (trimmed.endsWith("*/")) inComment = false
      continue
    }
    if (trimmed.startsWith("/**") || trimmed.startsWith("/*")) {
      if (!trimmed.endsWith("*/")) inComment = true
      continue
    }
    val opening = Regex("""^@(interface|protocol) (\w+)\b""").find(line)
    when {
      opening != null && !trimmed.endsWith(";") -> {
        current = opening.groupValues[2]
        lines = mutableListOf(line)
      }

      current != null && line.startsWith("@end") -> {
        blocks[current] = blocks[current].orEmpty() + lines
        current = null
      }

      current != null && trimmed.isNotEmpty() -> {
        lines += line
      }
    }
  }
  return blocks
}
