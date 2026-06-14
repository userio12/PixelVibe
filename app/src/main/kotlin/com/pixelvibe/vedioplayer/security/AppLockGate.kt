package com.pixelvibe.vedioplayer.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.core.data.security.AppLockManager
import com.pixelvibe.vedioplayer.core.data.security.AppLockState

@Composable
fun AppLockGate(
    appLockManager: AppLockManager,
    onUnlocked: () -> Unit,
    content: @Composable () -> Unit
) {
    val state by appLockManager.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state == AppLockState.UNINITIALIZED && appLockManager.isPinSet()) {
            appLockManager.lock()
        }
    }

    when (state) {
        AppLockState.UNLOCKED -> content()
        AppLockState.UNINITIALIZED -> {
            if (appLockManager.isPinSet()) {
                PinEntryScreen(
                    mode = PinMode.UNLOCK,
                    onPinVerified = { onUnlocked() },
                    title = "Enter PIN",
                    onUnlock = appLockManager::verifyPin
                )
            } else {
                content()
            }
        }
        AppLockState.LOCKED -> {
            PinEntryScreen(
                mode = PinMode.UNLOCK,
                onPinVerified = { onUnlocked() },
                title = "App Locked",
                onUnlock = appLockManager::verifyPin
            )
        }
    }
}

enum class PinMode { SET, CONFIRM, UNLOCK }

@Composable
fun PinEntryScreen(
    mode: PinMode,
    onPinVerified: () -> Unit,
    title: String,
    onUnlock: (String) -> Boolean,
    onSetPin: ((String) -> Unit)? = null,
    onSkip: (() -> Unit)? = null
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.take(6); error = null },
            label = { Text(if (mode == PinMode.SET) "New PIN" else "Enter PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (mode == PinMode.SET) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPin,
                onValueChange = { confirmPin = it.take(6); error = null },
                label = { Text("Confirm PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                when (mode) {
                    PinMode.UNLOCK -> {
                        if (onUnlock(pin)) {
                            onPinVerified()
                        } else {
                            error = "Incorrect PIN"
                        }
                    }
                    PinMode.SET -> {
                        if (pin.length < 4) {
                            error = "PIN must be at least 4 digits"
                        } else if (pin != confirmPin) {
                            error = "PINs do not match"
                        } else {
                            onSetPin?.invoke(pin)
                            onPinVerified()
                        }
                    }
                    PinMode.CONFIRM -> {
                        if (pin == confirmPin) {
                            onSetPin?.invoke(pin)
                            onPinVerified()
                        } else {
                            error = "PINs do not match"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = pin.length >= 4 && (mode != PinMode.SET || confirmPin.length >= 4)
        ) {
            Text(if (mode == PinMode.UNLOCK) "Unlock" else "Set PIN")
        }

        if (onSkip != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onSkip) {
                Text("Skip")
            }
        }
    }
}
