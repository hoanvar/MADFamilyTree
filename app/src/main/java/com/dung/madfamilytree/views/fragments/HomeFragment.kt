package com.dung.madfamilytree.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.FragmentHomeBinding
import com.dung.madfamilytree.utility.Utility
import com.dung.madfamilytree.views.activities.CreateNewTreeActivity
import com.dung.madfamilytree.views.activities.HomeActivity
import com.dung.madfamilytree.views.activities.HomeNotInTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.dung.madfamilytree.utility.TreeUtility
import com.dung.madfamilytree.dtos.TreeDTO

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
// ... existing code ...

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    // Changed binding property to use safe null check with descriptive error
    private val binding get() = _binding ?: throw IllegalStateException("Cannot access binding before onCreateView or after onDestroyView")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Changed to use viewLifecycleOwner.lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            setUp()
            navigateSetup()
        }
    }

    private suspend fun setUp() {
        if (!isAdded || _binding == null) return
        
        val tree_id = Utility.getTreeId()
        Log.d("HomeFragment", "Tree ID from account: $tree_id")
        binding.textView7.text = Utility.accountName

        try {
            // ... rest of the setup code ...
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error getting tree data", e)
            if (_binding != null) {
                binding.treeSummary.apply {
                    tvFamilyName.text = ""
                    tvExactAddress.text = ""
                    tvCommune.text = ""
                    tvDistrict.text = ""
                    tvProvince.text = ""
                    tvIntroduction.text = ""
                    tvGenerations.text = "0 đời"
                    tvMembers.text = "0 thành viên"
                }
            }
        }
    }

    private suspend fun navigateSetup() {
        if (!isAdded || _binding == null) return

        binding.mainMenu.album.setOnClickListener {
            if (isAdded) {
                activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav_view)?.selectedItemId = R.id.albumFragment
            }
        }

        binding.mainMenu.phaHe.setOnClickListener {
            if (isAdded) {
                findNavController().navigate(R.id.action_homeFragment_to_familyTreeFragment)
            }
        }

        binding.mainMenu.sukien.setOnClickListener {
            if (isAdded) {
                findNavController().navigate(R.id.action_homeFragment_to_eventsFragment)
            }
        }

        binding.mainMenu.chiase.setOnClickListener {
            if (isAdded) {
                findNavController().navigate(R.id.action_homeFragment_to_shareFragment)
            }
        }

        binding.mainMenu.timkiem.setOnClickListener {
            if (isAdded) {
                findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
            }
        }

        binding.mainMenu.thongke.setOnClickListener {
            if (isAdded) {
                findNavController().navigate(R.id.action_homeFragment_to_statisticsFragment)
            }
        }

        binding.btnNotification.setOnClickListener {
            if (isAdded) {
                findNavController().navigate(R.id.notificationFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}