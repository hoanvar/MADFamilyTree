package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dung.madfamilytree.databinding.DoiItemBinding

class DepthAdapter(
    private val depths: List<String>,
    private var selectedIndex: Int,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<DepthAdapter.DepthViewHolder>() {

    inner class DepthViewHolder(val binding: DoiItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepthViewHolder {
        val binding = DoiItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DepthViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DepthViewHolder, position: Int) {
        holder.binding.tvDoi.text = depths[position]
        holder.binding.root.isSelected = position == selectedIndex
        holder.binding.root.setOnClickListener {
            val oldIndex = selectedIndex
            selectedIndex = position
            notifyItemChanged(oldIndex)
            notifyItemChanged(selectedIndex)
            onClick(position)
        }
    }

    override fun getItemCount() = depths.size
} 