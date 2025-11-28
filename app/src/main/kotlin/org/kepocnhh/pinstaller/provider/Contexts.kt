package org.kepocnhh.pinstaller.provider

import kotlin.coroutines.CoroutineContext

internal class Contexts(
    val main: CoroutineContext,
    val default: CoroutineContext,
)
