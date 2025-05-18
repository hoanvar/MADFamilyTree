package com.dung.madfamilytree.views.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dung.madfamilytree.databinding.FragmentStatisticsBinding
import androidx.core.content.ContextCompat
import com.dung.madfamilytree.R
import androidx.lifecycle.lifecycleScope
import com.dung.madfamilytree.utility.TreeUtility
import com.dung.madfamilytree.utility.Utility
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.content.res.ResourcesCompat

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hàm tiện ích để set hoặc bỏ drawableEnd
        fun setArrow(view: android.widget.TextView, show: Boolean) {
            val drawable = if (show) ContextCompat.getDrawable(requireContext(), R.drawable.baseline_arrow_drop_down) else null
            view.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }

        // --- LẤY DỮ LIỆU THỐNG KÊ TỪ FIRESTORE ---
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Lấy treeId và treeName
                val treeId = Utility.getTreeId()
                val treeName = Utility.getTreeName() ?: ""
                binding.tvFamilyName.text = treeName

                // Lấy địa chỉ
                val treeDoc = Utility.db?.collection("Tree")?.document(treeId)?.get()?.await()
                val address = if (treeDoc != null && treeDoc.exists()) {
                    val data = treeDoc.data
                    "Địa chỉ: " + (data?.get("exact_address") as? String ?: "") + ", xã " + (data?.get("commune") as? String ?: "") + ", huyện " + (data?.get("district") as? String ?: "") + ", tỉnh " + (data?.get("province") as? String ?: "")
                } else ""
                binding.tvAddress.text = address

                // Lấy nodes và profiles
                val (nodes, profiles) = TreeUtility.fetchFamilyTree(treeId)
                val rootId = Utility.rootId
                val treeRoot = nodes?.let { TreeUtility.buildTree(rootId, it, profiles) }
                val profilesByDepth = TreeUtility.groupProfilesByDepth(treeRoot)
                val generationCount = profilesByDepth.keys.size
                val memberCount = profiles?.size ?: 0
                binding.tvGenerations.text = "$generationCount đời"
                binding.tvMembers.text = "$memberCount thành viên"

                // --- Tổng quan ---
                val aliveCount = profiles.values.count { it.died == 0 || it.died == null }
                val deadCount = profiles.values.count { it.died == 1 }
                val maleCount = profiles.values.count { it.gender?.lowercase(Locale.ROOT) == "nam" }
                val femaleCount = profiles.values.count { it.gender?.lowercase(Locale.ROOT) == "nữ" }
                val ages = profiles.values.mapNotNull { it.date_of_birth?.toDate()?.let { dob -> ((System.currentTimeMillis() - dob.time) / (1000L*60*60*24*365)).toInt() } }
                val maxAge = ages.maxOrNull() ?: 0
                val minAge = ages.minOrNull() ?: 0
                val avgAge = if (ages.isNotEmpty()) ages.sum() / ages.size else 0
                binding.layoutOverview.removeAllViews()
                val roboto = ResourcesCompat.getFont(requireContext(), R.font.roboto)
                fun addOverviewLine(text: String) {
                    val tv = TextView(context)
                    tv.text = text
                    tv.textSize = 15f
                    tv.typeface = roboto
                    tv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    binding.layoutOverview.addView(tv)
                }
                addOverviewLine("Số thành viên còn sống: $aliveCount người")
                addOverviewLine("Số thành viên đã mất: $deadCount người")
                addOverviewLine("Số lượng nam: $maleCount người")
                addOverviewLine("Số lượng nữ: $femaleCount người")
                addOverviewLine("Người lớn tuổi nhất: $maxAge tuổi")
                addOverviewLine("Người nhỏ tuổi nhất: $minAge tuổi")
                addOverviewLine("Tuổi thọ trung bình: $avgAge tuổi")

                // --- Địa lý ---
                binding.layoutLocation.removeAllViews()
                val abroadCount = profiles.values.count { it.province1?.lowercase(Locale.ROOT) == "nước ngoài" }
                val vietNamProfiles = profiles.values.filter { it.province1?.lowercase(Locale.ROOT) != "nước ngoài" }
                val byProvince = vietNamProfiles.groupBy { it.province1 ?: "Khác" }
                fun addLocationLine(text: String) {
                    val tv = TextView(context)
                    tv.text = text
                    tv.textSize = 15f
                    tv.typeface = roboto
                    tv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    binding.layoutLocation.addView(tv)
                }
                addLocationLine("Thành viên sinh sống tại nước ngoài: $abroadCount người")
                addLocationLine("Thành viên sinh sống tại Việt Nam:")
                byProvince.forEach { (province, list) ->
                    addLocationLine("- $province: ${list.size} người")
                }

                // --- Nghề nghiệp ---
                binding.layoutJob.removeAllViews()
                val jobs = profiles.values.groupBy { it.job ?: "Khác" }
                fun addJobLine(text: String) {
                    val tv = TextView(context)
                    tv.text = text
                    tv.textSize = 15f
                    tv.typeface = roboto
                    tv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    binding.layoutJob.addView(tv)
                }
                jobs.forEach { (job, list) ->
                    addJobLine("$job: ${list.size} người")
                }

                // --- Độ tuổi ---
                binding.layoutAge.removeAllViews()
                val ageGroups = listOf(
                    "0 - 18 tuổi" to ages.count { it in 0..18 },
                    "19 - 30 tuổi" to ages.count { it in 19..30 },
                    "31 - 54 tuổi" to ages.count { it in 31..54 },
                    "Trên 55 tuổi" to ages.count { it >= 55 }
                )
                fun addAgeLine(text: String) {
                    val tv = TextView(context)
                    tv.text = text
                    tv.textSize = 15f
                    tv.typeface = roboto
                    tv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    binding.layoutAge.addView(tv)
                }
                ageGroups.forEach { (label, count) ->
                    addAgeLine("$label: $count người")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // --- XỬ LÝ ẨN/HIỆN CÁC MỤC THỐNG KÊ ---
        setArrow(binding.tvOverviewTitle, true)
        binding.tvOverviewTitle.setOnClickListener {
            val isVisible = binding.layoutOverview.visibility == View.VISIBLE
            binding.layoutOverview.visibility = if (isVisible) View.GONE else View.VISIBLE
            setArrow(binding.tvOverviewTitle, !isVisible)
        }
        setArrow(binding.tvLocationTitle, false)
        binding.tvLocationTitle.setOnClickListener {
            val isVisible = binding.layoutLocation.visibility == View.VISIBLE
            binding.layoutLocation.visibility = if (isVisible) View.GONE else View.VISIBLE
            setArrow(binding.tvLocationTitle, !isVisible)
        }
        setArrow(binding.tvJobTitle, false)
        binding.tvJobTitle.setOnClickListener {
            val isVisible = binding.layoutJob.visibility == View.VISIBLE
            binding.layoutJob.visibility = if (isVisible) View.GONE else View.VISIBLE
            setArrow(binding.tvJobTitle, !isVisible)
        }
        setArrow(binding.tvAgeTitle, false)
        binding.tvAgeTitle.setOnClickListener {
            val isVisible = binding.layoutAge.visibility == View.VISIBLE
            binding.layoutAge.visibility = if (isVisible) View.GONE else View.VISIBLE
            setArrow(binding.tvAgeTitle, !isVisible)
        }

        // Xử lý nút back
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 