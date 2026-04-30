package org.turnbox.app.ui.activities

import android.app.Activity
import android.net.Uri
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.turnbox.app.ui.TurnboxAppContent
import org.turnbox.app.ui.features.home.HomeScreenViewModel
import org.turnbox.app.ui.features.locations.LocationViewModel

@Composable
fun AndroidMainScreen(
    viewModel: HomeScreenViewModel,
    locationViewModel: LocationViewModel
) {
    val context = LocalContext.current
    val pendingLogSaveCallbacks = remember {
        mutableStateOf<Pair<(String) -> Unit, (String) -> Unit>?>(null)
    }

    val vpnRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.ToggleVpn()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onFileSelected(it) {
                locationViewModel.loadLocations()
                viewModel.loadCurrentConfig()
            }
        }
    }

    val logSaveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        val callbacks = pendingLogSaveCallbacks.value
        pendingLogSaveCallbacks.value = null
        if (uri == null || callbacks == null) return@rememberLauncherForActivityResult

        viewModel.onSaveLogsToFile(
            target = uri,
            onSaved = callbacks.first,
            onError = callbacks.second
        )
    }

    TurnboxAppContent(
        homeViewModel = viewModel,
        locationViewModel = locationViewModel,
        onToggleClick = {
            val prepIntent = VpnService.prepare(context)
            if (prepIntent != null) {
                vpnRequestLauncher.launch(prepIntent)
            } else {
                viewModel.ToggleVpn()
            }
        },
        onImportFileRequested = {
            filePickerLauncher.launch("*/*")
        },
        onImportFromClipboardRequested = {
            viewModel.onPasteFromClipboard {
                locationViewModel.loadLocations()
                viewModel.loadCurrentConfig()
            }
        },
        onCopyConfigRequested = {
            viewModel.onCopyFullConfigClicked()
        },
        onSaveLogsRequested = { onSaved, onError ->
            pendingLogSaveCallbacks.value = onSaved to onError
            logSaveLauncher.launch(viewModel.suggestedLogsFileName())
        }
    )
}
