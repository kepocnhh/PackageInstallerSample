package org.kepocnhh.pinstaller

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MainActivity : ComponentActivity() {
    private val injection by lazy { App.injection }
    private val logger by lazy { injection.loggers.create("[Main]") }

    private fun install(context: Context, packageName: String, version: String) {
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        val isDeviceOwnerApp = dm.isDeviceOwnerApp(context.packageName)
        if (!isDeviceOwnerApp) TODO()
        val pm = context.packageManager
        val pi = pm.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(packageName)
        val sessionId = pi.createSession(params)
        val session = pi.openSession(sessionId)
        val offsetBytes = 0L
//        val version = "0.0.1-debug-1"
        val name = "Foo-${version}.apk"
        val lengthBytes = -1L // unknown
        session.openWrite("foo", offsetBytes, lengthBytes).use { dst ->
            injection.assets.open(name).use { src ->
                val buffer = ByteArray(1_024)
                while (true) {
                    val len = src.read(buffer)
                    if (len < 0) break
                    dst.write(buffer, 0, len)
                }
            }
            session.fsync(dst)
        }
        val intent = Intent("org.kepocnhh.pinstaller.ACTION_INSTALL_COMPLETE")
        val sender = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        ).intentSender
        session.commit(sender)
    }

    private fun render(root: LinearLayout) {
        root.removeAllViews()
        val context: Context = root.context ?: error("No context!")
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        val isDeviceOwnerApp = dm.isDeviceOwnerApp(context.packageName)
        TextView(context).also { view ->
            view.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            view.text = "device owner: $isDeviceOwnerApp"
            root.addView(view)
        }
        val pm = context.packageManager
        val packageName = "org.kepocnhh.foo.debug"
        val info = try {
            pm.getPackageInfo(packageName, 0)
        } catch (error: PackageManager.NameNotFoundException) {
            null
        }
        logger.debug("package: $packageName $info")
        if (info != null) {
            TextView(context).also { view ->
                view.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                val text = """
                    package: ${info.packageName}
                    version:code: ${info.versionCode}
                    version:name: ${info.versionName}
                """.trimIndent()
                view.text = text
                root.addView(view)
            }
        }
        if (info == null && isDeviceOwnerApp) {
            Button(context).also { view ->
                view.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                view.text = "install $packageName"
                view.setOnClickListener { _ ->
                    lifecycleScope.launch {
                        withContext(injection.contexts.default) {
                            runCatching {
                                install(context = context, packageName = packageName, version = "0.0.1-debug-1")
                            }
                        }.fold(
                            onSuccess = {
                                logger.debug("install $packageName")
                                render(root = root)
                            },
                            onFailure = { error ->
                                logger.warning("install $packageName error: $error")
                            },
                        )
                    }
                }
                root.addView(view)
            }
        }
        if (info != null && isDeviceOwnerApp && info.versionCode < 2) {
            Button(context).also { view ->
                view.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                view.text = "update $packageName"
                view.setOnClickListener { _ ->
                    lifecycleScope.launch {
                        withContext(injection.contexts.default) {
                            runCatching {
                                install(context = context, packageName = packageName, version = "0.0.2-debug-2")
                            }
                        }.fold(
                            onSuccess = {
                                logger.debug("install $packageName")
                                render(root = root)
                            },
                            onFailure = { error ->
                                logger.warning("install $packageName error: $error")
                            },
                        )
                    }
                }
                root.addView(view)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
        val root = LinearLayout(context).also { root ->
            root.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            root.orientation = LinearLayout.VERTICAL
            root.gravity = Gravity.CENTER_VERTICAL
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                when (intent?.action) {
                    "org.kepocnhh.pinstaller.ACTION_INSTALL_COMPLETE" -> {
                        render(root = root)
                    }
                }
            }
        }
        setContentView(root)
        render(root = root)
        val filter = IntentFilter("org.kepocnhh.pinstaller.ACTION_INSTALL_COMPLETE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }
}
