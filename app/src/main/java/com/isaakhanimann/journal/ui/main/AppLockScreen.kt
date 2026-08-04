package com.isaakhanimann.journal.ui.main

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.isaakhanimann.journal.localization.i18n

/**
 * Full-screen app lock: prompts for biometrics or the device PIN/pattern
 * (device-credential fallback), with a manual retry button.
 */
@Composable
fun AppLockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasTriedAutoPrompt by remember { mutableStateOf(false) }

    // Resolve strings in composable context (i18n() is @Composable and cannot be
    // called inside the remember {} calculation below); the prompt lambda captures them.
    val failedText = i18n("app_lock_failed")
    val titleText = i18n("app_lock_title")
    val subtitleText = i18n("app_lock_subtitle")
    val cancelText = i18n("app_lock_cancel")
    val noCredentialText = i18n("app_lock_no_credential")
    val unavailableText = i18n("app_lock_unavailable")

    val promptAuthenticate: () -> Unit = remember(context) {
        {
            val activity = context as? FragmentActivity
            if (activity == null) {
                errorMessage = failedText
                return@remember
            }

            // Pre-flight check so we can show a clear message instead of a dead prompt
            // (e.g. no PIN set up, no biometrics enrolled, no hardware).
            val authenticators =
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            val canAuthenticate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                BiometricManager.from(context).canAuthenticate(authenticators)
            } else {
                // DEVICE_CREDENTIAL only exists on API 30+; on older devices fall
                // back to the no-arg check (biometric-only).
                BiometricManager.from(context).canAuthenticate()
            }
            when (canAuthenticate) {
                BiometricManager.BIOMETRIC_ERROR_NO_DEVICE_CREDENTIAL,
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    errorMessage = noCredentialText
                    return@remember
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                    errorMessage = unavailableText
                    return@remember
                }
            }

            val executor = ContextCompat.getMainExecutor(context)
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        errorMessage = null
                        onUnlocked()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        // User-initiated cancellation is not an error worth showing.
                        if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                            errorCode != BiometricPrompt.ERROR_USER_CANCELED
                        ) {
                            errorMessage = errString.toString()
                        }
                    }
                }
            )
            val promptBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(titleText)
                .setSubtitle(subtitleText)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+: biometrics or device credential, no negative button needed.
                promptBuilder.setAllowedAuthenticators(authenticators)
            } else {
                // API < 30: device credential is unsupported and biometric-only
                // prompts require an explicit negative button, otherwise
                // authenticate() throws IllegalArgumentException.
                promptBuilder
                    .setNegativeButtonText(cancelText)
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
                    )
            }
            try {
                prompt.authenticate(promptBuilder.build())
            } catch (e: Exception) {
                // e.g. BiometricPrompt shown before the activity is resumed
                errorMessage = failedText
            }
        }
    }

    // Trigger the system prompt automatically on first entry. The delay gives the
    // activity time to reach the resumed state (BiometricPrompt requires it).
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        if (!hasTriedAutoPrompt) {
            hasTriedAutoPrompt = true
            promptAuthenticate()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = i18n("app_lock_title"),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage ?: i18n("app_lock_hint"),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = if (errorMessage != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { promptAuthenticate() }) {
            Text(text = i18n("app_lock_unlock"))
        }
    }
}
