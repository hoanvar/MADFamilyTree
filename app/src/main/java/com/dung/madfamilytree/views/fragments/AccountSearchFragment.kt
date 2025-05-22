package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dung.madfamilytree.adapters.AccountSearchAdapter
import com.dung.madfamilytree.databinding.FragmentAccountSearchBinding
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.SearchProfile

class AccountSearchFragment : Fragment() {
    private lateinit var binding: FragmentAccountSearchBinding
    private lateinit var adapter: AccountSearchAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = AccountSearchAdapter { profile ->
            val bundle = Bundle().apply {
                putString("profileId", profile.id)
            }
            findNavController().navigate(R.id.action_accountSearchFragment_to_profileDetail, bundle)
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(context)
        binding.rvSearchResults.adapter = adapter

        binding.btnSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString().trim()
            if (keyword.isNotEmpty()) {
                searchUsers(keyword)
            } else {
                Toast.makeText(context, "Vui lòng nhập tên để tìm kiếm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchUsers(keyword: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyMessage.visibility = View.GONE
        adapter.setData(emptyList())

        db.collection("Profile")
            .whereGreaterThanOrEqualTo("name", keyword)
            .whereLessThanOrEqualTo("name", keyword + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                val users = result.map { doc ->
                    val name = doc.getString("name") ?: ""
                    val dob = doc.getTimestamp("date_of_birth")?.toDate()
                    val location = doc.getString("province1") ?: ""
                    val dateStr = dob?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    } ?: "?"

                    SearchProfile(
                        id = doc.id,
                        name = name,
                        birthDate = dateStr,
                        location = location
                    )
                }

                if (users.isEmpty()) {
                    binding.tvEmptyMessage.visibility = View.VISIBLE
                }

                adapter.setData(users)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show()
            }
    }
}

