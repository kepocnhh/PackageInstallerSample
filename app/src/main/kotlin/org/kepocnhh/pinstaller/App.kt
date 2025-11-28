package org.kepocnhh.pinstaller

import android.app.Application
import android.content.Context
import kotlinx.coroutines.Dispatchers
import org.kepocnhh.pinstaller.provider.Contexts
import org.kepocnhh.pinstaller.provider.FinalAssets
import org.kepocnhh.pinstaller.provider.FinalLoggers
import org.kepocnhh.pinstaller.provider.Injection

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val context: Context = this
        _injection = Injection(
            loggers = FinalLoggers,
            assets = FinalAssets(context = context),
            contexts = Contexts(
                main = Dispatchers.Main,
                default = Dispatchers.Default,
            ),
        )
    }

    companion object {
        private var _injection: Injection? = null
        val injection: Injection get() = checkNotNull(_injection) { "No injection!" }
    }
}
