package com.tbread.windows

import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

object NpcapInstaller {
    private val logger = LoggerFactory.getLogger(NpcapInstaller::class.java)
    private val winPcapDllNames = listOf("wpcap.dll", "Packet.dll")

    fun ensureInstalled() {
        if (!isWindows()) return
        if (isNpcapPresent()) {
            logger.info("Npcap detected; skipping bundled installer.")
            return
        }

        val installerPath = resolveInstallerPath()
        if (installerPath == null) {
            logger.error("Npcap installer not found; expected in the app resources under npcap/ directory.")
            return
        }

        runInstaller(installerPath)
    }

    private fun isWindows(): Boolean =
        System.getProperty("os.name")?.contains("Windows", ignoreCase = true) == true

    private fun isNpcapPresent(): Boolean {
        val systemRoot = System.getenv("SystemRoot") ?: return false
        val probeDirs = listOf(
            Paths.get(systemRoot, "System32", "Npcap"),
            Paths.get(systemRoot, "SysWOW64", "Npcap")
        )
        return probeDirs.any { dir ->
            winPcapDllNames.any { dll ->
                dir.resolve(dll).exists()
            }
        }
    }

    private fun resolveInstallerPath(): Path? {
        val resourceDir = System.getProperty("compose.application.resources.dir")
        val candidates = sequenceOf(
            resourceDir?.let { Paths.get(it, "npcap", "npcap-installer.exe") },
            Paths.get(System.getProperty("user.dir"), "npcap", "npcap-installer.exe"),
            Paths.get(System.getProperty("user.dir"), "resources", "npcap", "npcap-installer.exe"),
            Paths.get(System.getProperty("user.dir"), "..", "resources", "npcap", "npcap-installer.exe")
        ).filterNotNull()

        candidates.firstOrNull { it.exists() && it.isRegularFile() }?.let { return it }

        val embedded = javaClass.getResourceAsStream("/npcap/npcap-installer.exe") ?: return null
        val tempFile = Files.createTempFile("npcap-installer", ".exe")
        tempFile.toFile().deleteOnExit()
        embedded.use { input ->
            Files.newOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun runInstaller(installerPath: Path) {
        val command = listOf(
            installerPath.toAbsolutePath().toString(),
            "/S",
            "/winpcap_mode=yes"
        )
        logger.info("Launching bundled Npcap installer with WinPcap compatibility mode.")
        try {
            val process = ProcessBuilder(command)
                .inheritIO()
                .start()
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                logger.info("Npcap installer completed successfully.")
            } else {
                logger.error("Npcap installer exited with code {}", exitCode)
            }
        } catch (e: Exception) {
            logger.error("Failed to run bundled Npcap installer.", e)
        }
    }
}
