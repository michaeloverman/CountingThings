package digital.overman.michael.countingteslas.domain

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.overman.michael.countingteslas.data.Item
import digital.overman.michael.countingteslas.data.ItemRepository
import digital.overman.michael.countingteslas.data.Tesla
import digital.overman.michael.countingteslas.data.TeslaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@HiltViewModel
class TeslaTallyViewModel @Inject constructor(
    private val repository: TeslaRepository,
    private val iRepository: ItemRepository,
    private val application: Application,
) : AndroidViewModel(application) {

    val teslas = repository.getTeslas()
    fun teslasByColor(color: TeslaColor): Flow<List<Tesla>> = repository.getTeslasByColor(color.colorString)

    val items = iRepository.getItems()
    fun itemsByTitle(title: String): Flow<List<Item>> = iRepository.getItemsByTitle(title)

//    private var lastTeslaId: Long? = null
    private var _lastTesla = MutableStateFlow<Tesla?>(null)
    val lastTesla = _lastTesla.asStateFlow()
    private var _lastItem = MutableStateFlow<Item?>(null)
    val lastItem = _lastItem.asStateFlow()

    private var _lastAddress = MutableStateFlow<String?>(null)
    val lastAddress = _lastAddress.asStateFlow()
    private var _lastItemAddress = MutableStateFlow<String?>(null)
    val lastItemAddress = _lastItemAddress.asStateFlow()

    init {
        viewModelScope.launch {
            _lastTesla.value = repository.getLast()
            getAddressFromLatLng()
            _lastItem.value = iRepository.getLast()
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun teslaSpotted(color: TeslaColor, location: Location?) {
        Timber.d("NewTesla: $color, $location")
//        val tesla = Tesla(color = color,
//            subColor = when (color) {
//                TeslaColor.Other -> "specific color"
//                else -> ""
//            },
//            location = location?.let { LatLng(location.latitude, location.longitude) } ?: LatLng(0.0,0.0)
//        )
//        insertTesla(tesla)
        itemCounted(title = color.colorString,
                    subTitle = when (color) {
                        TeslaColor.Other -> "specific color"
                        else -> null
                    },
                    location = location
        )
    }
    fun itemCounted(title: String, subTitle: String? = null, location: Location? = null) {
        Timber.d("New item: $title, $location")
        val item = Item(
            title = title,
            subTitle = subTitle,
            location = location?.let { LatLng(location.latitude, location.longitude) }
        )
        insertItem(item)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun insertTesla(tesla: Tesla) {
        viewModelScope.launch {
            repository.insertTesla(tesla)
            _lastTesla.value = tesla
            getAddressFromLatLng()
        }
    }
    private fun insertItem(item: Item) {
        viewModelScope.launch {
            iRepository.insertItem(item)
            _lastItem.value = item
            getAddressFromLatLng()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun removeLast() {
        Timber.d("Removing lastTesla")
        lastTesla.value?.timestamp?.let {
            viewModelScope.launch {
                repository.removeByTimestamp(it)
                _lastTesla.value = repository.getLast()
                getAddressFromLatLng()
            }
        }
    }
    fun removeLastItem() {
        Timber.d("Removing lastItem")
        lastItem.value?.timestamp?.let {
            viewModelScope.launch {
                iRepository.removeByTimestamp(it)
                _lastItem.value = iRepository.getLast()
//                getAddressFromLatLng()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getAddressFromLatLng() {
        val geocoder = Geocoder(application.applicationContext, Locale.getDefault())
        val location = lastTesla.value?.location
        if (location == null) {
            _lastAddress.value = null
            return
        }
        geocoder.getFromLocation(location.latitude, location.longitude, 1, @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (addresses.isNotEmpty()) {
                    val address: Address = addresses[0]
                    // Here, you can extract various address components like addressLine, locality, etc.
                    Timber.d("ADDRESS:\n$address")
                    _lastAddress.value = "${address.featureName} ${address.thoroughfare}, ${address.locality}"
                }
            }

            override fun onError(errorMessage: String?) {
                _lastAddress.value = null
            }
        } )
    }
}

sealed class TeslaColor(val ordinal: Int, val colorString: String, val screenColor: Color) {
    data object White : TeslaColor(1, "white", Color.White)
    data object Black : TeslaColor(2, "black", Color.Black)
    data object Red : TeslaColor(3, "red", Color.Red)
    data object Gray : TeslaColor(4, "gray", Color.Gray)
    data object Blue : TeslaColor(5, "blue", Color.Blue)
    data object Other : TeslaColor(6, "other", Color.Green)

    companion object {
        val all = TeslaColor::class.sealedSubclasses.map { it.objectInstance as TeslaColor }.sortedBy { it.ordinal }
    }
}