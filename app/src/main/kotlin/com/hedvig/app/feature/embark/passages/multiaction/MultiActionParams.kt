package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MultiActionParams(
  val key: String,
  val messages: List<String>,
  val passageName: String,
  val link: String,
  val addLabel: String,
  val maxAmount: Int,
  val components: List<MultiActionComponent>,
  val submitLabel: String,
) : Parcelable

sealed class MultiActionComponent : Parcelable {
  @Parcelize
  data class Dropdown(
    val key: String,
    val label: String,
    val options: List<Option>,
  ) : MultiActionComponent() {
    @Parcelize
    data class Option(
      val text: String,
      val value: String,
    ) : Parcelable
  }

  @Parcelize
  data class Switch(
    val key: String,
    val label: String,
    val defaultValue: Boolean,
  ) : MultiActionComponent()

  @Parcelize
  data class Number(
    val key: String,
    val placeholder: String,
    val unit: String?,
    val label: String?,
  ) : MultiActionComponent()
}
