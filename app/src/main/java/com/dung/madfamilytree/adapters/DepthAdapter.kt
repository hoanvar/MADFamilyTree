package com.dung.madfamilytree.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dung.madfamilytree.R
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

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: DepthViewHolder, position: Int) {
        // Change text format to "Đời X"
        holder.binding.tvDoi.text = "Đời ${position + 1}"
        
        // Set text color to white
        holder.binding.tvDoi.setTextColor(holder.itemView.context.getColor(R.color.white))
        
        // Set background color based on selection state
        holder.binding.root.setCardBackgroundColor(
            if (position == selectedIndex) {
                R.color.family_node_background_blue
            } else {
                R.color.black
            }
        )
        
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