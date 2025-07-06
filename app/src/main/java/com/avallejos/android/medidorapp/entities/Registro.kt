package com.avallejos.android.medidorapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class Registro (
    @PrimaryKey(autoGenerate = true ) var id:Long? = null,
    val medidor: Int,
    val fecha: LocalDate,
    val tipo: String //radioOption
)