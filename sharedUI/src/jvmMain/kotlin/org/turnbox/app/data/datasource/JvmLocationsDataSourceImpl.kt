package org.turnbox.app.data.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.turnbox.app.data.LOCATIONS_BUNDLE_FILE_NAME
import org.turnbox.app.data.model.LocationBundleV3
import org.turnbox.app.desktop.DesktopPaths
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class JvmLocationsDataSourceImpl(
    private val appDir: Path = DesktopPaths.appDataDir()
) : LocationsDataSource {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val bundleFile: Path
        get() = appDir.resolve(LOCATIONS_BUNDLE_FILE_NAME)

    override suspend fun loadLocationBundle(): LocationBundleV3? = withContext(Dispatchers.IO) {
        val file = bundleFile
        if (!file.exists()) return@withContext null
        runCatching {
            json.decodeFromString(LocationBundleV3.serializer(), file.readText()).normalized()
        }.getOrNull()
    }

    override suspend fun saveLocationBundle(bundle: LocationBundleV3): Unit = withContext(Dispatchers.IO) {
        Files.createDirectories(appDir)
        bundleFile.writeText(
            json.encodeToString(LocationBundleV3.serializer(), bundle.normalized())
        )
    }

    override suspend fun loadLegacyLocations(): List<Pair<String, String>> = emptyList()

    override suspend fun loadLegacyActiveLocationId(): String? = null
}
