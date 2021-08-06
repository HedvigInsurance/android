package com.hedvig.app.feature.keygear.data

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.util.extensions.getActivity
import com.hedvig.app.util.extensions.windowHeight
import com.hedvig.app.util.extensions.windowWidth
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.pow
import kotlin.math.sqrt

class DeviceInformationService(
    private val context: Context
) {
    @SuppressLint("HardwareIds")
    fun getDeviceFingerprint(): String {
        val messageDigest = MessageDigest.getInstance(SHA256)
        messageDigest.reset()

        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        messageDigest.update(androidId.toByteArray())

        val result = messageDigest.digest()
        return result.toHexString()
    }

    fun getDeviceType(): DeviceType {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        when (uiModeManager.currentModeType) {
            Configuration.UI_MODE_TYPE_TELEVISION -> return DeviceType.TV
            Configuration.UI_MODE_TYPE_WATCH -> return DeviceType.WATCH
        }

        val widthInches = (context.getActivity()?.windowWidth ?: 0).toFloat()
        val heightInches = (context.getActivity()?.windowHeight ?: 0).toFloat()

        val diagonalInches = sqrt(widthInches.pow(2) + heightInches.pow(2))

        // This is unfortunately the only trick that is available
        if (diagonalInches >= MINIMUM_TABLET_SCREEN_SIZE_INCHES) {
            return DeviceType.TABLET
        }

        return DeviceType.PHONE
    }

    fun getDeviceName() = "${Build.MANUFACTURER} ${Build.MODEL}"

    companion object {
        private const val SHA256 = "SHA-256"

        private const val MINIMUM_TABLET_SCREEN_SIZE_INCHES = 7.0f

        private fun ByteArray.toHexString() = BigInteger(1, this).toString(16).padStart(32, '0')
    }
}

enum class DeviceType {
    PHONE,
    TABLET,
    WATCH,
    TV;

    fun into() = when (this) {
        PHONE -> KeyGearItemCategory.PHONE
        TABLET -> KeyGearItemCategory.TABLET
        WATCH -> KeyGearItemCategory.SMART_WATCH
        TV -> KeyGearItemCategory.TV
    }
}
