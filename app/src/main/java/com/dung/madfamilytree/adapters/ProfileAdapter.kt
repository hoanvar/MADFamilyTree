package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dung.madfamilytree.R
import com.dung.madfamilytree.databinding.ProfileSummaryItemBinding
import com.dung.madfamilytree.dtos.TreeNode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileAdapter(
    private val profiles: List<TreeNode>,
    private val onAddNewClick: (TreeNode) -> Unit,
    private val onProfileClick: (TreeNode) -> Unit
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
        
        // Load avatar image
        profile?.avatar_url?.let { avatarUrl ->
            if (avatarUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.profile_icon)
                    .into(holder.binding.imageView6)
            } else {
                holder.binding.imageView6.setImageResource(R.drawable.profile_icon)
            }
        } ?: holder.binding.imageView6.setImageResource(R.drawable.profile_icon)

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
        holder.binding.textView16.setImageResource(
            if (isAlive) R.drawable.baseline_person_24 else R.drawable.baseline_person_off_24
        )
        holder.binding.textView16.setColorFilter(
            if (isAlive) android.graphics.Color.GREEN else android.graphics.Color.GRAY
        )

        // Trạng thái hôn nhân
        val isMarried = profile?.marital_status == "Đã kết hôn"
        holder.binding.textView17.setColorFilter(
            if (isMarried) android.graphics.Color.RED else android.graphics.Color.WHITE
        )

        // Set click listener for add new button
        holder.binding.btnAddNew.setOnClickListener {
            onAddNewClick(node)
        }
        holder.binding.root.setOnClickListener {
            onProfileClick(node)
        }
    }

    override fun getItemCount() = profiles.size
} 