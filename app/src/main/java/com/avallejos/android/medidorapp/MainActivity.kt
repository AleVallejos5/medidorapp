package com.avallejos.android.medidorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Info
//import androidx.compose.material.icons.filled.Lightbulb
//import androidx.compose.material.icons.filled.LocalGasStation
//import androidx.compose.material.icons.filled.WaterDrop
//import androidx.compose.material3
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
//import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.avallejos.android.medidorapp.entities.Registro
import com.avallejos.android.medidorapp.ui.ListaRegistrosViewModel
//import com.avallejos.android.medidorapp.R
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar edge-to-edge si es necesario
        enableEdgeToEdge()

        setContent {
            AppRegistrosUI()
        }
    }
}

@Composable
fun AppRegistrosUI(
    navController: NavHostController = rememberNavController(),
    vmListaRegistros: ListaRegistrosViewModel = viewModel(factory = ListaRegistrosViewModel.Factory)
) {
    NavHost(
        navController = navController,
        startDestination = "inicio"
    )
    {
        composable("inicio") {
            PantallaListaRegistros(
                registros = vmListaRegistros.registros,
                onAdd = { navController.navigate("form") }
            )
        }
        composable("form") {
            PantallaFormRegistro(
                vmListaRegistros = vmListaRegistros,
                onRegistroExitoso = { navController.popBackStack()} // Regresa a la pantalla de lista
            )
        }
    }
}

@Composable
fun OpcionesTiposUi( onTipoSeleccionado: (String) -> Unit ){
    val tipos = listOf("Agua", "Luz", "Gas")
    var tipoSeleccionado by rememberSaveable { mutableStateOf(tipos[0]) }

    Column(Modifier.selectableGroup()) {
        tipos.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == tipoSeleccionado),
                        onClick = { tipoSeleccionado = text
                            onTipoSeleccionado(text)
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == tipoSeleccionado),
                    onClick = null // null recommended for accessibility with screenreaders
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

//@Preview(showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormRegistro(
    vmListaRegistros: ListaRegistrosViewModel,
    onRegistroExitoso: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nuevo_registro)) }
            )
        }
    ) { paddingValues ->
        FormularioContenido(
            vmListaRegistros = vmListaRegistros,
            onRegistroExitoso = onRegistroExitoso,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContenido(
    vmListaRegistros: ListaRegistrosViewModel,
    onRegistroExitoso: () -> Unit,
    modifier: Modifier = Modifier
) {
    var medidor by rememberSaveable { mutableIntStateOf(0) }
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var tipo by rememberSaveable { mutableStateOf("Agua") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // obtener el texto del error ANTES de la corrutina
    val mensajeError = stringResource(R.string.mensaje_error)

    // DatePicker Compose
    val datePickerState = rememberDatePickerState()
    val openDatePicker = remember { mutableStateOf(false) }

    if (openDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { openDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    openDatePicker.value = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        fecha = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            TextField(
                value = medidor.toString(),
                onValueChange = { medidor = it.toIntOrNull() ?: 0 },
                label = { Text(stringResource(R.string.btn_registrar_medidor)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { openDatePicker.value = true },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("${stringResource(R.string.seleccionar_fecha)}: $fecha")
                //Text("Seleccionar Fecha: ${fecha.toString()}")
            }

            Text(
                text = stringResource(R.string.tipo_medidor),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
                //text = "Tipo de medidor",
                //style = MaterialTheme.typography.titleMedium,
                //modifier = Modifier.padding(top = 16.dp)
            )

            OpcionesTiposUi { tipoSeleccionado -> tipo = tipoSeleccionado }

            //OpcionesTiposUi(
            //    onTipoSeleccionado = { tipoSeleccionado -> tipo = tipoSeleccionado }
            //)

            Button(
                onClick = {
                    if (medidor <= 0 || tipo.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(mensajeError)
                            //snackbarHostState.showSnackbar(stringResource(R.string.mensaje_error))
                            //snackbarHostState.showSnackbar("Completa todos los campos correctamente")
                        }
                    } else {
                        val nuevoRegistro = Registro(
                            null,
                            medidor,
                            fecha,
                            tipo
                        )
                        vmListaRegistros.insertarRegistro(nuevoRegistro)
                        onRegistroExitoso()
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.guardar))
                //Text("Guardar registro")
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Preview(showSystemUi = true)
//@Composable
//fun PantallaListaRegistros(
//    registros: List<Registro> = listOf(),
//    onAdd:() -> Unit = {}
//){
//    Scaffold(
//        topBar = {
//            androidx.compose.material3.TopAppBar(
//                title = { Text(text = stringResource(R.string.app_name)) }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = onAdd) {
//                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_registrar_medidor))
//            }
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize()
//        ) {
//            items(registros) { registro ->
//                RegistroItem(registro)
//                HorizontalDivider()
//            }
//        }
//    }
//}

//@Preview(showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaRegistros(
    registros: List<Registro>,
    onAdd: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_registrar_medidor))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(registros) { registro ->
                RegistroItem(registro)
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun RegistroItem(registro: Registro) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cambiamos a painterResource para usar las imágenes del drawable
            val icon = when (registro.tipo) {
                "Agua" -> painterResource(id = R.drawable.ic_agua)
                "Luz" -> painterResource(id = R.drawable.ic_luz)
                "Gas" -> painterResource(id = R.drawable.ic_gas)
                else -> painterResource(id = android.R.drawable.ic_menu_help)
            }

            Icon(
                painter = icon,
                contentDescription = registro.tipo,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp)
            )

            Column(Modifier.weight(1f)) {
                Text(
                    text = registro.tipo,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Medición: ${registro.medidor}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Fecha: ${registro.fecha}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

//    // Se ejecuta 1 vez al iniciar el composable
//    LaunchedEffect(Unit) {
//        vmListaRegistros.obtenerRegistros()
//    }

//    LazyColumn {
//        items(vmListaRegistros.registros) {
//            Text(it.tipo)
//        }
//        item {
//            Button(onClick = {
//                vmListaRegistros.insertarRegistro(Registro(null, 15000, LocalDate.now(), "AGUA" ))
//            }) {
//                Text("AGREGAR")
//            }
//        }
//    }
//}


//){
//
//    // se ejecuta 1 vez al iniciar el composable
//    LaunchedEffect(Unit) {
//        vmListaRegistros.obtenerRegistros()
//    }
//
//        item {
//            Button(onClick = {
//                vmListaRegistros.insertarRegistro( Registro(null, 10500, LocalDate.now(), "Agua") )
//            }) {
//                Text("AGREGAR")
//            }
//        }
//    }
//}


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MyAppRegistroMedidorTheme {
//        Greeting("Android")
//    }
//}