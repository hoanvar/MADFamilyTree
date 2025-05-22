package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                binding.etName.setText(doc.getString("name"))
                binding.etName2.setText(doc.getString("name2"))
                binding.etGender.setText(doc.getString("gender"))
//                binding.etDateOfBirth.setText(doc.getString("date_of_birth"))
                val dob = doc.getTimestamp("date_of_birth")?.toDate()
                val dobStr = dob?.let {
                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it)
                } ?: ""
                binding.etDateOfBirth.setText(dobStr)

                binding.etMarried.setText(doc.getString("Married"))
                binding.etEducation.setText(doc.getString("education"))
                binding.etJob.setText(doc.getString("job"))
                binding.etAddress1.setText(doc.getString("address"))
                binding.etJob.setText(doc.getString("job"))
                binding.etAddress2.setText(doc.getString("address2"))
                binding.etStatus.setText(doc.getString("status"))
//                binding.etDied.setText(doc.getString("died"))
                val isDead = doc.getLong("died") == 1L
                binding.switch1.isChecked = isDead
                binding.layoutDeathInfo.visibility = if (isDead) View.VISIBLE else View.GONE

                if (isDead) {
                    val diedDate = doc.getTimestamp("died_date")?.toDate()
                    val diedStr = diedDate?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    } ?: ""

                    val deathAnniversary = doc.getString("death_anniversary") ?: ""

                    binding.ngaymat2.text = diedStr
                    binding.ngaygio2.text = deathAnniversary
                }



                // ... và các field khác
            }
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
