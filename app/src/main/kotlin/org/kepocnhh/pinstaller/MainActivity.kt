package org.kepocnhh.pinstaller

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity

internal class MainActivity : ComponentActivity() {
    private val injection by lazy { App.injection }
    private val logger by lazy { injection.loggers.create("[Main]") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        val name = ComponentName(context, MainDeviceAdminReceiver::class.java)
        if (dm.isDeviceOwnerApp(context.packageName)) {
            logger.debug("${name.shortClassName} device owner")
        }
        val pm = context.packageManager
        val packageName = "org.kepocnhh.foo.debug"
        val info = try {
            pm.getPackageInfo(packageName, 0)
        } catch (error: PackageManager.NameNotFoundException) {
            null
        }
        logger.debug("package: $packageName $info")
    }
}
