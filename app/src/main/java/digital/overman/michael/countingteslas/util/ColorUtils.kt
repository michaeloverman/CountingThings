package digital.overman.michael.countingteslas.util

import androidx.compose.ui.graphics.Color
import timber.log.Timber

// Counting the perceptive luminance - human eye favors green color...
fun Color.contrastColor() =
    if (0.299 * this.red + 0.587 * this.green + 0.114 * this.blue > 0.5)
        Color.Black else Color.White

internal val AppDataTextColor = Color.Yellow