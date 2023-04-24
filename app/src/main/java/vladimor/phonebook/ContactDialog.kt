package vladimor.phonebook

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import vladimor.phonebook.database.Address
import vladimor.phonebook.database.CustomItem
import vladimor.phonebook.databinding.ContactDialogBinding
import vladimor.phonebook.databinding.ContactFullscreenBinding
import vladimor.phonebook.databinding.CustomContactItemInputBinding
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val REQUEST_CAMERA_PERMISSION = 2

class ContactDialog(
    private val addressVM: AddressListViewModel,
    private val isChange: Boolean = false,
    private val oldAddress: Address? = null
) :
    DialogFragment() {

    private val customItems: ArrayList<CustomContactItemInputBinding> = ArrayList()

    private var _binding: ContactDialogBinding? = null
    private val binding get() = _binding!!

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as Bitmap
                binding.profilePicture.setImageBitmap(bitmap)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val newContact = ContactFullscreenBinding.inflate(inflater, container, false)
        val binding = ContactDialogBinding.bind(newContact.contactDialogInclude.root)
        val appBar = newContact.topAppBar
        enterTransition = androidx.transition.Slide(Gravity.END)
        exitTransition = androidx.transition.Slide(Gravity.END)
        bindDialog(binding)
        appBar.title = getString(
            if (isChange) {
                R.string.modify_contact
            } else {
                R.string.new_contact
            }
        )
        appBar.setNavigationOnClickListener {
            closeFullscreenDialog()
        }
        appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_save -> {
                    saveChange(binding)
                    closeFullscreenDialog()
                    true
                }
                else -> false
            }
        }

        binding.cameraButton.setOnClickListener {
            if (hasCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        (context as Activity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        return newContact.root
    }

    private fun closeFullscreenDialog() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        parentFragmentManager.popBackStack()
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return activity?.let {
            val newContact = ContactDialogBinding.inflate(requireActivity().layoutInflater)
            bindDialog(newContact)
            val dialog = MaterialAlertDialogBuilder(it)
                .setTitle(
                    if (isChange) {
                        R.string.new_contact
                    } else {
                        R.string.modify_contact
                    }
                )
                .setView(newContact.root)
                .setNeutralButton(R.string.cancel) { _, _ ->
                    // Do Nothing
                }.setPositiveButton(R.string.ok) { _, _ ->
                    saveChange(newContact)
                }.create()
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun saveChange(binding: ContactDialogBinding) {
        val name = binding.nameInput.text.toString()
        val phone = binding.phoneInput.text.toString()
        val email = binding.emailInput.text.toString()
        val customItemList = customItems.map {
            val tag = it.tagInput.text.toString()
            val value = it.valueInput.text.toString()
            CustomItem(tag, value)
        }.filter { it.tag != "" || it.value != "" }.toList()
        if (name != "") {
            val newAddress = Address(
                name = name,
                phone = phone,
                email = email,
                customs = customItemList
            )
            if (isChange) {
                addressVM.update(newAddress.copy(id = oldAddress!!.id))
            } else {
                addressVM.insert(newAddress)
            }
        }
    }

    private fun bindDialog(binding: ContactDialogBinding) {
        val name = binding.nameInput
        val phone = binding.phoneInput
        val email = binding.emailInput
        if (isChange) {
            name.setText(oldAddress!!.name)
            phone.setText(oldAddress.phone)
            email.setText(oldAddress.email)
            val viewGroup = binding.custom as ViewGroup
            for (item in oldAddress.customs) {
                val itemView = CustomContactItemInputBinding.inflate(layoutInflater)
                itemView.tagInput.setText(item.tag)
                itemView.valueInput.setText(item.value)
                itemView.delete.setOnClickListener {
                    viewGroup.removeView(itemView.root)
                    customItems.remove(itemView)
                }
                customItems.add(itemView)
                viewGroup.addView(itemView.root)
            }
        }
        binding.newCustomItem.setOnClickListener {
            val viewGroup = binding.custom as ViewGroup
            val item = CustomContactItemInputBinding.inflate(layoutInflater)
            item.delete.setOnClickListener {
                viewGroup.removeView(item.root)
                customItems.remove(item)
            }
            customItems.add(item)
            viewGroup.addView(item.root)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                "Error!"
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultLauncher.launch(cameraIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}