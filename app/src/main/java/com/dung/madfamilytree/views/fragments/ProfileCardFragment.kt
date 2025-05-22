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
import android.app.AlertDialog

import com.dung.madfamilytree.dtos.Province
import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher

class ProfileCardFragment : Fragment() {

    private var _binding: ProfileCardBinding? = null
    private val binding get() = _binding!!
    private val args: com.dung.madfamilytree.views.fragments.ProfileCardFragmentArgs by navArgs()
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
        binding.etAgeAtDeath.setText(args.profileAgeAtDied.toString())
        binding.etDateOfDeath.setText(args.profileTimeDied)
        binding.etDeathAnniversary.setText(args.profileTimeDiedWas)
        binding.etBurialInfo.setText(args.profileBurialInfo)
        if(binding.switchDeceased.isChecked){
            binding.deceasedInfoLayout.visibility = View.VISIBLE
        }

        //link account
        setupLinkAccountButton()
        setupClaimProfileButton()


        val profileId = args.profileId
        checkLinkStatus(profileId)

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
    // set upClaimProfileButton
    private fun setupClaimProfileButton() {
        val currentUserId = Utility.accountId ?: return
        val currentProfileId = args.profileId

        val accountRef = Utility.db?.collection("Account")?.document(currentUserId)
        val profileQuery = Utility.db?.collection("Account")?.whereEqualTo("profileId", currentProfileId)

        accountRef?.get()?.addOnSuccessListener { accountDoc ->
            val myLinkedProfileId = accountDoc.getString("profileId")

            profileQuery?.get()?.addOnSuccessListener { querySnapshot ->
                val isProfileLinkedToSomeoneElse = querySnapshot.documents.any { it.id != currentUserId }

                // Nếu profile đã được liên kết với người khác
                if (isProfileLinkedToSomeoneElse) {
                    binding.btnClaimProfile.visibility = View.GONE
                    binding.btnLinkAccount.visibility = View.GONE
                    return@addOnSuccessListener
                }

                // Nếu tài khoản hiện tại đã gán profileId
                if (myLinkedProfileId == currentProfileId) {
                    binding.btnClaimProfile.visibility = View.GONE
                    updateLinkButtonState("success") // Đã xác nhận
                } else if (myLinkedProfileId.isNullOrEmpty()) {
                    binding.btnClaimProfile.visibility = View.VISIBLE
                    binding.btnClaimProfile.setOnClickListener {
                        claimThisProfile(currentProfileId)
                    }
                } else {
                    binding.btnClaimProfile.visibility = View.GONE
                }
            }
        }
    }

    private fun claimThisProfile(profileId: String) {
        val currentUserId = Utility.accountId ?: return

        Utility.db?.collection("Account")
            ?.document(currentUserId)
            ?.update("profileId", profileId)
            ?.addOnSuccessListener {
                Toast.makeText(requireContext(), "Bạn đã xác nhận profile này là của mình", Toast.LENGTH_SHORT).show()
                Utility.myProfileId = profileId
                updateLinkButtonState("success") // Cập nhật trạng thái nút
                binding.btnClaimProfile.visibility = View.GONE
            }
            ?.addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi khi xác nhận profile", Toast.LENGTH_SHORT).show()
            }
    }

    // setupLinkAccountButton
    private fun setupLinkAccountButton() {
        binding.btnLinkAccount.setOnClickListener {
            showLinkAccountDialog()
        }
    }
    // kiem tra trang thai lien ket
//    private fun checkLinkStatus(profileId: String) {
//        val currentUserId = Utility.accountId ?: return
//        val linkId = "${currentUserId}_$profileId"
//
//
//        Utility.db?.collection("LinkRequests")
//            ?.document(linkId)
//            ?.get()
//            ?.addOnSuccessListener { doc ->
//                if (doc.exists()) {
//                    when (doc.getString("status")) {
//                        "pending" -> updateLinkButtonState("pending")
//                        "success" -> updateLinkButtonState("success")
//                        "declined" -> updateLinkButtonState("declined")
//                        else -> updateLinkButtonState("default")
//                    }
//                } else {
//                    updateLinkButtonState("default")
//                }
//            }
//    }
    private fun checkLinkStatus(profileId: String) {
        val currentUserId = Utility.accountId ?: return

        Utility.db?.collection("LinkRequests")
            ?.whereEqualTo("fromId", currentUserId)
            ?.whereEqualTo("profileId", profileId)
            ?.get()
            ?.addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val status = documents.first().getString("status")
                    updateLinkButtonState(status ?: "default")
                } else {
                    updateLinkButtonState("default")
                }
            }
            ?.addOnFailureListener {
                updateLinkButtonState("default")
            }
    }

    // Cập nhật trạng thái nút liên kết
    private fun updateLinkButtonState(state: String) {
        when (state) {
            "pending" -> {
                binding.btnLinkAccount.isEnabled = false
                binding.btnLinkAccount.text = "Đang chờ xác nhận"
            }
            "success" -> {
                binding.btnLinkAccount.isEnabled = false
                binding.btnLinkAccount.text = "Đã liên kết"
            }
            "declined" -> {
                binding.btnLinkAccount.isEnabled = true
                binding.btnLinkAccount.text = "Liên kết tài khoản"
            }
            else -> {
                binding.btnLinkAccount.isEnabled = true
                binding.btnLinkAccount.text = "Liên kết tài khoản"
            }
        }
    }

    private fun showLinkAccountDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Liên kết tài khoản")

        val input = android.widget.EditText(requireContext())
        input.hint = "Nhập username người cần liên kết"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Tìm & Gửi yêu cầu") { _, _ ->
            val username = input.text.toString().trim()
            if (username.isNotEmpty()) {
                sendLinkRequestByUsername(username)
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập username", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        builder.setNegativeButton("Hủy", null)

        builder.create().show()
    }


    private fun sendLinkRequestByUsername(username: String) {
        val currentUserId = Utility.accountId ?: return
        val profileId = args.profileId
        val fromProfileId = Utility.myProfileId ?: return

        Utility.db?.collection("Account")
            ?.whereEqualTo("username", username)
            ?.get()
            ?.addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    showNotFoundDialog(username)
                    return@addOnSuccessListener
                }

                val targetUser = documents.documents[0]
                val targetId = targetUser.id

                // Tạo ID duy nhất theo từng profile gửi đi
                val linkId = "${currentUserId}_${targetId}_${args.profileId}_${fromProfileId}"

                // Kiểm tra trạng thái yêu cầu liên kết
                Utility.db?.collection("LinkRequests")
                    ?.document(linkId)
                    ?.get()
                    ?.addOnSuccessListener { doc ->
                        val existingStatus = doc.getString("status")

                        when (existingStatus) {
                            "pending" -> {
                                Toast.makeText(requireContext(), "Đã gửi yêu cầu và đang chờ xác nhận.", Toast.LENGTH_SHORT).show()
                                updateLinkButtonState("pending")
                            }

                            "success" -> {
                                Toast.makeText(requireContext(), "Tài khoản này đã được liên kết.", Toast.LENGTH_SHORT).show()
                                updateLinkButtonState("success")
                            }

                            "declined" -> {
                                Toast.makeText(requireContext(), "Yêu cầu trước đó đã bị từ chối. Gửi lại yêu cầu mới.", Toast.LENGTH_SHORT).show()
                                sendNewLinkRequest(linkId, currentUserId, targetId, profileId)
                            }

                            else -> {
                                sendNewLinkRequest(linkId, currentUserId, targetId, profileId)
                            }
                        }
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(requireContext(), "Lỗi kiểm tra trạng thái liên kết", Toast.LENGTH_SHORT).show()
                    }
            }
            ?.addOnFailureListener {

                Toast.makeText(requireContext(), "Lỗi tìm kiếm username: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun sendNewLinkRequest(linkId: String, fromId: String, toId: String, profileId: String) {


        val linkRequest = hashMapOf(
            "fromId" to fromId,
            "toId" to toId,
            "profileId" to profileId,
            "fromName" to (Utility.accountName ?: "Không rõ"),
            "status" to "pending",
            "timestamp" to Timestamp.now()
        )

        Utility.db?.collection("LinkRequests")
            ?.document(linkId)
            ?.set(linkRequest)
            ?.addOnSuccessListener {

                Toast.makeText(requireContext(), "Đã gửi yêu cầu liên kết", Toast.LENGTH_SHORT).show()
                updateLinkButtonState("pending")
            }
            ?.addOnFailureListener {

                Toast.makeText(requireContext(), "Gửi yêu cầu thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    // Hiển thị thông báo không tìm thấy username
    private fun showNotFoundDialog(username: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Không tìm thấy tài khoản")
            .setMessage("Không có tài khoản nào sử dụng username: $username")
            .setPositiveButton("Thử lại") { dialog, _ -> dialog.dismiss() }
            .show()
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

        // Load avatar if exists
        args.profileAvatarUrl?.let { avatarUrl ->
            if (avatarUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.profile_icon)
                    .into(binding.profileAvatar)
            }
        }

        // Setup marital status spinner
        val maritalStatuses = arrayOf("Độc thân", "Đã kết hôn", "Ly hôn", "Góa")
        val maritalStatusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, maritalStatuses).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerMaritalStatus.adapter = maritalStatusAdapter
        // Set selected value after setting adapter
        args.profileMaritalStatus?.let { status ->
            val index = maritalStatuses.indexOf(status)
            if (index >= 0) {
                binding.spinnerMaritalStatus.setSelection(index, false)
            }
        }

        // Setup education level spinner
        val educationLevels = arrayOf("Tiểu học", "THCS", "THPT", "Đại học", "Thạc sĩ", "Tiến sĩ")
        val educationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, educationLevels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerEducation.adapter = educationAdapter
        // Set selected value after setting adapter
        args.profileEducationalLevel?.let { level ->
            val index = educationLevels.indexOf(level)
            if (index >= 0) {
                binding.spinnerEducation.setSelection(index, false)
            }
        }

        // Setup gender spinner
        val genders = arrayOf("Nam", "Nữ", "Khác")
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genders).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerGender.adapter = genderAdapter
        // Set selected value after setting adapter
        args.profileGender?.let { gender ->
            val index = genders.indexOf(gender)
            if (index >= 0) {
                binding.spinnerGender.setSelection(index, false)
            }
        }
    }

private fun setupSpinners() {
    lifecycleScope.launch {
        try {
            val provinces = addressManager.loadProvinces()
            val provinceNames = provinces.map { it.name }

            // Setup province spinners với adapter
            val provinceAdapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, provinceNames).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            val provinceAdapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, provinceNames).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spinnerProvince1.adapter = provinceAdapter1
            binding.spinnerProvince2.adapter = provinceAdapter2

            // Xử lý địa chỉ nguyên quán (Province 1)
            args.profileProvince1?.let { province1 ->
                val provinceIndex = provinceNames.indexOf(province1)
                if (provinceIndex >= 0) {
                    binding.spinnerProvince1.setSelection(provinceIndex, false)

                    // Load districts cho province 1
                    val provinceCode = provinces[provinceIndex].code
                    val districts = addressManager.getDistricts(provinceCode)
                    val districtNames = districts.map { it.name }
                    val districtAdapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districtNames).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    binding.spinnerDistrict1.adapter = districtAdapter1

                    // Set giá trị cho district 1
                    args.profileDistrict1?.let { district1 ->
                        val districtIndex = districtNames.indexOf(district1)
                        if (districtIndex >= 0) {
                            binding.spinnerDistrict1.setSelection(districtIndex, false)

                            // Load communes cho district 1
                            val districtCode = districts[districtIndex].code
                            val communes = addressManager.getWards(provinceCode, districtCode)
                            val communeNames = communes.map { it.name }
                            val communeAdapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, communeNames).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            binding.spinnerCommune1.adapter = communeAdapter1

                            // Set giá trị cho commune 1
                            args.profileCommune1?.let { commune1 ->
                                val communeIndex = communeNames.indexOf(commune1)
                                if (communeIndex >= 0) {
                                    binding.spinnerCommune1.setSelection(communeIndex, false)
                                }
                            }
                        }
                    }
                }
            }

            // Xử lý địa chỉ hiện tại (Province 2)
            args.profileProvince2?.let { province2 ->
                val provinceIndex = provinceNames.indexOf(province2)
                if (provinceIndex >= 0) {
                    binding.spinnerProvince2.setSelection(provinceIndex, false)

                    // Load districts cho province 2
                    val provinceCode = provinces[provinceIndex].code
                    val districts = addressManager.getDistricts(provinceCode)
                    val districtNames = districts.map { it.name }
                    val districtAdapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districtNames).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    binding.spinnerDistrict2.adapter = districtAdapter2

                    // Set giá trị cho district 2
                    args.profileDistrict2?.let { district2 ->
                        val districtIndex = districtNames.indexOf(district2)
                        if (districtIndex >= 0) {
                            binding.spinnerDistrict2.setSelection(districtIndex, false)

                            // Load communes cho district 2
                            val districtCode = districts[districtIndex].code
                            val communes = addressManager.getWards(provinceCode, districtCode)
                            val communeNames = communes.map { it.name }
                            val communeAdapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, communeNames).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            binding.spinnerCommune2.adapter = communeAdapter2

                            // Set giá trị cho commune 2
                            args.profileCommune2?.let { commune2 ->
                                val communeIndex = communeNames.indexOf(commune2)
                                if (communeIndex >= 0) {
                                    binding.spinnerCommune2.setSelection(communeIndex, false)
                                }
                            }
                        }
                    }
                }
            }

            // Setup listeners cho các thay đổi trong tương lai
            setupSpinnerListeners(provinces)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading address data", e)
            Toast.makeText(context, "Lỗi khi tải địa chỉ", Toast.LENGTH_SHORT).show()
        }
    }
}

    // Tách riêng phần setup listeners để code dễ đọc hơn
    private fun setupSpinnerListeners(provinces: List<Province>) {
        binding.spinnerProvince1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val provinceCode = provinces[position].code
                val districts = addressManager.getDistricts(provinceCode)
                val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts.map { it.name }).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                binding.spinnerDistrict1.adapter = districtAdapter

                binding.spinnerDistrict1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        val districtCode = districts[pos].code
                        val wards = addressManager.getWards(provinceCode, districtCode)
                        val wardAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wards.map { it.name }).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
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
                val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts.map { it.name }).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                binding.spinnerDistrict2.adapter = districtAdapter

                binding.spinnerDistrict2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        val districtCode = districts[pos].code
                        val wards = addressManager.getWards(provinceCode, districtCode)
                        val wardAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, wards.map { it.name }).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        binding.spinnerCommune2.adapter = wardAdapter
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
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
                    gender = binding.spinnerGender.selectedItem?.toString() ?: "",
                    educational_level = binding.spinnerEducation.selectedItem?.toString()?: "",
                    marital_status = binding.spinnerMaritalStatus.selectedItem?.toString()?: "",
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
//                    death_anniversary = binding.etDeathAnniversary.text.toString(),
                    death_anniversary = parseDate(binding.etDeathAnniversary.text.toString()),
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
//                    ?.addOnFailureListener { e ->
//                        Log.e(TAG, "Lỗi khi lưu", e)
//                        Toast.makeText(context, "Không thể lưu thông tin", Toast.LENGTH_SHORT).show()
//                    }

            } catch (e: Exception) {
                Log.e(TAG, "Lỗi không xác định", e)
                Toast.makeText(context, "Lỗi khi lưu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseDate(dateStr: String): Timestamp? {
        return try {
            if (dateStr.isBlank()) return null
            val date = dateFormat.parse(dateStr)
            if (date != null) {
                Timestamp(date)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
