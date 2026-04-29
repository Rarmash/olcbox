package org.turnbox.app.vpn.desktop

import org.turnbox.app.data.model.LocationConfig
import java.nio.file.Path

internal data class OlcRtcCommand(
    val binary: Path,
    val location: LocationConfig,
    val socksHost: String = PacServer.LOCAL_SOCKS_HOST,
    val socksPort: Int = PacServer.LOCAL_SOCKS_PORT
) {
    fun args(): List<String> {
        val config = location.normalized()
        return listOf(
            binary.toString(),
            "-mode", "cnc",
            "-provider", config.bypassProvider,
            "-id", config.id,
            "-key", config.key,
            "-socks-host", socksHost,
            "-socks-port", socksPort.toString()
        )
    }
}
