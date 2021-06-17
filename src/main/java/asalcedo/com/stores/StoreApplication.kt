package asalcedo.com.stores

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class StoreApplication: Application() {
    // Uso del patrón Singleton para acceder desde cualquier parte de la aplicación a la BDD
    // el uso de la palabra object nos va a configurar el patrón Singleton
    // el uso de la palabra companion nos sirve para poder hacer accesible desde cualquier punto de la aplicación
    companion object{
        lateinit var database: StoreDatabase
    }

    override fun onCreate() {
        super.onCreate()

        //Como añadimos el campo photoUrl tenemos que indicarle a la BDD cuales son los cambios
        //por estandar se crea una constante que indique de que version a que version se migra
        val MIGRATION_1_2 = object: Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                // con esta senetencia TEXT NOT NULL DEFAULT '' le indicamos que para los demás registros que no tienen datos en ese nuevo campo
                // les ponga un string vacío
                database.execSQL("ALTER TABLE StoreEntity ADD COLUMN photoUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        database = Room.databaseBuilder(this, StoreDatabase::class.java, "StoreDatabase")
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}