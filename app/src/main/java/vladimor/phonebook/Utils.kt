package vladimor.phonebook

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

class Utils {
    companion object {
        private val random by lazy { Random(Calendar.getInstance().time.time) }

        fun giveMeAName(): String {
            val familyNames = listOf(
                "Smith", "Johnson", "Brown", "Taylor", "Miller",
                "Davis", "Garcia", "Wilson", "Martinez", "Anderson",
                "Thompson", "Thomas", "Hernandez", "Moore", "Jackson",
                "Martin", "Lee", "Perez", "Walker", "Harris",
                "Clark", "Lewis", "Robinson", "Young", "Allen",
                "King", "Wright", "Scott", "Green", "Baker",
                "Adams", "Nelson", "Carter", "Mitchell", "Perez",
                "Roberts", "Turner", "Phillips", "Campbell", "Parker",
                "Evans", "Edwards", "Collins", "Stewart", "Sanchez",
                "Morris", "Rogers", "Reed", "Cook", "Bailey",
                "Lund", "Schmidt", "Poulsen", "Olsen", "Larsen",
                "Pedersen", "Jensen", "Nielsen", "Hansen", "Petersen",
                "Madsen", "Petkov", "Sirilen", "Czarski", "Petrov",
                "Christoffersen"
            )

            val names = listOf(
                "Sofia", "Erika", "Gabriella", "Anja", "Antonia",
                "Daniela", "Elvira", "Leila", "Alina", "Emma",
                "Laura", "Isabella", "Andrea", "Beatrice", "Martina",
                "Valentina", "Vera", "Marlene", "Emilia", "Selena",
                "Patricia", "Marta", "Miriam", "Adele", "Eleonora",
                "Bianca", "Maria", "Melanie", "Tessa", "Arianna",
                "Franziska", "Livia", "Johanna", "Vanessa", "Elena",
                "Luisa", "Angelika", "Sarah", "Annika", "Olivia",
                "Lisa", "Clara", "Ina", "Sina", "Alice",
                "Tanja", "Elisa", "Linda", "Ramona"
            )

            return "${names.random(random)} ${familyNames.random(random)}"
        }

        private tailrec fun Context.getActivity(): Activity? = this as? Activity
            ?: (this as? ContextWrapper)?.baseContext?.getActivity()

        private val showedPermissionInfo: HashMap<String, Boolean> = HashMap()

        lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

        fun needPermission(
            context: Context,
            permission: String,
            infoResId: Int? = null
        ) {
            val activity = context.getActivity() ?: return

            val readCallLogPermissionResult =
                context.checkSelfPermission(permission)
            if (readCallLogPermissionResult == PackageManager.PERMISSION_GRANTED) {

            } else if (infoResId != null && shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            ) {
                if (showedPermissionInfo[permission] != true) {
                    Toast.makeText(
                        context,
                        infoResId,
                        Toast.LENGTH_LONG
                    ).show()
                    showedPermissionInfo[permission] = true
                }
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}