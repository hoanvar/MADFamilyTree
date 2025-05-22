package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.FragmentRelationInfoBinding
import com.google.firebase.firestore.FirebaseFirestore

class RelationInfoFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var binding: FragmentRelationInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRelationInfoBinding.inflate(inflater, container, false)
        profileId = arguments?.getString("PROFILE_ID") ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Node")
            .whereEqualTo("id_profile", profileId)
            .get()
            .addOnSuccessListener { nodeSnap ->
                val node = nodeSnap.firstOrNull()
                if (node != null) {


//                    val fatherNodeId = node.getString("id_father")
//                    val motherNodeId = node.getString("id_mother")

// Load thông tin bố
//                    fatherNodeId?.let { fid ->
//                        db.collection("Node").document(fid).get()
//                            .addOnSuccessListener { fatherNode ->
//                                val fatherProfileId = fatherNode.getString("id_profile")
//                                fatherProfileId?.let {
//                                    db.collection("Profile").document(it).get()
//                                        .addOnSuccessListener { doc ->
//                                            val name = doc.getString("name") ?: "Không rõ"
//                                            val dob = doc.getTimestamp("date_of_birth")?.toDate()
//                                            val dobStr = dob?.let {
//                                                java.text.SimpleDateFormat(
//                                                    "dd/MM/yyyy",
//                                                    java.util.Locale.getDefault()
//                                                ).format(it)
//                                            } ?: "Không rõ"
//                                            binding.tvFather.text = "$name\nNgày sinh: $dobStr"
//                                        }
//                                }
//                            }
//                    }

// Load thông tin mẹ
//                    motherNodeId?.let { mid ->
//                        db.collection("Node").document(mid).get()
//                            .addOnSuccessListener { motherNode ->
//                                val motherProfileId = motherNode.getString("id_profile")
//                                motherProfileId?.let {
//                                    db.collection("Profile").document(it).get()
//                                        .addOnSuccessListener { doc ->
//                                            val name = doc.getString("name") ?: "Không rõ"
//                                            val dob = doc.getTimestamp("date_of_birth")?.toDate()
//                                            val dobStr = dob?.let {
//                                                java.text.SimpleDateFormat(
//                                                    "dd/MM/yyyy",
//                                                    java.util.Locale.getDefault()
//                                                ).format(it)
//                                            } ?: "Không rõ"
//                                            binding.tvMother.text = "$name\nNgày sinh: $dobStr"
//                                        }
//                                }
//                            }
//                    }

                    val partnerId = node.getString("id_partner")
                    val children = node.get("id_children") as? List<String> ?: emptyList()

                    // Lấy đối tác
                    partnerId?.let {
                        db.collection("Profile").document(it).get()
                            .addOnSuccessListener { doc ->
                                binding.tvPartner.text =
                                    doc.getString("name") + "\n" + doc.getString("date_of_birth")
                            }
                    }

                    // Lấy danh sách con
                    val childrenLayout = binding.llChildren
                    children.forEach { childId ->
                        db.collection("Profile").document(childId).get()
                            .addOnSuccessListener { doc ->
                                val childView = LayoutInflater.from(context)
                                    .inflate(R.layout.item_child_card, null)
                                val tvName: TextView = childView.findViewById(R.id.tvName)
                                val tvInfo: TextView = childView.findViewById(R.id.tvDetails)
                                tvName.text = doc.getString("name")

                                val dob = doc.getTimestamp("date_of_birth")?.toDate()
                                val dobStr = dob?.let {
                                    java.text.SimpleDateFormat(
                                        "dd/MM/yyyy",
                                        java.util.Locale.getDefault()
                                    ).format(it)
                                } ?: "Không rõ"
                                tvInfo.text = "Ngày sinh: $dobStr"

                                childrenLayout.addView(childView)
                            }
                    }
                }
            }
    }

    companion object {
        fun newInstance(profileId: String): RelationInfoFragment {
            val fragment = RelationInfoFragment()
            fragment.arguments = Bundle().apply {
                putString("PROFILE_ID", profileId)
            }
            return fragment
        }
    }
}



