// SystemServicesHooks.kt
package com.noobexon.xposedfakelocation.xposed.hooks

import android.location.Location
import android.location.LocationManager
import android.net.wifi.WifiInfo
import android.os.Build
import android.util.ArrayMap
import com.noobexon.xposedfakelocation.xposed.utils.LocationUtil
import dalvik.system.PathClassLoader
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Field
import java.lang.reflect.Method

class SystemServicesHooks(val appLpparam: LoadPackageParam) {
    private val tag = "[SystemServicesHooks]"

    fun initHooks() {
        val classLoader = appLpparam.classLoader
        hookLastLocation(classLoader)
        hookLocationDispatch(classLoader)
        hookMiuiLocationServices(classLoader)
        hookWifiServices(classLoader)
        hookGnssRegistration(classLoader)
        hookGeofence(classLoader)
        XposedBridge.log("$tag Instantiated hooks successfully")
    }

    private fun hookLastLocation(classLoader: ClassLoader) {
        val serviceClass = findClass(
            classLoader,
            "com.android.server.location.LocationManagerService",
            "com.android.server.LocationManagerService"
        ) ?: return

        hookAll(serviceClass, "getLastLocation", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return

                val original = param.result as? Location
                param.result = LocationUtil.createFakeLocation(original)
                XposedBridge.log("$tag Replaced getLastLocation result.")
            }
        })
    }

    private fun hookLocationDispatch(classLoader: ClassLoader) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hookLocationProviderManager(classLoader)
        }
        hookReceiverCallbacks(classLoader)
    }

    private fun hookLocationProviderManager(classLoader: ClassLoader) {
        val providerClass = findClass(
            classLoader,
            "com.android.server.location.provider.LocationProviderManager"
        ) ?: return

        hookAll(providerClass, "onReportLocation", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!LocationUtil.isSpoofingEnabled()) return
                val locationResult = param.args.firstOrNull() ?: return
                val registrationsField = findField(providerClass, "mRegistrations") ?: return
                val registrations = registrationsField.get(param.thisObject) as? Map<*, *> ?: return

                val locationsField = findField(locationResult.javaClass, "mLocations") ?: return
                val originalLocations = locationsField.get(locationResult) as? List<*> ?: return
                val original = originalLocations.firstOrNull() as? Location
                val fakeLocation = LocationUtil.createFakeLocation(original)
                val originalRegistrations = ArrayMap<Any?, Any?>()
                val passthroughRegistrations = ArrayMap<Any?, Any?>()

                registrations.forEach { (key, value) ->
                    originalRegistrations[key] = value
                    val packageName = extractPackageName(value)
                    if (LocationUtil.shouldSpoofPackage(packageName)) {
                        locationsField.set(locationResult, arrayListOf(fakeLocation))
                        deliverLocationToRegistration(value, locationResult)
                        XposedBridge.log("$tag Delivered spoofed provider location to $packageName.")
                    } else {
                        passthroughRegistrations[key] = value
                    }
                }

                locationsField.set(locationResult, ArrayList(originalLocations))
                param.setObjectExtra("target_apps_original_registrations", originalRegistrations)
                registrationsField.set(param.thisObject, passthroughRegistrations)
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                val originalRegistrations = param.getObjectExtra("target_apps_original_registrations") as? Map<*, *>
                    ?: return
                val registrationsField = findField(providerClass, "mRegistrations") ?: return
                registrationsField.set(param.thisObject, originalRegistrations)
            }
        })
    }

    private fun hookMiuiLocationServices(classLoader: ClassLoader) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val miuiClass = findClass(
            classLoader,
            "com.android.server.location.MiuiBlurLocationManagerImpl",
            "com.android.server.location.MiuiBlurLocationManager"
        ) ?: return

        hookAll(miuiClass, "getBlurryLocation", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                param.result = replaceLocationLikeResult(param.result, param.method as? Method)
                XposedBridge.log("$tag Replaced MIUI blurry location result.")
            }
        })

        hookAll(miuiClass, "getBlurryCellLocation", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                // TODO: Synthesize coherent fake cell data before modifying this result.
                // Empty/null cell feedback is easier to detect than real pass-through data.
                XposedBridge.log("$tag Left MIUI blurry cell location result unchanged.")
            }
        })

        hookAll(miuiClass, "getBlurryCellInfos", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                // TODO: Synthesize coherent fake cell data before modifying this result.
                // Empty/null cell feedback is easier to detect than real pass-through data.
                XposedBridge.log("$tag Left MIUI blurry cell info result unchanged.")
            }
        })

        hookAll(miuiClass, "handleGpsLocationChangedLocked", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                param.result = defaultReturnValue(param.method as? Method)
                XposedBridge.log("$tag Blocked MIUI GPS location refresh while spoofing.")
            }
        })
    }

    private fun hookReceiverCallbacks(classLoader: ClassLoader) {
        val receiverClass = findClass(
            classLoader,
            "com.android.server.location.LocationManagerService\$Receiver",
            "com.android.server.LocationManagerService\$Receiver"
        ) ?: return

        hookAll(receiverClass, "callLocationChangedLocked", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!LocationUtil.shouldSpoofPackage(extractPackageName(param.thisObject))) return

                val locationArgIndex = param.args.indexOfFirst { it is Location }
                if (locationArgIndex == -1) return

                val original = param.args[locationArgIndex] as? Location
                param.args[locationArgIndex] = LocationUtil.createFakeLocation(original)
                XposedBridge.log("$tag Replaced Receiver.callLocationChangedLocked argument.")
            }
        })
    }

    private fun hookGnssRegistration(classLoader: ClassLoader) {
        val serviceClasses = listOfNotNull(
            findClass(classLoader, "com.android.server.location.gnss.GnssManagerService"),
            findClass(
                classLoader,
                "com.android.server.location.LocationManagerService",
                "com.android.server.LocationManagerService"
            )
        ).distinct()

        val methodsToBlock = listOf(
            "addGnssBatchingCallback",
            "addGnssMeasurementsListener",
            "addGnssNavigationMessageListener",
            "addGnssAntennaInfoListener",
            "registerGnssStatusCallback",
            "registerGnssNmeaCallback"
        )

        serviceClasses.forEach { serviceClass ->
            methodsToBlock.forEach { methodName ->
                hookAll(serviceClass, methodName, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!shouldSpoofArgs(param.args)) return
                        param.result = defaultReturnValue(param.method as? Method)
                        XposedBridge.log("$tag Blocked $methodName while spoofing is enabled.")
                    }
                })
            }
        }
    }

    private fun hookWifiServices(classLoader: ClassLoader) {
        val systemServiceManagerClass = findClass(
            classLoader,
            "com.android.server.SystemServiceManager"
        ) ?: return

        hookAll(systemServiceManagerClass, "loadClassFromLoader", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val serviceName = param.args.getOrNull(0) as? String ?: return
                if (serviceName != "com.android.server.wifi.WifiService") return

                val serviceClassLoader = param.args.getOrNull(1) as? PathClassLoader ?: return
                val wifiServiceClass = findClass(
                    serviceClassLoader,
                    "com.android.server.wifi.WifiServiceImpl"
                ) ?: return

                hookWifiServiceImpl(wifiServiceClass)
            }
        })
    }

    private fun hookWifiServiceImpl(wifiServiceClass: Class<*>) {
        hookAll(wifiServiceClass, "getScanResults", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                param.result = emptyList<Any>()
                XposedBridge.log("$tag Cleared Wi-Fi scan results while spoofing.")
            }
        })

        hookAll(wifiServiceClass, "getConnectionInfo", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                param.result = WifiInfo.Builder()
                    .setBssid("02:00:00:00:00:00")
                    .setSsid("AndroidAP".toByteArray())
                    .setRssi(-60)
                    .setNetworkId(0)
                    .build()
                XposedBridge.log("$tag Replaced Wi-Fi connection info while spoofing.")
            }
        })
    }

    private fun hookGeofence(classLoader: ClassLoader) {
        val serviceClass = findClass(
            classLoader,
            "com.android.server.location.LocationManagerService",
            "com.android.server.LocationManagerService"
        ) ?: return

        hookAll(serviceClass, "requestGeofence", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                param.result = defaultReturnValue(param.method as? Method)
                XposedBridge.log("$tag Blocked geofence registration while spoofing is enabled.")
            }
        })
    }

    private fun hookAll(clazz: Class<*>, methodName: String, callback: XC_MethodHook) {
        try {
            val hooks = XposedBridge.hookAllMethods(clazz, methodName, callback)
            if (hooks.isNotEmpty()) {
                XposedBridge.log("$tag Hooked ${clazz.name}#$methodName (${hooks.size} overloads).")
            }
        } catch (e: Throwable) {
            XposedBridge.log("$tag Failed hooking ${clazz.name}#$methodName: ${e.message}")
        }
    }

    private fun findClass(classLoader: ClassLoader, vararg names: String): Class<*>? {
        names.forEach { name ->
            try {
                return XposedHelpers.findClass(name, classLoader)
            } catch (_: Throwable) {
                // Try the next framework class name. AOSP moved these across releases.
            }
        }
        XposedBridge.log("$tag None of these classes were found: ${names.joinToString()}")
        return null
    }

    private fun findField(clazz: Class<*>, fieldName: String): Field? {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName).apply { isAccessible = true }
            } catch (_: NoSuchFieldException) {
                currentClass = currentClass.superclass
            }
        }
        return null
    }

    private fun deliverLocationToRegistration(registration: Any?, locationResult: Any) {
        if (registration == null) return
        runCatching {
            val acceptMethod = registration.javaClass.methods.firstOrNull { it.name == "acceptLocationChange" }
                ?: registration.javaClass.declaredMethods.firstOrNull { it.name == "acceptLocationChange" }
                ?: return
            acceptMethod.isAccessible = true
            val operation = acceptMethod.invoke(registration, locationResult)

            val executeMethod = registration.javaClass.methods.firstOrNull { it.name == "executeOperation" }
                ?: registration.javaClass.declaredMethods.firstOrNull { it.name == "executeOperation" }
                ?: return
            executeMethod.isAccessible = true
            executeMethod.invoke(registration, operation)
        }.onFailure {
            XposedBridge.log("$tag Failed delivering spoofed provider location: ${it.message}")
        }
    }

    private fun shouldSpoofArgs(args: Array<Any?>?): Boolean {
        return args?.asSequence()
            ?.mapNotNull(::extractPackageName)
            ?.any(LocationUtil::shouldSpoofPackage) == true
    }

    private fun extractPackageName(value: Any?): String? {
        if (value == null) return null
        if (value is String) return value.takeIf(::looksLikePackageName)

        listOf("mPackageName", "packageName", "callingPackage", "mCallingPackage").forEach { fieldName ->
            val packageName = findField(value.javaClass, fieldName)?.get(value) as? String
            if (looksLikePackageName(packageName)) return packageName
        }

        listOf("mIdentity", "mCallerIdentity", "callerIdentity", "identity").forEach { fieldName ->
            val packageName = extractPackageName(findField(value.javaClass, fieldName)?.get(value))
            if (packageName != null) return packageName
        }

        return null
    }

    private fun looksLikePackageName(value: String?): Boolean {
        return value != null && "." in value && !value.startsWith("android.location.")
    }

    private fun replaceLocationLikeResult(result: Any?, method: Method?): Any? {
        if (result is Location) {
            return LocationUtil.createFakeLocation(result)
        }

        if (result != null) {
            val locationsField = findField(result.javaClass, "mLocations")
            val originalLocations = locationsField?.get(result) as? List<*>
            val original = originalLocations?.firstOrNull() as? Location
            if (locationsField != null) {
                locationsField.set(result, arrayListOf(LocationUtil.createFakeLocation(original)))
                return result
            }

            if (result is List<*>) {
                val original = result.firstOrNull() as? Location
                return listOf(LocationUtil.createFakeLocation(original))
            }

            runCatching {
                val sizeMethod = result.javaClass.methods.firstOrNull { it.name == "size" && it.parameterTypes.isEmpty() }
                val getMethod = result.javaClass.methods.firstOrNull { it.name == "get" && it.parameterTypes.size == 1 }
                val size = sizeMethod?.invoke(result) as? Int ?: return@runCatching
                if (size > 0) {
                    val originalLocation = getMethod?.invoke(result, 0) as? Location ?: return@runCatching
                    val fakeLocation = LocationUtil.createFakeLocation(originalLocation)
                    originalLocation.latitude = fakeLocation.latitude
                    originalLocation.longitude = fakeLocation.longitude
                    originalLocation.altitude = fakeLocation.altitude
                    originalLocation.accuracy = fakeLocation.accuracy
                    originalLocation.speed = fakeLocation.speed
                }
            }.onFailure {
                XposedBridge.log("$tag Could not inspect MIUI location container: ${it.message}")
            }

            return result
        }

        return if (method?.returnType?.let { Location::class.java.isAssignableFrom(it) } == true) {
            LocationUtil.createFakeLocation(provider = LocationManager.FUSED_PROVIDER)
        } else {
            null
        }
    }

    private fun defaultReturnValue(method: Method?): Any? {
        return when (method?.returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0F
            java.lang.Double.TYPE -> 0.0
            else -> null
        }
    }
}
