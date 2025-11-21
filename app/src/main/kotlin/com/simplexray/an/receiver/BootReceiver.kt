package com.simplexray.an.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.core.content.ContextCompat
import com.simplexray.an.prefs.Preferences

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.d(TAG, "Boot completed event received")

        try {
            // 1. 检查用户是否开启了开机自启
            val prefs = Preferences(context)
            if (!prefs.startOnBoot) {
                Log.d(TAG, "Start on boot is disabled in preferences")
                return
            }

            Log.d(TAG, "Start on boot is enabled, proceeding with VPN permission check")

            // 2. 检查 VPN 权限（关键步骤）
            val vpnIntent = VpnService.prepare(context)
            if (vpnIntent != null) {
                Log.d(TAG, "VPN permission not granted, cannot start automatically")
                // 权限未获取，无法在后台启动，直接返回
                return
            }

            Log.d(TAG, "VPN permission already granted, starting service")

            // 3. 启动服务，携带特定的 Action 标识
            val serviceIntent = Intent(context, Class.forName("com.simplexray.an.service.TProxyService"))
            serviceIntent.action = "ACTION_START_ON_BOOT"

            // 使用 ContextCompat.startForegroundService 启动前台服务
            ContextCompat.startForegroundService(context, serviceIntent)

            Log.d(TAG, "Boot start service initiated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in boot receiver", e)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}