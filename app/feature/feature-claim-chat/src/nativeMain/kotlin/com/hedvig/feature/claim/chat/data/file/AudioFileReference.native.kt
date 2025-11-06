package com.hedvig.feature.claim.chat.data.file

actual class AudioFileReference {
    actual val pathOrUri: String = ""

    actual suspend fun readBytes(): ByteArray {
        return ByteArray(0)
    }
}
