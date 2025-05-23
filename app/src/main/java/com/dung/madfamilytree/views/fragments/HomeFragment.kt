//package com.dung.madfamilytree.views.fragments
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.dung.madfamilytree.R
//import com.dung.madfamilytree.databinding.FragmentHomeBinding
//import com.dung.madfamilytree.utility.Utility
//import com.dung.madfamilytree.views.activities.CreateNewTreeActivity
//import com.dung.madfamilytree.views.activities.HomeActivity
//import com.dung.madfamilytree.views.activities.HomeNotInTree
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import com.dung.madfamilytree.utility.TreeUtility
//import com.dung.madfamilytree.dtos.TreeDTO
//
//// TODO: Rename parameter arguments, choose names that match
//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"
//
///**
// * A simple [Fragment] subclass.
// * Use the [HomeFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
//class HomeFragment : Fragment() {
//
//    private var _binding: FragmentHomeBinding? = null
//    private val binding get() = _binding!!
//    private val coroutineScope = CoroutineScope(Dispatchers.Main)
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentHomeBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Launch coroutine to call suspend function
//        coroutineScope.launch {
//            setUp()
//            navigateSetup()
//        }
//    }
//
//    private suspend fun setUp() {
//        val tree_id = Utility.getTreeId()
//        Log.d("HomeFragment", "Tree ID from account: $tree_id")
//        binding.textView7.text = Utility.accountName;
//
//        try {
//            val treeDocument = Utility.db?.collection("Tree")
//                ?.document(tree_id)
//                ?.get()
//                ?.await()
//
//            if (treeDocument != null) {
//                if (treeDocument.exists()) {
//                    val treeData = treeDocument.toObject(TreeDTO::class.java)
//                    Log.d("HomeFragment", "Tree data: $treeData")
//
//                    // Update UI with tree data
//                    treeData?.let { data ->
//                        binding.treeSummary.apply {
//                            tvFamilyName.text = data.tree_name ?: ""
//
//                            // Combine address information into one line
//                            val addressBuilder = StringBuilder()
//                            if (!data.exact_address.isNullOrEmpty()) {
//                                addressBuilder.append(data.exact_address)
//                            }
//                            if (!data.commune.isNullOrEmpty()) {
//                                if (addressBuilder.isNotEmpty()) addressBuilder.append(", ")
//                                addressBuilder.append(data.commune)
//                            }
//                            if (!data.district.isNullOrEmpty()) {
//                                if (addressBuilder.isNotEmpty()) addressBuilder.append(", ")
//                                addressBuilder.append(data.district)
//                            }
//                            if (!data.province.isNullOrEmpty()) {
//                                if (addressBuilder.isNotEmpty()) addressBuilder.append(", ")
//                                addressBuilder.append(data.province)
//                            }
//
//                            // Set the combined address
//                            tvExactAddress.text = addressBuilder.toString()
//                            // Hide other address TextViews since we're combining them
//                            tvCommune.visibility = View.GONE
//                            tvDistrict.visibility = View.GONE
//                            tvProvince.visibility = View.GONE
//
//                            tvIntroduction.text = data.introduce ?: ""
//                        }
//
//                        // Get tree structure and calculate counts
//                        val (nodes, profiles) = TreeUtility.fetchFamilyTree(tree_id)
//                        val rootId = Utility.rootId
//                        val treeRoot = nodes?.let { TreeUtility.buildTree(rootId, it, profiles) }
//
//                        // Calculate generations and members
//                        val profilesByDepth = TreeUtility.groupProfilesByDepth(treeRoot)
//                        val generationCount = profilesByDepth.keys.size
//                        val memberCount = profilesByDepth.values.flatten().size
//
//                        binding.treeSummary.tvGenerations.text = "$generationCount đời"
//                        binding.treeSummary.tvMembers.text = "$memberCount thành viên"
//                    }
//                } else {
//                    Log.e("HomeFragment", "Tree document does not exist")
//                    // If tree document doesn't exist, navigate to HomeNotInTree
//                    (activity as? HomeActivity)?.navToHomeNotInTree()
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("HomeFragment", "Error getting tree data", e)
//            // Handle any errors that occur during the query
//            e.printStackTrace()
//            // Set default values in case of error
//            binding.treeSummary.apply {
//                tvFamilyName.text = ""
//                tvExactAddress.text = ""
//                tvCommune.text = ""
//                tvDistrict.text = ""
//                tvProvince.text = ""
//                tvIntroduction.text = ""
//                tvGenerations.text = "0 đời"
//                tvMembers.text = "0 thành viên"
//            }
//        }
//    }
//
//    private suspend fun navigateSetup(){
//        binding.mainMenu.album.setOnClickListener {
//            // Find the bottom navigation view and set the selected item
//            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav_view)?.selectedItemId = R.id.albumFragment
//        }
//
////        binding.mainMenu.phaky.setOnClickListener {
////            findNavController().navigate(R.id.action_homeFragment_to_phakyFragment)
////        }
//
//        binding.mainMenu.phaHe.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_familyTreeFragment)
//        }
//
//        binding.mainMenu.sukien.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_eventsFragment)
//        }
//
//        binding.mainMenu.chiase.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_shareFragment)
//        }
//
//        binding.mainMenu.timkiem.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_accountSearchFragment)
//        }
//
//        binding.mainMenu.thongke.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_statisticsFragment)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

package com.dung.madfamilytree.views.fragments

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
import com.dung.madfamilytree.views.activities.HomeActivity
import com.dung.madfamilytree.dtos.TreeDTO
import com.dung.madfamilytree.utility.TreeUtility
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        // Sử dụng lifecycleScope để an toàn theo vòng đời view
        viewLifecycleOwner.lifecycleScope.launch {
            setUp()
            navigateSetup()
        }
    }

    private suspend fun setUp() {
        if (_binding == null || !isAdded) return

        val treeId = Utility.getTreeId()
        Log.d("HomeFragment", "Tree ID from account: $treeId")
        binding.textView7.text = Utility.accountName

        try {
            val treeDoc = Utility.db?.collection("Tree")?.document(treeId)?.get()?.await()
            if (treeDoc != null && treeDoc.exists()) {
                val treeData = treeDoc.toObject(TreeDTO::class.java)

                treeData?.let { data ->
                    binding.treeSummary.apply {
                        tvFamilyName.text = data.tree_name ?: ""

                        val address = listOfNotNull(
                            data.exact_address,
                            data.commune,
                            data.district,
                            data.province
                        ).joinToString(", ")

                        tvExactAddress.text = address
                        tvCommune.visibility = View.GONE
                        tvDistrict.visibility = View.GONE
                        tvProvince.visibility = View.GONE
                        tvIntroduction.text = data.introduce ?: ""
                    }

                    // Tính số đời và thành viên
                    val (nodes, profiles) = TreeUtility.fetchFamilyTree(treeId)
                    val rootId = Utility.rootId
                    val treeRoot = nodes?.let { TreeUtility.buildTree(rootId, it, profiles) }

                    val groupedProfiles = TreeUtility.groupProfilesByDepth(treeRoot)
                    binding.treeSummary.tvGenerations.text = "${groupedProfiles.keys.size} đời"
                    binding.treeSummary.tvMembers.text = "${groupedProfiles.values.flatten().size} thành viên"
                }
            } else {
                Log.e("HomeFragment", "Tree document does not exist")
                (activity as? HomeActivity)?.navToHomeNotInTree()
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error getting tree data", e)
            e.printStackTrace()

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

    private suspend fun navigateSetup() {
        if (_binding == null || !isAdded) return

        binding.mainMenu.album.setOnClickListener {
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                com.dung.madfamilytree.R.id.bottom_nav_view
            )?.selectedItemId = com.dung.madfamilytree.R.id.albumFragment
        }

        binding.mainMenu.phaHe.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_familyTreeFragment)
        }

        binding.mainMenu.sukien.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_eventsFragment)
        }

        binding.mainMenu.chiase.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_linkResult)
        }

        binding.mainMenu.timkiem.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_accountSearchFragment)
        }

        binding.mainMenu.thongke.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_statisticsFragment)
        }

        binding.mainMenu.timkiem.setOnClickListener {
            findNavController().navigate(
                R.id.accountSearchFragment,  // ID fragment
                null,
                androidx.navigation.NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setEnterAnim(android.R.anim.slide_in_left)
                    .setExitAnim(android.R.anim.slide_out_right)
                    .setPopUpTo(R.id.homeFragment, false)  // giữ lại HomeFragment trong back stack
                    .build()
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
