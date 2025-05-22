package com.dung.madfamilytree.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.LinkResult

class LinkResultAdapter(private val results: List<LinkResult>) :
    RecyclerView.Adapter<LinkResultAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMemberName: TextView = itemView.findViewById(R.id.tvMemberName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_link_result, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = results.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val linkResult = results[position]
        holder.tvMemberName.text = linkResult.name
        holder.tvStatus.text = when (linkResult.status) {
            "success" -> "Thành công"
            "declined" -> "Từ chối"
            else -> "Đang xử lý"
        }

        // xử lý màu sắc cho trạng thái liên kết
        when (linkResult.status) {
            "success" -> holder.tvStatus.setTextColor(Color.GREEN)
            "declined" -> holder.tvStatus.setTextColor(Color.RED)
            else -> holder.tvStatus.setTextColor(Color.BLACK)
        }
    }

}
