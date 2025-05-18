package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dung.madfamilytree.databinding.ProfileSummaryItemBinding
import com.dung.madfamilytree.dtos.TreeNode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileAdapter(
    private val profiles: List<TreeNode>,
    private val onAddNewClick: (TreeNode) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(val binding: ProfileSummaryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ProfileSummaryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val node = profiles[position]
        val profile = node.profile
        holder.binding.textView8.text = profile?.name ?: ""
        // Giới tính
        holder.binding.textView13.text = "GT: ${profile?.gender ?: "?"}"
        // Tuổi
        val age = profile?.date_of_birth?.let {
            val cal = Calendar.getInstance()
            val now = Calendar.getInstance()
            cal.time = it.toDate()
            val years = now.get(Calendar.YEAR) - cal.get(Calendar.YEAR)
            if (now.get(Calendar.DAY_OF_YEAR) < cal.get(Calendar.DAY_OF_YEAR)) years - 1 else years
        } ?: "?"
        holder.binding.textView14.text = "Tuổi: $age"
        // Số con - Nếu là partner thì tìm node gốc để lấy số con
        val childrenCount = if (node.partner != null) {
            // Nếu là partner, tìm node gốc để lấy số con
            node.partner.children.size
        } else {
            // Nếu là node gốc, lấy số con trực tiếp
            node.children.size
        }
        holder.binding.textView15.text = "Con: $childrenCount"
        // Đời (nếu cần truyền vào adapter thì thêm trường, ở đây để trống)
        // Trạng thái sống
        val isAlive = profile?.died == null || profile.died == 0
        holder.binding.textView16.text = if (isAlive) "●" else "○"
        // Có thể gán màu cho textView16 nếu muốn (xanh lá cho sống, xám cho mất)
        // ... binding thêm nếu cần

        // Set click listener for add new button
        holder.binding.btnAddNew.setOnClickListener {
            onAddNewClick(node)
        }
    }

    override fun getItemCount() = profiles.size
} 