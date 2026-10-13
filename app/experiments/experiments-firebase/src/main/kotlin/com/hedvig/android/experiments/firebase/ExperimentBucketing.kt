package com.hedvig.android.experiments.firebase

/**
 * The bucket, 0 to 99, a member falls into for an experiment. A member is in the treatment when their bucket is
 * below the experiment's percentage.
 *
 * Both platforms must compute this identically, so the recipe is fixed: take the UTF-8 bytes of
 * `"<experimentName>:<memberId>"`, hash them with 32-bit FNV-1a (offset basis 2166136261, prime 16777619, XOR the
 * byte then multiply, wrapping at 32 bits), and reduce the unsigned result modulo 100. Prefixing the experiment name
 * keeps the buckets of different experiments independent of each other. Known values, for cross-platform tests:
 * `ACCIDENT_DISCOUNT:123456` hashes to 1311234767, bucket 67; `ACCIDENT_DISCOUNT:member-abc` to 1936731147, bucket 47.
 */
internal fun experimentBucket(experimentName: String, memberId: String): Int {
  return (fnv1a32("$experimentName:$memberId".encodeToByteArray()) % 100u).toInt()
}

internal fun fnv1a32(bytes: ByteArray): UInt {
  var hash = 2166136261u
  for (byte in bytes) {
    hash = hash xor byte.toUByte().toUInt()
    hash *= 16777619u
  }
  return hash
}
