package com.example.cdads

import CDAdViewFactory
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.chalkdigital.ads.CDAds
import com.chalkdigital.ads.CDAdsInitialisationParams
import com.chalkdigital.common.CDAdsUtils
import android.content.Context
import com.chalkdigital.BuildConfig
import java.util.logging.Level
import android.util.Log
import android.app.Application
import com.chalkdigital.ads.CDDefines.CDADProvider
import com.chalkdigital.ads.CDDefines.CDEnvironment


/** CdadsFlutterPlugin */
class CdadsFlutterPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var applicationContext: Context
  private lateinit var cdAds: CDAds

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "cdads_flutter")
    channel.setMethodCallHandler(this)
    val messenger = flutterPluginBinding.binaryMessenger
        flutterPluginBinding
            .platformViewRegistry
            .registerViewFactory("cdads_ad_view", CDAdViewFactory(messenger))
    // application = binding.applicationContext as Application
    // initializeSdkIfNeeded(application)
    }

    // override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    //     // Optional: do activity-specific stuff
    // }



    // private fun initializeSdkIfNeeded(app: Application) {
    //     // Check if SDK is already initialized, then call init
    //     if (!YourSdk.isInitialized()) {
    //         YourSdk.init(app)
    //     }
    // }


  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method == "initialize") {

        val args = call.arguments as? Map<*, *> ?: run {
            result.error("INVALID_ARGUMENTS", "Expected a Map for initialization params", null)
            return
        }

        val partnerKey = args["partnerKey"] as? String ?: ""
        val applicationIABCategory = args["applicationIABCategory"] as? String ?: ""
        val distanceFilter = (args["distanceFilter"] as? Double ?: 100.0).toFloat()
        val locationUpdateInterval = (args["locationUpdateInterval"] as? Long ?: 30).toLong()
        val adLocationExpiryInterval = (args["adLocationExpiryInterval"] as? Long ?: 300).toLong()

        val logLevelOrdinal = (args["logLevel"] as? Int) ?: 0
        val providerOrdinal = (args["provider"] as? Int) ?: 0
        val environmentOrdinal = (args["environment"] as? Int) ?: 0

        val enableTracking = args["enableTracking"] as? Boolean ?: false

        // Set global config if needed
        CDAdsUtils.setLogLevel(Level.ALL)
        CDAdsUtils.initialize(applicationContext)
        CDAdsUtils.setGeoIpLocationEnabled(false)

        val cdAdsInitialisationParams = CDAdsInitialisationParams(applicationContext)


        CDAdsUtils.setLogLevel(
            when (logLevelOrdinal) {
            0 -> Level.ALL
            20 -> Level.FINER
            30 -> Level.INFO
            40 -> Level.WARNING
            50 -> Level.SEVERE
            10 -> Level.FINE
            70 -> Level.OFF
            else -> Level.INFO
        }
        )

        val providerEnum = when (providerOrdinal) {
            0 -> CDADProvider.CHALK
            1 -> CDADProvider.GOOGLE
            2 -> CDADProvider.AD_MARVEL
            3 -> CDADProvider.AD_COLONY_AURORA
            4 -> CDADProvider.AMAZON
            5 -> CDADProvider.CHARTBOOST
            6 -> CDADProvider.FACEBOOK
            7 -> CDADProvider.IN_MOBI
            8 -> CDADProvider.HEYZAP
            9 -> CDADProvider.MILLENNIAL
            10 -> CDADProvider.VUNGLE
            11 -> CDADProvider.VURVE
            12 -> CDADProvider.UNITY_ADS
            13 -> CDADProvider.YUME
            else -> CDADProvider.CHALK
        }
        cdAdsInitialisationParams.provider = providerEnum
        val environmentEnum = when (environmentOrdinal) {
            0 -> CDEnvironment.CDEnvironmentProduction
            1 -> CDEnvironment.CDEnvironmentTest
            else -> CDEnvironment.CDEnvironmentTest
        }
        cdAdsInitialisationParams.environment = environmentEnum

        CDAdsUtils.initialize(applicationContext)
        CDAdsUtils.setGeoIpLocationEnabled(false)

        cdAdsInitialisationParams.partnerKey = partnerKey
        cdAdsInitialisationParams.applicationIABCategory = applicationIABCategory
        cdAdsInitialisationParams.distanceFilter = distanceFilter
        cdAdsInitialisationParams.locationUpdateInterval = locationUpdateInterval
        cdAdsInitialisationParams.adLocationExpiryInterval = adLocationExpiryInterval
        cdAds = CDAds.initialiseWithParams(cdAdsInitialisationParams, (applicationContext as Application)) // or whatever your SDK expects
        cdAds.setEnableTracking(enableTracking);
        cdAds.start();
        result.success(null)
    }
     else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
