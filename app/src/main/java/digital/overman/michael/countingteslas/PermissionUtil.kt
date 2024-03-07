package digital.overman.michael.countingteslas

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.security.Permission

internal const val PERM_LOCATION_COARSE = "android.permission.ACCESS_COARSE_LOCATION"
internal const val PERM_LOCATION_FINE = "android.permission.ACCESS_FINE_LOCATION"

internal fun AppCompatActivity.doWithPermission(
    permission: String,
    rationale: String = "",
    onPermissionNotGranted: () -> Unit = {},
    onPermissionGranted: () -> Unit
) {
    val permissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onPermissionGranted()
        else onPermissionNotGranted()
    }
    if (hasPermission(permission)) onPermissionGranted()
    else if (shouldShowRequestPermissionRationale(permission)) requestWithRationale(rationale, permissionRequestLauncher)
    else permissionRequestLauncher.launch(permission)
}

internal fun Activity.requestWithRationale(permission: String, requestLauncher: ActivityResultLauncher<String>) {
    // show rationale
    requestLauncher.launch(permission)
}

internal fun Context.hasPermission(vararg permission: String): Boolean =
    permission.all { perm ->
        checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED
    }

internal fun showRationale(rationale: String) {

}