package com.pixelvibe.vedioplayer.core.data.security

import android.content.Context
import org.json.JSONObject
import java.io.File

data class BackupData(
    val version: Int = 1,
    val appLockEnabled: Boolean = false,
    val incognitoMode: Boolean = false,
    val amoledTheme: Boolean = false,
    val subtitleFontSize: Int = 18,
    val subtitleFontColor: String = "#FFFFFF",
    val playbackSpeed: Float = 1f,
    val bassBoostLevel: Int = 0,
    val virtualizerStrength: Int = 0,
    val equalizerPreset: String = "NORMAL"
)

open class SettingsBackupManager(private val context: Context) {

    private val backupDir = File(context.filesDir, "backups")

    init {
        backupDir.mkdirs()
    }

    open fun exportBackup(
        appLockEnabled: Boolean = false,
        incognitoMode: Boolean = false,
        amoledTheme: Boolean = false,
        subtitleFontSize: Int = 18,
        subtitleFontColor: String = "#FFFFFF",
        playbackSpeed: Float = 1f,
        bassBoostLevel: Int = 0,
        virtualizerStrength: Int = 0,
        equalizerPreset: String = "NORMAL"
    ): String {
        val json = JSONObject()
        json.put("version", 1)
        json.put("appLockEnabled", appLockEnabled)
        json.put("incognitoMode", incognitoMode)
        json.put("amoledTheme", amoledTheme)
        json.put("subtitleFontSize", subtitleFontSize)
        json.put("subtitleFontColor", subtitleFontColor)
        json.put("playbackSpeed", playbackSpeed.toDouble())
        json.put("bassBoostLevel", bassBoostLevel)
        json.put("virtualizerStrength", virtualizerStrength)
        json.put("equalizerPreset", equalizerPreset)
        return json.toString(2)
    }

    open fun saveBackupToFile(
        filename: String = "pixelvibe_backup.json",
        data: BackupData = BackupData()
    ): File? {
        return try {
            val file = File(backupDir, filename)
            file.writeText(
                exportBackup(
                    appLockEnabled = data.appLockEnabled,
                    incognitoMode = data.incognitoMode,
                    amoledTheme = data.amoledTheme,
                    subtitleFontSize = data.subtitleFontSize,
                    subtitleFontColor = data.subtitleFontColor,
                    playbackSpeed = data.playbackSpeed,
                    bassBoostLevel = data.bassBoostLevel,
                    virtualizerStrength = data.virtualizerStrength,
                    equalizerPreset = data.equalizerPreset
                )
            )
            file
        } catch (_: Exception) {
            null
        }
    }

    open fun importBackup(json: String): BackupData? {
        return try {
            val obj = JSONObject(json)
            BackupData(
                version = obj.optInt("version", 1),
                appLockEnabled = obj.optBoolean("appLockEnabled"),
                incognitoMode = obj.optBoolean("incognitoMode"),
                amoledTheme = obj.optBoolean("amoledTheme"),
                subtitleFontSize = obj.optInt("subtitleFontSize", 18),
                subtitleFontColor = obj.optString("subtitleFontColor", "#FFFFFF"),
                playbackSpeed = obj.optDouble("playbackSpeed", 1.0).toFloat(),
                bassBoostLevel = obj.optInt("bassBoostLevel"),
                virtualizerStrength = obj.optInt("virtualizerStrength"),
                equalizerPreset = obj.optString("equalizerPreset", "NORMAL")
            )
        } catch (_: Exception) {
            null
        }
    }

    open fun loadBackupFromFile(filename: String = "pixelvibe_backup.json"): BackupData? {
        return try {
            val file = File(backupDir, filename)
            if (file.exists()) importBackup(file.readText()) else null
        } catch (_: Exception) {
            null
        }
    }
}
