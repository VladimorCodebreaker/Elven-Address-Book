package vladimor.phonebook.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Address::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AddressDatabase : RoomDatabase() {
    abstract fun addressDao(): AddressDao

    companion object {
        @Volatile
        private var INSTANCE: AddressDatabase? = null

        fun getDatabase(context: Context? = null): AddressDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context!!.applicationContext,
                    AddressDatabase::class.java,
                    "address_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}