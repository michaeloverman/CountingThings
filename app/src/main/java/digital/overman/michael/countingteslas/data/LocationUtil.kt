package digital.overman.michael.countingteslas.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable
import com.google.android.gms.maps.model.LatLng
import digital.overman.michael.countingteslas.PERM_LOCATION_FINE
import digital.overman.michael.countingteslas.doWithPermission
import timber.log.Timber
import java.io.IOException
import java.util.Locale

object LocationUtil {
    fun available(context: Context): Int {
        return isGooglePlayServicesAvailable(context)
    }

    fun getLocation(activity: AppCompatActivity): Location? {
        val loc: Location? = null
        activity.doWithPermission(
            PERM_LOCATION_FINE,
            "We gotta have this to do the thing",
            onPermissionNotGranted = {
                Timber.d("Permission $PERM_LOCATION_FINE not granted")
            }) {
            Timber.d("Permission granted")
            // do the thing
        }
        return loc
    }

}