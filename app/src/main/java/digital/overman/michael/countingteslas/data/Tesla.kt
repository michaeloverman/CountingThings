package digital.overman.michael.countingteslas.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import digital.overman.michael.countingteslas.domain.TeslaColor

data class Tesla(
    val color: TeslaColor,
    val subColor: String = "",
    var location: LatLng,
    var timestamp: Long? = null,
) {
    init {
        if (timestamp == null) timestamp = System.currentTimeMillis()
    }

    fun toTeslaEntity(): TeslaEntity {
        return TeslaEntity(
            color.colorString,
            subColor,
            location.latitude,
            location.longitude,
            timestamp!!
        )
    }
}

data class Item(
    val title: String,
    val subTitle: String? = null,
    var location: LatLng? = null,
    var timestamp: Long? = null,
) {
    init {
        if (timestamp == null) timestamp = System.currentTimeMillis()
    }

    fun toItemEntity(): ItemEntity {
        return ItemEntity(
            title,
            subTitle,
            location?.latitude,
            location?.longitude,
            timestamp!!
        )
    }
}

@Entity
data class ItemEntity(
    val title: String,
    val subTitle: String?,
    var latitude: Double?,
    var longitude: Double?,
    val timestamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Long? = null
) {
    fun toItem(): Item {
        return Item(
            title,
            subTitle,
            latitude?.let { lat -> longitude?.let { long -> LatLng(lat, long) } },
            timestamp
        )
    }
}

@Entity
data class TeslaEntity(
    val color: String,
    val subColor: String,
    var latitude: Double,
    var longitude: Double,
    val timestamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Long? = null
) {
    fun toTesla(): Tesla {
        return Tesla(
            when(color) {
                "white" -> TeslaColor.White
                "black" -> TeslaColor.Black
                "red" -> TeslaColor.Red
                "gray" -> TeslaColor.Gray
                "blue" -> TeslaColor.Blue
                else -> TeslaColor.Other
            },
            subColor,
            LatLng(latitude, longitude),
            timestamp
        )
    }
}