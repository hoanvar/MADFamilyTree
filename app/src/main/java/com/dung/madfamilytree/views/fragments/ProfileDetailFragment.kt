package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dung.madfamilytree.R
import com.dung.madfamilytree.adapters.ViewPagerAdapter
import com.dung.madfamilytree.databinding.FragmentProfileDetailBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore

class ProfileDetailFragment : Fragment() {

    private lateinit var binding: FragmentProfileDetailBinding
    private lateinit var profileId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        profileId = arguments?.getString("profileId") ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Gán tên người dùng
        FirebaseFirestore.getInstance().collection("Profile").document(profileId)
            .get()
            .addOnSuccessListener { doc ->
                binding.tvProfileName.text = doc.getString("name") ?: "Không rõ"
            }

        // ViewPager & Tab
        val adapter = ViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, profileId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Cá nhân"
                    tab.setIcon(R.drawable.canhan_inf)
                }
                1 -> {
                    tab.text = "Gia đình"
                    tab.setIcon(R.drawable.quanhe_inf)
                }
            }
        }.attach()
    }

    companion object {
        fun newInstance(profileId: String): ProfileDetailFragment {
            val fragment = ProfileDetailFragment()
            fragment.arguments = Bundle().apply {
                putString("profileId", profileId)
            }
            return fragment
        }
    }
}
