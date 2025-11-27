package org.kepocnhh.pinstaller

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

internal class MainDeviceAdminReceiver : DeviceAdminReceiver() {
    private val injection by lazy { App.injection }
    private val logger by lazy { injection.loggers.create("[Admin]") }

    override fun onEnabled(context: Context, intent: Intent) {
        logger.debug("on enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        logger.debug("on disabled")
    }
}
