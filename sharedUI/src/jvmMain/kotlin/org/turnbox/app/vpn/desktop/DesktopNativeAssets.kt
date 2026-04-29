package org.turnbox.app.vpn.desktop

import org.turnbox.app.desktop.DesktopOs
import org.turnbox.app.desktop.DesktopPaths
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import kotlin.io.path.exists

internal object DesktopNativeAssets {
    private const val DEFAULT_OLCRTC_REPO = "/Users/nigga/Personal/Projects/olcrtc"

    fun resolveOlcRtcBinary(): Path {
        val fileName = olcRtcFileName()
        val target = DesktopPaths.appDataDir().resolve("bin").resolve(fileName)
        Files.createDirectories(target.parent)

        val resourceName = "native/$fileName"
        val resource = javaClass.classLoader.getResourceAsStream(resourceName)
        if (resource != null) {
            resource.use {
                Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            }
            makeExecutable(target)
            return target
        }

        sourceCandidates(fileName).firstOrNull { it.exists() }?.let {
            Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            makeExecutable(target)
            return target
        }

        error("Bundled olcRTC binary is missing: $resourceName")
    }

    fun olcRtcFileName(): String {
        return when (DesktopPaths.os) {
            DesktopOs.MacOS -> "olcrtc-darwin-arm64"
            DesktopOs.Windows -> "olcrtc-windows-amd64.exe"
            DesktopOs.Other -> error("Turnbox desktop proxy mode supports macOS and Windows")
        }
    }

    private fun sourceCandidates(fileName: String): List<Path> {
        val explicitBinary = System.getenv("OLCRTC_BINARY")?.takeIf { it.isNotBlank() }?.let { Path(it) }
        val repo = System.getenv("OLCRTC_REPO")?.takeIf { it.isNotBlank() } ?: DEFAULT_OLCRTC_REPO
        return listOfNotNull(
            explicitBinary,
            Path(repo).resolve("build").resolve(fileName),
            Path(repo).resolve(fileName.removeSuffix(".exe")),
            Path(repo).resolve("olcrtc")
        )
    }

    private fun makeExecutable(path: Path) {
        if (DesktopPaths.os != DesktopOs.Windows) {
            path.toFile().setExecutable(true, true)
        }
    }
}
