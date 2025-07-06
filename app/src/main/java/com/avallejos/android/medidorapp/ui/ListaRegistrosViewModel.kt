package com.avallejos.android.medidorapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.avallejos.android.medidorapp.Aplicacion
import com.avallejos.android.medidorapp.dao.RegistroDao
import com.avallejos.android.medidorapp.entities.Registro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListaRegistrosViewModel( private val registroDao: RegistroDao): ViewModel() {

    var registros by mutableStateOf(listOf<Registro>())
        private set

    init {
        obtenerRegistros()
    }

    // Corrutina para insertar registros
    fun insertarRegistro(registro: Registro) {
        viewModelScope.launch(Dispatchers.IO) {
            registroDao.insertar(registro)
            obtenerRegistros()  // Actualiza la lista después de insertar
        }
    }

    // Corrutina para obtener registros
    private fun obtenerRegistros()
    //: List<Registro>
    {
        viewModelScope.launch(Dispatchers.IO) {
            registros = registroDao.obtenerTodos()
        }
        //return registros
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                //val app = (this[APPLICATION_KEY] as Aplicacion)
                //ListaRegistrosViewModel(app.registroDao)
                val aplicacion = (this[APPLICATION_KEY] as Aplicacion)
                ListaRegistrosViewModel(aplicacion.registroDao)
            }
        }
    }
}