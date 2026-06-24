package com.chalkdigital.cdads

import android.app.Activity
import com.chalkdigital.cdads.generated.CDAdsFlutterApi
import com.chalkdigital.cdads.generated.CDAdsHostApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/** Entry point for the cdads_flutter Flutter plugin on Android. */
class CdadsFlutterPlugin : FlutterPlugin, ActivityAware {

    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var currentActivity: Activity? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        flutterPluginBinding = binding

        val messenger = binding.binaryMessenger
        val flutterApi = CDAdsFlutterApi(messenger)
        val hostApiImpl = CDAdsHostApiImpl(flutterApi) { currentActivity }

        CDAdsHostApi.setUp(messenger, hostApiImpl)
        binding.platformViewRegistry.registerViewFactory(
            "cdads_flutter/banner",
            CDABannerPlatformViewFactory(),
        )
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        CDAdsHostApi.setUp(binding.binaryMessenger, null)
        flutterPluginBinding = null
    }

    // ── ActivityAware ─────────────────────────────────────────────────────────

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        currentActivity = null
    }

    override fun onDetachedFromActivity() {
        currentActivity = null
    }
}
