// MainHook.kt
package com.noobexon.xposedfakelocation.xposed

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.noobexon.xposedfakelocation.data.MANAGER_APP_PACKAGE_NAME
import com.noobexon.xposedfakelocation.xposed.hooks.LocationApiHooks
import com.noobexon.xposedfakelocation.xposed.hooks.PhoneServicesHooks
import com.noobexon.xposedfakelocation.xposed.hooks.SystemServicesHooks
import com.noobexon.xposedfakelocation.xposed.utils.PreferencesUtil
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class MainHook : IXposedHookLoadPackage {
    private val tag = "[MainHook]"

    lateinit var context: Context

    private var locationApiHooks: LocationApiHooks? = null
    private var systemServicesHooks: SystemServicesHooks? = null
    private var phoneServicesHooks: PhoneServicesHooks? = null

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                XposedBridge.log("$tag Installing system-server location hooks.")
                systemServicesHooks = SystemServicesHooks(lpparam).also { it.initHooks() }
                return
            }
            "com.android.phone" -> {
                XposedBridge.log("$tag Installing phone-process location side-channel hooks.")
                phoneServicesHooks = PhoneServicesHooks(lpparam).also { it.initHooks() }
                return
            }
            MANAGER_APP_PACKAGE_NAME -> {
                XposedBridge.log("$tag Manager process loaded; skipping spoof hooks.")
                return
            }
            else -> initHookingLogic(lpparam)
        }
    }

    private fun initHookingLogic(lpparam: LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            "android.app.Instrumentation",
            lpparam.classLoader,
            "callApplicationOnCreate",
            Application::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    context = (param.args[0] as Application).applicationContext.also {
                        XposedBridge.log("$tag Target App's context has been acquired successfully.")
                        if (!PreferencesUtil.getHideFakeLocationToast()) {
                            Toast.makeText(it, "Fake Location Is Active!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    locationApiHooks = LocationApiHooks(lpparam).also { it.initHooks() }
                }
            }
        )
    }
}