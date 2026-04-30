import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import org.turnbox.app.data.datasource.JvmLocationsDataSourceImpl
import org.turnbox.app.data.datasource.LocationsRepositoryImpl
import org.turnbox.app.data.exporter.JvmLogExporter
import org.turnbox.app.data.importer.JvmConfigImporter
import org.turnbox.app.ui.TurnboxAppContent
import org.turnbox.app.ui.features.home.HomeScreenViewModel
import org.turnbox.app.ui.features.locations.LocationViewModel
import org.turnbox.app.ui.theme.AppTheme
import org.turnbox.app.vpn.DesktopVpnManager

private class DesktopAppDependencies {
    private val locationsDataSource = JvmLocationsDataSourceImpl()
    val locationsRepository = LocationsRepositoryImpl(locationsDataSource)
    val vpnManager = DesktopVpnManager(locationsRepository)
    val homeViewModel = HomeScreenViewModel(
        vpnManager = vpnManager,
        locationsRepository = locationsRepository,
        configImporter = JvmConfigImporter(),
        logExporter = JvmLogExporter()
    )
    val locationViewModel = LocationViewModel(locationsRepository)

    fun close() {
        vpnManager.close()
    }
}

fun main() = application {
    val dependencies = remember { DesktopAppDependencies() }

    Window(
        title = "Turnbox",
        state = rememberWindowState(width = 430.dp, height = 780.dp),
        onCloseRequest = {
            dependencies.close()
            exitApplication()
        },
    ) {
        window.minimumSize = Dimension(350, 600)
        DisposableEffect(Unit) {
            onDispose { dependencies.close() }
        }

        AppTheme {
            TurnboxAppContent(
                homeViewModel = dependencies.homeViewModel,
                locationViewModel = dependencies.locationViewModel,
                onImportFileRequested = {
                    chooseConfigFile(window)?.let { file ->
                        dependencies.homeViewModel.onFileSelected(file) {
                            dependencies.locationViewModel.loadLocations()
                            dependencies.homeViewModel.loadCurrentConfig()
                        }
                    }
                },
                onSaveLogsRequested = { onSaved, onError ->
                    chooseSaveFile(window, dependencies.homeViewModel.suggestedLogsFileName())?.let { file ->
                        dependencies.homeViewModel.onSaveLogsToFile(file, onSaved, onError)
                    }
                }
            )
        }
    }
}

private fun chooseConfigFile(owner: Frame): java.io.File? {
    val dialog = FileDialog(owner, "Import Turnbox Config", FileDialog.LOAD)
    dialog.isVisible = true
    return dialog.files.firstOrNull()
}

private fun chooseSaveFile(owner: Frame, defaultName: String): File? {
    val dialog = FileDialog(owner, "Save Turnbox Logs", FileDialog.SAVE)
    dialog.file = defaultName
    dialog.isVisible = true

    val fileName = dialog.file ?: return null
    val directory = dialog.directory ?: return File(fileName)
    return File(directory, fileName)
}
