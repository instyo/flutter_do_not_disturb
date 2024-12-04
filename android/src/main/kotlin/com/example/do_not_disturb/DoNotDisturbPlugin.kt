package com.example.do_not_disturb

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** DoNotDisturbPlugin */
class DoNotDisturbPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "do_not_disturb")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getDNDStatus" -> result.success(getDNDStatus())
      "openDndSettings" -> {
        openDndSettings()
        result.success(null)
      }
      "openNotificationPolicyAccessSettings" -> {
        openNotificationPolicyAccessSettings()
        result.success(null)
      }
      "isNotificationPolicyAccessGranted" -> {
        result.success(isNotificationPolicyAccessGranted())
      }
      "setInterruptionFilter" -> {
        val interruptionFilter: Int? = call.arguments()
        if(interruptionFilter == null) {
          result.error("INVALID_ARGUMENT", "Interruption filter is required", null)
        }
        else{
          result.success(setInterruptionFilter(interruptionFilter))
        }
      }
      "setNotificationPolicy" -> {
        val notificationPolicy: Int? = call.arguments()
        if(notificationPolicy == null) {
          result.error("INVALID_ARGUMENT", "Notification policy is required", null)
        }
        else{
          result.success(setNotificationPolicy(notificationPolicy))
        }
      }
      else -> result.notImplemented()
    }

  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }


  private fun getDNDStatus(): Int {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.currentInterruptionFilter
  }

  private fun openDndSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val intent = Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    }
  }

  private fun openNotificationPolicyAccessSettings() {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
  }

  private fun isNotificationPolicyAccessGranted(): Boolean {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      return notificationManager.isNotificationPolicyAccessGranted
  }

  private fun setInterruptionFilter(interruptionFilter: Int): Boolean {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (notificationManager.isNotificationPolicyAccessGranted) {
      notificationManager.setInterruptionFilter(interruptionFilter)
      return true
    }
    return false
  }

  private fun setNotificationPolicy(notificationPolicy: Int): Boolean {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (notificationManager.isNotificationPolicyAccessGranted) {
      notificationManager.setNotificationPolicy(notificationPolicy)
      return true
    }
    return false
  }
}
