package vladimor.phonebook.database

import androidx.room.*
import com.google.gson.Gson

private fun getRuby(str: String): String {
    val ruby = str.lowercase()

    if (ruby == "") {
        return "#$str"
    } else if (!(ruby.first() in 'a'..'z')) {
        return "#$ruby"
    } else {
        return ruby
    }
}

data class CustomItem(
    val tag: String,
    val value: String
)

class Converters {
    @TypeConverter
    fun listToJson(value: List<CustomItem>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<CustomItem>::class.java).toList()
}

@Entity(indices = [Index("ruby")])
data class Address(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val ruby: String = getRuby(name),
    val customs: List<CustomItem> = listOf()
)