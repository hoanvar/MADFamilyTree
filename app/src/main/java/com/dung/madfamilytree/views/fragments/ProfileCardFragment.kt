package com.dung.madfamilytree.views.fragments

import AddressManager
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ProfileCardBinding
import com.dung.madfamilytree.dtos.ProfileDTO
import com.dung.madfamilytree.utility.Utility

import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import SupabaseClientProvider

class ProfileCardFragment : Fragment() {

    private var _binding: ProfileCardBinding? = null
    private val binding get() = _binding!!
    private val args: ProfileCardFragmentArgs by navArgs()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val addressManager = AddressManager()
    private val TAG = "ProfileCardFragment"
    private var selectedAvatarUri: Uri? = null

    private lateinit var editBiographyLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ProfileCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProfileData()
        setupSpinners()
        setupSaveButton()
        setupImagePicker()

        // Đăng ký nhận kết quả từ EditBiographyActivity
        editBiographyLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val newBio = result.data?.getStringExtra("updatedBiography") ?: ""
                binding.tvBiography.text = newBio
            }
        }

        binding.switchDeceased.setOnCheckedChangeListener { _, isChecked ->
            binding.deceasedInfoLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.btnEditBiography.setOnClickListener {
            val intent = Intent(requireContext(), com.dung.madfamilytree.views.activities.EditBiographyActivity::class.java).apply {
                putExtra("profileId", args.profileId)
                putExtra("currentBiography", binding.tvBiography.text.toString())
            }
            editBiographyLauncher.launch(intent)
        }

        binding.btnAddAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupImagePicker() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedAvatarUri = it
                Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.profile_icon)
                    .into(binding.profileAvatar)
            }
        }
    }

    private fun setupProfileData() {
        binding.etName.setText(args.profileName)
        binding.etAnotherName.setText(args.profileAnotherName)
        binding.etDateOfBirth.setText(args.profileDateOfBirth)
        binding.etPhoneNumber.setText(args.profilePhoneNumber)
        binding.etJob.setText(args.profileJob)
        binding.switchDeceased.isChecked = args.profileDied == 1
        binding.tvBiography.text = args.profileBiography ?: "Chưa có tiểu sử"

        Log.d("test uri", args.profileAvatarUrl)

        // Load avatar if exists
        args.profileAvatarUrl?.let { avatarUrl ->
            if (avatarUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.profile_icon)
                    .into(binding.profileAvatar)
            }
        }

        val maritalStatuses = arrayOf("Độc thân", "Đã kết hôn", "Ly hôn", "Góa")
        binding.spinnerMaritalStatus.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, maritalStatuses)
        args.profileMaritalStatus?.let {
            val index = maritalStatuses.indexOf(it)
            if (index >= 0) binding.spinnerMaritalStatus.setSelection(index)
        }

        val educationLevels = arrayOf("Tiểu học", "THCS", "THPT", "Đại học", "Thạc sĩ", "Tiến sĩ")
        binding.spinnerEducation.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, educationLevels)

        val genders = arrayOf("Nam", "Nữ", "Khác")
        binding.spinnerGender.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genders)
    }

    private fun setupSpinners() {
        lifecycleScope.launch {
            try {
                val provinces = addressManager.loadProvinces()
                val provinceNames = provinces.map { it.name }

                val provinceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, provinceNames)
                binding.spinnerProvince1.adapter = provinceAdapter
                binding.spinnerProvince2.adapter = provinceAdapter

                binding.spinnerProvince1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val provinceCode = provinces[position].code
                        val districts = addressManager.getDistricts(provinceCode)
                        val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts.map { it.name })
                        binding.spinnerDistrict1.adapter = districtAdapter

                        binding.spinnerDistrict1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                                val districtCode = districts[pos].code
                                val wards = addressManager.getWards(provinceCode, districtCode)
                                val wardAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wards.map { it.name })
                                binding.spinnerCommune1.adapter = wardAdapter
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                binding.spinnerProvince2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val provinceCode = provinces[position].code
                        val districts = addressManager.getDistricts(provinceCode)
                        val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts.map { it.name })
                        binding.spinnerDistrict2.adapter = districtAdapter

                        binding.spinnerDistrict2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                                val districtCode = districts[pos].code
                                val wards = addressManager.getWards(provinceCode, districtCode)
                                val wardAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wards.map { it.name })
                                binding.spinnerCommune2.adapter = wardAdapter
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading address data", e)
                Toast.makeText(context, "Lỗi khi tải địa chỉ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                var avatarUrl = args.profileAvatarUrl

                // Upload new avatar if selected
                selectedAvatarUri?.let { uri ->
                    try {
                        avatarUrl =
                            SupabaseClientProvider.uploadImageFromUri(requireContext(), uri).toString()
                    } catch (e: Exception) {
                        Log.e(TAG, "Lỗi khi tải lên avatar", e)
                        Toast.makeText(context, "Không thể tải lên avatar", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }

                val profile = ProfileDTO(
                    name = binding.etName.text.toString(),
                    another_name = binding.etAnotherName.text.toString(),
                    date_of_birth = parseDate(binding.etDateOfBirth.text.toString()),
                    phone_number = binding.etPhoneNumber.text.toString(),
                    job = binding.etJob.text.toString(),
                    province1 = binding.spinnerProvince1.selectedItem?.toString() ?: "",
                    district1 = binding.spinnerDistrict1.selectedItem?.toString() ?: "",
                    commune1 = binding.spinnerCommune1.selectedItem?.toString() ?: "",
                    province2 = binding.spinnerProvince2.selectedItem?.toString() ?: "",
                    district2 = binding.spinnerDistrict2.selectedItem?.toString() ?: "",
                    commune2 = binding.spinnerCommune2.selectedItem?.toString() ?: "",
                    died = if (binding.switchDeceased.isChecked) 1 else 0,
                    date_of_death = parseDate(binding.etDateOfDeath.text.toString()),
                    death_anniversary = binding.etDeathAnniversary.text.toString(),
                    age_at_death = binding.etAgeAtDeath.text.toString().toIntOrNull(),
                    burial_info = binding.etBurialInfo.text.toString(),
                    biography = binding.tvBiography.text.toString(),
                    avatar_url = avatarUrl
                )

                Utility.db?.collection("Profile")
                    ?.document(args.profileId)
                    ?.set(profile)
                    ?.addOnSuccessListener {
                        Toast.makeText(context, "Lưu thành công", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                    ?.addOnFailureListener { e ->
                        Log.e(TAG, "Lỗi khi lưu", e)
                        Toast.makeText(context, "Không thể lưu thông tin", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Lỗi không xác định", e)
                Toast.makeText(context, "Lỗi khi lưu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseDate(dateStr: String): Timestamp? {
        return try {
            val date = dateFormat.parse(dateStr)
            Timestamp(date ?: Date())
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
