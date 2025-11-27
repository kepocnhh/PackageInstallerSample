package org.kepocnhh.pinstaller.provider

import java.io.InputStream

internal interface Assets {
    fun open(name: String): InputStream
}
