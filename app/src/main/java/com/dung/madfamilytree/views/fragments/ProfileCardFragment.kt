package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ProfileCardBinding
import com.dung.madfamilytree.dtos.ProfileDTO
import com.dung.madfamilytree.utility.Utility
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileCardFragment : Fragment() {
    private var _binding: ProfileCardBinding? = null
    private val binding get() = _binding!!
    private val args: ProfileCardFragmentArgs by navArgs()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val TAG = "ProfileCardFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProfileData()
        setupSaveButton()
    }

    private fun setupProfileData() {
        // Set profile data from arguments
        binding.etNameGiaPha.setText(args.profileName)
        binding.etName.setText(args.profileName)
        binding.etAnotherName.setText(args.profileAnotherName)
        binding.etGender.setText(args.profileGender)
        binding.etDateOfBirth.setText(args.profileDateOfBirth)
        binding.etPhoneNumber.setText(args.profilePhoneNumber)
        binding.etEducationalLevel.setText(args.profileEducationalLevel)
        binding.etJob.setText(args.profileJob)
        binding.etProvince1.setText(args.profileProvince1)
        binding.etDistrict1.setText(args.profileDistrict1)
        binding.etCommune1.setText(args.profileCommune1)
        binding.etProvince2.setText(args.profileProvince2)
        binding.etDistrict2.setText(args.profileDistrict2)
        binding.etCommune2.setText(args.profileCommune2)

        // Setup marital status spinner
        val maritalStatuses = arrayOf("Độc thân", "Đã kết hôn", "Ly hôn", "Góa")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, maritalStatuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMaritalStatus.adapter = adapter
        
        // Set selected marital status
        val maritalStatusIndex = maritalStatuses.indexOf(args.profileMaritalStatus)
        if (maritalStatusIndex != -1) {
            binding.spinnerMaritalStatus.setSelection(maritalStatusIndex)
        }

        // Make all fields editable
        binding.etName.isEnabled = true
        binding.etAnotherName.isEnabled = true
        binding.etGender.isEnabled = true
        binding.etDateOfBirth.isEnabled = true
        binding.etPhoneNumber.isEnabled = true
        binding.spinnerMaritalStatus.isEnabled = true
        binding.etEducationalLevel.isEnabled = true
        binding.etJob.isEnabled = true
        binding.etProvince1.isEnabled = true
        binding.etDistrict1.isEnabled = true
        binding.etCommune1.isEnabled = true
        binding.etProvince2.isEnabled = true
        binding.etDistrict2.isEnabled = true
        binding.etCommune2.isEnabled = true

        // Show save button
        binding.btnSave.visibility = View.VISIBLE
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profileId = args.profileId
                val profile = ProfileDTO(
                    name = binding.etName.text.toString(),
                    another_name = binding.etAnotherName.text.toString(),
                    gender = binding.etGender.text.toString(),
                    date_of_birth = try {
                        val date = dateFormat.parse(binding.etDateOfBirth.text.toString())
                        Timestamp(date ?: Date())
                    } catch (e: Exception) {
                        null
                    },
                    phone_number = binding.etPhoneNumber.text.toString(),
                    marital_status = binding.spinnerMaritalStatus.selectedItem.toString(),
                    educational_level = binding.etEducationalLevel.text.toString(),
                    job = binding.etJob.text.toString(),
                    province1 = binding.etProvince1.text.toString(),
                    district1 = binding.etDistrict1.text.toString(),
                    commune1 = binding.etCommune1.text.toString(),
                    province2 = binding.etProvince2.text.toString(),
                    district2 = binding.etDistrict2.text.toString(),
                    commune2 = binding.etCommune2.text.toString(),
                    died = args.profileDied
                )

                // Update profile in Firestore
                Utility.db?.collection("Profile")
                    ?.document(profileId)
                    ?.set(profile)
                    ?.addOnSuccessListener {
                        Toast.makeText(context, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show()
                        // Navigate back to tree view
                        requireActivity().onBackPressed()
                    }
                    ?.addOnFailureListener { e ->
                        Log.e(TAG, "Error saving profile", e)
                        Toast.makeText(context, "Lỗi khi lưu thông tin", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile", e)
                Toast.makeText(context, "Lỗi khi lưu thông tin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 