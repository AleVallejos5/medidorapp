package com.avallejos.android.medidorapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.avallejos.android.medidorapp.dao.RegistroDao
import com.avallejos.android.medidorapp.entities.LocalDateConverter
import com.avallejos.android.medidorapp.entities.Registro

@Database(entities = [Registro::class], version = 1)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun registroDao(): RegistroDao

}