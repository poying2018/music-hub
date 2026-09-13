package dev.naominet.lazer

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = "Java " + System.getProperty("java.version")
}
