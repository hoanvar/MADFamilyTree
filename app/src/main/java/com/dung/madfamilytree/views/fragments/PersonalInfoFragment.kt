//package com.dung.madfamilytree.views.fragments
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import com.dung.madfamilytree.R
//import com.dung.madfamilytree.databinding.FragmentPersonalInfoBinding
//import com.google.firebase.firestore.FirebaseFirestore
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class PersonalInfoFragment : Fragment() {
//    private lateinit var profileId: String
//    private lateinit var binding: FragmentPersonalInfoBinding
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
//        profileId = arguments?.getString("PROFILE_ID") ?: ""
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        FirebaseFirestore.getInstance().collection("Profile").document(profileId)
//            .get()
//            .addOnSuccessListener { doc ->
//                binding.etName.setText(doc.getString("name"))
//                binding.etName2.setText(doc.getString("name2"))
//                binding.etGender.setText(doc.getString("gender"))
////                binding.etDateOfBirth.setText(doc.getString("date_of_birth"))
//                val dob = doc.getTimestamp("date_of_birth")?.toDate()
//                val dobStr = dob?.let {
//                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it)
//                } ?: ""
//                binding.etDateOfBirth.setText(dobStr)
//
//                binding.etMarried.setText(doc.getString("Married"))
//                binding.etEducation.setText(doc.getString("education"))
//                binding.etJob.setText(doc.getString("job"))
//                binding.etAddress1.setText(doc.getString("address"))
//                binding.etJob.setText(doc.getString("job"))
//                binding.etAddress2.setText(doc.getString("address2"))
////                binding.etStatus.setText(doc.getString("status"))
////                binding.etDied.setText(doc.getString("died"))
//                val isDead = doc.getLong("died") == 1L
//                binding.switch1.isChecked = isDead
//                binding.layoutDeathInfo.visibility = if (isDead) View.VISIBLE else View.GONE
//
//                if (isDead) {
//                    val diedDate = doc.getTimestamp("died_date")?.toDate()
//                    val diedStr = diedDate?.let {
//                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
//                    } ?: ""
//                    binding.ngaymat2.text = diedStr
//                }
//
//            }
//    }
//
//    companion object {
//        fun newInstance(profileId: String): PersonalInfoFragment {
//            val fragment = PersonalInfoFragment()
//            fragment.arguments = Bundle().apply {
//                putString("PROFILE_ID", profileId)
//            }
//            return fragment
//        }
//    }
//}
package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.FragmentPersonalInfoBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class PersonalInfoFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var binding: FragmentPersonalInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        profileId = arguments?.getString("PROFILE_ID") ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FirebaseFirestore.getInstance().collection("Profile").document(profileId)
            .get()
            .addOnSuccessListener { doc ->
                // Xoá hết view cũ nếu có
                binding.infoContainer.removeAllViews()

                addInfo("Họ và tên", doc.getString("name") ?: "")
                addInfo("Tên khác", doc.getString("name2") ?: "")
                addInfo("Giới tính", doc.getString("gender") ?: "")

                val dob = doc.getTimestamp("date_of_birth")?.toDate()
                val dobStr = dob?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: "Không rõ"
                addInfo("Ngày sinh", dobStr)

                addInfo("Tình trạng hôn nhân", doc.getString("marital_status") ?: "")
                addInfo("Trình độ học vấn", doc.getString("educational_level") ?: "")
                addInfo("Nghề nghiệp", doc.getString("job") ?: "")

                // Gộp Nguyên quán từ commune1 + district1 + province1
                val address1 = listOf(
                    doc.getString("commune1"),
                    doc.getString("district1"),
                    doc.getString("province1")
                ).filterNotNull().filter { it.isNotBlank() }.joinToString(", ")
                addInfo("Nguyên quán", address1)

                // Gộp Địa chỉ hiện tại từ commune2 + district2 + province2
                val address2 = listOf(
                    doc.getString("commune2"),
                    doc.getString("district2"),
                    doc.getString("province2")
                ).filterNotNull().filter { it.isNotBlank() }.joinToString(", ")
                addInfo("Địa chỉ hiện tại", address2)


                val isDead = doc.getLong("died") == 1L
                if (isDead) {
                    val diedDate = doc.getTimestamp("date_of_death")?.toDate()
                    val diedStr = diedDate?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    } ?: "Không rõ"
                    addInfo("Ngày mất", diedStr)
                }
            }
    }

    private fun addInfo(label: String, value: String) {
        val itemView = layoutInflater.inflate(R.layout.item_child_card, binding.infoContainer, false)
        itemView.findViewById<TextView>(R.id.tvName).text = label
        itemView.findViewById<TextView>(R.id.tvDetails).text = value
        binding.infoContainer.addView(itemView)
    }

    companion object {
        fun newInstance(profileId: String): PersonalInfoFragment {
            val fragment = PersonalInfoFragment()
            fragment.arguments = Bundle().apply {
                putString("PROFILE_ID", profileId)
            }
            return fragment
        }
    }
}
