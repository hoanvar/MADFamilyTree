package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.FragmentRelationInfoBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RelationInfoFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var binding: FragmentRelationInfoBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRelationInfoBinding.inflate(inflater, container, false)
        profileId = arguments?.getString("PROFILE_ID") ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadNodeInfo()
        loadParentInfo()
    }

    private fun loadNodeInfo() {
        db.collection("Node")
            .whereEqualTo("id_profile", profileId)
            .get()
            .addOnSuccessListener { nodeSnap ->
                val node = nodeSnap.firstOrNull() ?: return@addOnSuccessListener

                // Lấy thông tin đối tác
                val partnerId = node.getString("id_partner")
                partnerId?.let { loadProfileCard(it, binding.llPartner) }

                // Lấy thông tin con cái
                val children = node.get("id_children") as? List<String> ?: emptyList()
                children.forEach { childId ->
                    loadProfileCard(childId, binding.llChildren)
                }
            }
    }

    private fun loadParentInfo() {
        db.collection("Node")
            .whereArrayContains("id_children", profileId)
            .get()
            .addOnSuccessListener { nodes ->
                for (doc in nodes) {
                    val parentId = doc.getString("id_profile") ?: continue
                    db.collection("Profile").document(parentId)
                        .get()
                        .addOnSuccessListener { profile ->
                            val gender = profile.getString("gender") ?: ""
                            val layout = if (gender == "Nam") binding.llFather else binding.llMother
                            addProfileToLayout(profile, layout)
                        }
                }
            }
    }

    private fun loadProfileCard(profileId: String, layout: LinearLayout) {
        db.collection("Profile").document(profileId)
            .get()
            .addOnSuccessListener { doc ->
                addProfileToLayout(doc, layout)
            }
    }

    private fun addProfileToLayout(doc: com.google.firebase.firestore.DocumentSnapshot, layout: LinearLayout) {
        val view = LayoutInflater.from(context).inflate(R.layout.item_child_card, null)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvInfo = view.findViewById<TextView>(R.id.tvDetails)

        val name = doc.getString("name") ?: "Không rõ"
        val dob = doc.getTimestamp("date_of_birth")?.toDate()
        val dobStr = dob?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
        } ?: "Không rõ"

        tvName.text = name
        tvInfo.text = "Ngày sinh: $dobStr"

        layout.addView(view)
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
