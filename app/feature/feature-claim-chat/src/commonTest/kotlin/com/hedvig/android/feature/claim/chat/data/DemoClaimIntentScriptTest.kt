package com.hedvig.android.feature.claim.chat.data

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class DemoClaimIntentScriptTest {
  @Test
  fun `the script walks every step and then reaches an outcome`() {
    val script = DemoClaimIntentScript()

    val visited = mutableListOf<StepContent>()
    var intent = script.start()
    while (intent.next is ClaimIntent.Next.Step) {
      visited += (intent.next as ClaimIntent.Next.Step).claimIntentStep.stepContent
      intent = script.advance()
    }

    // The loop exits on the first non-step, which must be the outcome rather than a stall.
    assertThat(intent.next).isInstanceOf(ClaimIntent.Next.Outcome::class)
    assertThat(visited.map { it::class.simpleName }).isEqualTo(
      listOf("AudioRecording", "Task", "Form", "ContentSelect", "Summary"),
    )
  }

  @Test
  fun `regretting re-presents the same step instead of advancing`() {
    val script = DemoClaimIntentScript()
    script.start()
    script.advance()

    val before = script.currentStep()
    val regretted = script.current()

    assertThat(before).isNotNull()
    assertThat((regretted.next as ClaimIntent.Next.Step).claimIntentStep.id).isEqualTo(before!!.id)
    assertThat(script.currentStep()!!.id).isEqualTo(before.id)
  }

  @Test
  fun `progress increases as the script advances`() {
    val script = DemoClaimIntentScript()

    val first = script.start().progress!!
    script.advance()
    val second = script.advance().progress!!

    assertThat(second > first).isTrue()
  }

  @Test
  fun `the single select step offers the contract options without a prefill`() {
    val script = DemoClaimIntentScript()
    var intent = script.start()
    var form: StepContent.Form? = null
    while (intent.next is ClaimIntent.Next.Step) {
      val content = (intent.next as ClaimIntent.Next.Step).claimIntentStep.stepContent
      if (content is StepContent.Form) {
        form = content
        break
      }
      intent = script.advance()
    }

    val field = form!!.fields.single()
    assertThat(field.type).isEqualTo(StepContent.Form.FieldType.SINGLE_SELECT)
    assertThat(field.options.size).isEqualTo(4)
    // Every option carries the secondary line the picker is meant to render.
    assertThat(field.options.all { it.subtitle != null }).isTrue()
    assertThat(field.selectedOptions).isEqualTo(emptyList())
  }

  @Test
  fun `the task flow emits every scheduled description including the ones that arrive together`() = runTest {
    val demo = GetClaimIntentUseCaseDemo(DemoClaimIntentScript())

    demo.invoke(DemoClaimIntentScript.intentId).test {
      val emitted = buildList {
        repeat(DemoClaimIntentScript.taskDescriptionSchedule.size) {
          addAll(awaitItem().getOrNull()!!.task.descriptions)
        }
      }

      assertThat(emitted).isEqualTo(
        DemoClaimIntentScript.taskDescriptionSchedule.flatMap { (_, descriptions) -> descriptions },
      )

      val completing = awaitItem().getOrNull()!!
      assertThat(completing.task.isCompleted).isTrue()
      awaitComplete()
    }
  }

  @Test
  fun `two descriptions share a single emission so the consumer has to pace them itself`() = runTest {
    // Guards the property that makes this script worth having: if the schedule ever flattens out to one description
    // per emission, a consumer that renders only the most recent one would look correct while still dropping
    // messages against a real backend.
    val burst = DemoClaimIntentScript.taskDescriptionSchedule.any { (_, descriptions) -> descriptions.size > 1 }

    assertThat(burst).isTrue()
  }
}
