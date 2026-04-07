package com.pixelvibe.vedioplayer.presentation

import android.content.Context
import java.io.File
import kotlin.system.exitProcess

/**
 * Global exception handler that catches uncaught exceptions
 * and launches CrashActivity to display the crash details.
 */
class GlobalExceptionHandler(
    private val context: Context,
    private val crashActivityClass: Class<*>
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Build crash report
            val crashReport = buildCrashReport(thread, throwable)

            // Save crash report to file
            saveCrashReport(crashReport)

            // Launch crash activity
            val intent = android.content.Intent(context, crashActivityClass).apply {
                putExtra("CRASH_REPORT", crashReport)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)

            // Kill the process
            exitProcess(2)
        } catch (e: Exception) {
            // If crash activity fails, use default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun buildCrashReport(thread: Thread, throwable: Throwable): String {
        val sb = StringBuilder()
        sb.append("═══════════════════════════════════════\n")
        sb.append("       PIXELVIBE CRASH REPORT\n")
        sb.append("═══════════════════════════════════════\n\n")

        sb.append("Thread: ${thread.name}\n")
        sb.append("Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n\n")

        sb.append("─── Exception ───\n")
        sb.append("${throwable.javaClass.name}: ${throwable.message}\n\n")

        sb.append("─── Stack Trace ───\n")
        sb.append(android.util.Log.getStackTraceString(throwable))
        sb.append("\n\n")

        sb.append("─── Device Info ───\n")
        sb.append("Brand: ${android.os.Build.BRAND}\n")
        sb.append("Model: ${android.os.Build.MODEL}\n")
        sb.append("Manufacturer: ${android.os.Build.MANUFACTURER}\n")
        sb.append("Device: ${android.os.Build.DEVICE}\n")
        sb.append("Product: ${android.os.Build.PRODUCT}\n")
        sb.append("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})\n")
        sb.append("ABI: ${android.os.Build.SUPPORTED_ABIS.joinToString(", ")}\n\n")

        sb.append("─── App Info ───\n")
        try {
            val pm = context.packageManager
            val info = pm.getPackageInfo(context.packageName, 0)
            sb.append("Version: ${info.versionName} (${info.versionCode})\n")
        } catch (e: Exception) {
            sb.append("Version: Unknown\n")
        }

        sb.append("\n═══════════════════════════════════════\n")

        return sb.toString()
    }

    private fun saveCrashReport(report: String) {
        try {
            val crashDir = java.io.File(context.getExternalFilesDir(null), "crashes")
            if (!crashDir.exists()) crashDir.mkdirs()

            val timestamp = System.currentTimeMillis()
            val file = java.io.File(crashDir, "crash_$timestamp.txt")
            file.writeText(report)
        } catch (e: Exception) {
            // Ignore - crash report couldn't be saved
        }
    }
}
