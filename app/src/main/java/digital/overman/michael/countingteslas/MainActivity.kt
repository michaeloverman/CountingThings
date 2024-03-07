package digital.overman.michael.countingteslas

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import digital.overman.michael.countingteslas.data.LocationUtil
import digital.overman.michael.countingteslas.data.Tesla
import digital.overman.michael.countingteslas.domain.TeslaColor
import digital.overman.michael.countingteslas.domain.TeslaTallyViewModel
import digital.overman.michael.countingteslas.ui.theme.CountingTeslasTheme
import digital.overman.michael.countingteslas.util.AppDataTextColor
import digital.overman.michael.countingteslas.util.contrastColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CountingTeslasTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    ButtonScreenWrapper()
                }
            }
        }


        Timber.d("PLAY ${LocationUtil.available(this)}")
    }

}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ButtonScreenWrapper(viewModel: TeslaTallyViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var enterColor by remember { mutableStateOf(false) }
    val locationPermissionState = rememberMultiplePermissionsState(permissions = listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ))
    val scope = rememberCoroutineScope()

    fun addTeslaWithLocation(color: TeslaColor) {
        Timber.d("New Tesla: $color")
        @SuppressLint("MissingPermission")
        if (locationPermissionState.allPermissionsGranted) {
            scope.launch(Dispatchers.IO) {
                val result = locationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()
                result?.let { location ->
                    Timber.d("Location acquired: $location")
                    viewModel.teslaSpotted(color, location)
                }
            }
        } else locationPermissionState.launchMultiplePermissionRequest()
    }

    ButtonScreen(
        buttons = TeslaColor.all.map {
            ButtonData(
                name = it.colorString,
                count = viewModel.teslasByColor(it).collectAsState(emptyList()).value.size,
                background = it.screenColor
            ) {
                Timber.d("calling addTeslaWithLocation")
                addTeslaWithLocation(it)
            }
        },
        lastItem = viewModel.lastTesla.collectAsState(null).value,
        totalCount = viewModel.teslas.collectAsState(emptyList()).value.size,
        lastAddress = viewModel.lastAddress.collectAsState(null).value,
        undo = {
            Timber.d("removing last added")
            viewModel.removeLast()
        }
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ButtonScreen(
    modifier: Modifier = Modifier,
    buttons: List<ButtonData>,
    lastItem: Tesla?,
    lastAddress: String?,
    totalCount: Int,
    undo: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        LazyVerticalGrid(modifier = Modifier.weight(1f), columns = GridCells.Fixed(2)) {
            items(buttons) { item ->
                Timber.d("adding $item")
                TeslaButton(
                    name = item.name.capitalize(locale = Locale.current),
                    count = item.count,
                    background = item.background,
                    onClick = {
                        Timber.d("button clicked: $this")
                        item.onClick()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LastItem(lastItem, lastAddress)
        TeslaCountText(totalCount)

        Spacer(
            Modifier
                .height(1.dp)
//                .weight(0.1f)
        )

        TeslaButton(
            name = "Remove Last",
            count = -1,
            background = lastItem?.color?.screenColor ?: Color.White,
            onBackground = lastItem?.color?.screenColor?.contrastColor() ?: Color.Black,
            circular = false,
            enabled = lastItem != null
        ) {
            Timber.d("calling undo()")
            undo()
        }
    }

//    if (showProgressSpinner) {
//        CircularProgressIndicator(
//            modifier = Modifier.fillMaxSize().width(64.dp),
//            color = MaterialTheme.colorScheme.surfaceVariant,
//            strokeWidth = 2.dp
//        )
//    }
//    if (enterColor) {
//        var customColor by remember { mutableStateOf("") }
//        EnterColorDialog(
//            title = { Text("Enter Color")},
//            content = { OutlinedTextField(value = "", onValueChange = { color -> customColor = color})},
//            dismissButton = {customColor = ""},
//            confirmButton = {viewModel.teslaSpotted(customColor)}) {
//
//            customColor = ""
//            enterColor = false
//        }
//    }
}
@Composable
fun TeslaCountText(count: Int) {
    Text("Total: $count", color = AppDataTextColor)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun LastItem(lastTesla: Tesla?, address: String?) {
    Text(text = if (lastTesla != null) "${lastTesla.color}: $address" else "No last tesla", color = AppDataTextColor)
}
data class ButtonData(
    val name: String,
    val count: Int,
    val background: Color,
    val onClick: () -> Unit
)

@Composable
fun TeslaButton(
    modifier: Modifier = Modifier,
    name: String = "",
    count: Int,
    background: Color,
    onBackground: Color = background.contrastColor(),
    circular: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var displayCount = count

    Button(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp)
            .then(if (circular) modifier.aspectRatio(1f) else modifier),
        colors = ButtonDefaults.buttonColors(containerColor = background, contentColor = onBackground, disabledContainerColor = background.copy(alpha = 0.5F)),
        shape = CircleShape,
        contentPadding = PaddingValues(if (circular) 4.dp else 10.dp),
        enabled = enabled,
        onClick = {
            Timber.d("TeslaButton composable click registered")
            displayCount++
            onClick()
        }
    ) {
        if (name != "" && !circular) {
            Text(
                text = name,
                modifier = modifier.weight(1f),
                fontSize = 32.sp
            )
        }
        AnimatedContent(
            targetState = displayCount,
            modifier = modifier,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInVertically { 100 } + fadeIn() togetherWith
                            slideOutVertically { -100 } + fadeOut()
                } else {
                    slideInVertically { -100 } + fadeIn() togetherWith
                            slideOutVertically { 100 } + fadeOut()
                }
            }, label = "odometer"
        ) { displayCount ->
            Text(
                text = displayCount.toString(),
                modifier = modifier,
                fontSize = 32.sp
            )
        }
    }
}
@Composable
fun EnterColorDialog (
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column {
                Column(Modifier.padding(24.dp)) {
                    title.invoke()
                    Spacer(Modifier.size(16.dp))
                    content.invoke()
                }
                Spacer(Modifier.size(4.dp))
                Row(
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    dismissButton.invoke()
                    confirmButton.invoke()
                }
            }
        }
    }
}
@Preview
@Composable
fun ButtonLayoutPreview(viewModel: TeslaTallyViewModel = hiltViewModel()) {
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(TeslaColor.all) { color ->
            TeslaButton(
                name = color.colorString.capitalize(locale = Locale.current),
                count = viewModel.teslasByColor(color).collectAsState(
                    emptyList()
                ).value.size,
                background = color.screenColor,
                onBackground = color.screenColor.contrastColor()
            ) { }
        }
    }
}
@Preview
@Composable
fun TeslaBlackButtonPreview() {
    TeslaButton(name = "Black", count = 419, background = Color.Black, onBackground = Color.Black.contrastColor()) { }
}
@Preview
@Composable
fun TeslaWhiteButtonPreview() {
    TeslaButton(name = "White", count = 419, background = Color.White, onBackground = Color.White.contrastColor()) { }
}