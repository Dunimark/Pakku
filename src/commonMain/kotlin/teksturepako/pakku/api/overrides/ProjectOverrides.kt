package teksturepako.pakku.api.overrides

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import teksturepako.pakku.api.data.workingPath
import teksturepako.pakku.api.overrides.Overrides.PAKKU_DIR
import teksturepako.pakku.api.projects.ProjectType
import teksturepako.pakku.io.tryOrNull
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries

suspend fun readProjectOverrides(): Set<ProjectOverride> = ProjectOverride.Type.entries
    .flatMap { ovType ->
        ProjectType.entries.map { projType ->
            Path(workingPath, PAKKU_DIR, ovType.folderName, projType.folderName)
        }
    }
    .mapNotNull { path ->
        path.tryOrNull { it.listDirectoryEntries() }
    }
    .flatten()
    .map { path ->
        coroutineScope {
            async {
                ProjectOverride.createOrNull(path)
            }
        }
    }
    .awaitAll()
    .filterNotNull()
    .toSet()