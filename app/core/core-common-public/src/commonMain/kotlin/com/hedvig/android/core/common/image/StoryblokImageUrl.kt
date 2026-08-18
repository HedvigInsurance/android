package com.hedvig.android.core.common.image

/**
 * Asks Storyblok's image service for a [targetPx]-square render of [src].
 *
 * Some assets are only published at their master resolution — the shop session pillow is 832x832 — and
 * collapsing that to an icon-sized box on device costs visible sharpness, because the decoder gets there
 * via a power-of-two subsample plus a residual bilinear pass. Letting the CDN resample instead means the
 * bytes arrive at the size they are drawn at, and the decode is 1:1.
 *
 * Returns [src] unchanged for anything that is not a Storyblok asset URL, for a URL that already carries a
 * transform, and for a non-positive [targetPx], so it is safe to call on any image URL the backend returns.
 */
fun storyblokResized(src: String, targetPx: Int): String {
  if (targetPx <= 0) return src
  if (!src.startsWith(STORYBLOK_ASSET_PREFIX)) return src
  val path = src.substringBefore('?').substringBefore('#')
  if (path.contains(STORYBLOK_TRANSFORM_SEGMENT)) return src
  val suffix = src.substring(path.length)
  return path.trimEnd('/') + STORYBLOK_TRANSFORM_SEGMENT + targetPx + "x" + targetPx + suffix
}

private const val STORYBLOK_ASSET_PREFIX = "https://a.storyblok.com/"
private const val STORYBLOK_TRANSFORM_SEGMENT = "/m/"
