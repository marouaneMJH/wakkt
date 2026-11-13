package com.wakkt.app.ui.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.wakkt.app.R

data class PermissionStatus(val overlay: Boolean, val usage: Boolean) {
    val all: Boolean get() = overlay && usage
}

/** Current permission state, re-checked every time the screen resumes (user returns from Settings). */
@Composable
fun rememberPermissionStatus(): PermissionStatus {
    val context = LocalContext.current
    var status by remember { mutableStateOf(readStatus(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { status = readStatus(context) }
    return status
}

private fun readStatus(context: Context) = PermissionStatus(
    overlay = PermissionChecks.hasOverlayPermission(context),
    usage = PermissionChecks.hasUsageAccess(context),
)

fun Context.openOverlaySettings() = startSafely(
    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
)

fun Context.openUsageAccessSettings() = startSafely(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))

fun Context.startSafely(intent: Intent) {
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        // Settings screen not available on this device; nothing sensible to do.
    }
}

/** The two permission rows, shared by onboarding and settings. */
@Composable
fun PermissionRows(status: PermissionStatus, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PermissionRow(
            title = stringResource(R.string.onboarding_overlay_title),
            description = stringResource(R.string.onboarding_overlay_desc),
            granted = status.overlay,
            onGrant = { context.openOverlaySettings() },
        )
        PermissionRow(
            title = stringResource(R.string.onboarding_usage_title),
            description = stringResource(R.string.onboarding_usage_desc),
            granted = status.usage,
            onGrant = { context.openUsageAccessSettings() },
        )
    }
}

@Composable
fun PermissionRow(
    title: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.size(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(16.dp))
            if (granted) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.onboarding_granted),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp),
                )
            } else {
                FilledTonalButton(onClick = onGrant) { Text(stringResource(R.string.onboarding_grant)) }
            }
        }
    }
}
