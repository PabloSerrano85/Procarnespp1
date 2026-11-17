package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        PersonaEntity::class,
        AdjuntoPersonaEntity::class,
        OperadorEntity::class,
        RequisitoEntity::class,
        AdjuntoEntity::class,
        CertificadoEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class RoccaDatabase : RoomDatabase() {
    abstract fun roccaDao(): RoccaDao

    companion object {
        @Volatile
        private var INSTANCE: RoccaDatabase? = null

        fun getInstance(context: Context): RoccaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RoccaDatabase::class.java,
                    "rocca_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
