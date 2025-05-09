package com.dung.madfamilytree.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

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
        
        // Launch coroutine to call suspend function
        coroutineScope.launch {
            setUp()
            navigateSetup()
        }
    }

    private suspend fun setUp() {
        val tree_id = Utility.getTreeId()
        Log.d("HomeFragment", "Tree ID from account: $tree_id")
        binding.textView7.text = Utility.accountName;

        try {
            val treeDocument = Utility.db?.collection("Tree")
                ?.document(tree_id)
                ?.get()
                ?.await()

            if (treeDocument != null) {
                if (treeDocument.exists()) {
                    val treeData = treeDocument.data
                    Log.d("HomeFragment", "Tree data: $treeData")

                    // Update UI with tree data
                    treeData?.let { data ->
                        binding.treeSummary.tvFamilyName.text = data["tree_name"] as? String ?: ""
                        binding.treeSummary.tvAddress.text = "Địa chỉ: Xã " + data["commune"] + " Huyện " + data["commune"] + " Tỉnh " + data["province"]
                        binding.treeSummary.tvGenerations.text = "${data["generations"] as? Long ?: 0} đời"
                        binding.treeSummary.tvMembers.text = "${data["members"] as? Long ?: 0} thành viên"
                    }
                } else {
                    Log.e("HomeFragment", "Tree document does not exist")
                    // If tree document doesn't exist, navigate to HomeNotInTree
                    (activity as? HomeActivity)?.navToHomeNotInTree()
                }
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error getting tree data", e)
            // Handle any errors that occur during the query
            e.printStackTrace()
            // Set default values in case of error
            binding.treeSummary.tvFamilyName.text = ""
            binding.treeSummary.tvAddress.text = ""
            binding.treeSummary.tvGenerations.text = "0 đời"
            binding.treeSummary.tvMembers.text = "0 thành viên"
        }
    }

    private suspend fun navigateSetup(){
        binding.mainMenu.album.setOnClickListener {
            // Find the bottom navigation view and set the selected item
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav_view)?.selectedItemId = R.id.albumFragment
        }

//        binding.mainMenu.phaky.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_phakyFragment)
//        }

        binding.mainMenu.phaHe.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_familyTreeFragment)
        }

        binding.mainMenu.sukien.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_eventsFragment)
        }

        binding.mainMenu.chiase.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_shareFragment)
        }

        binding.mainMenu.timkiem.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        binding.mainMenu.thongke.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_statisticsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}