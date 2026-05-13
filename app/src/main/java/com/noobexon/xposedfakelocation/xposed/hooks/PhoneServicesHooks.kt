package com.noobexon.xposedfakelocation.xposed.hooks

import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class PhoneServicesHooks(private val appLpparam: LoadPackageParam) {
    private val tag = "[PhoneServicesHooks]"

    fun initHooks() {
        findClass(
            appLpparam.classLoader,
            "com.android.phone.PhoneInterfaceManager"
        ) ?: return

        // TODO: Implement coherent synthetic cell data before changing phone-process
        // cell APIs. Returning null/empty cell info is too easy for map SDKs and
        // anti-fraud checks to detect, so cell data is currently passed through.
        XposedBridge.log("$tag Instantiated hooks successfully")
    }

    private fun findClass(classLoader: ClassLoader, vararg names: String): Class<*>? {
        names.forEach { name ->
            try {
                return XposedHelpers.findClass(name, classLoader)
            } catch (_: Throwable) {
                // Keep trying ROM-specific framework names.
            }
        }
        XposedBridge.log("$tag None of these classes were found: ${names.joinToString()}")
        return null
    }
}
