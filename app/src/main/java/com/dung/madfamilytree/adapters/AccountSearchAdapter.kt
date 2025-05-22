package com.dung.madfamilytree.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.SearchProfile

//class AccountSearchAdapter : RecyclerView.Adapter<AccountSearchAdapter.ViewHolder>() {
//
//    private var data: List<SearchProfile> = emptyList()
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun setData(newData: List<SearchProfile>) {
//        data = newData
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_search_result, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = data.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val person = data[position]
//        holder.tvName.text = person.name
//        holder.tvDetails.text = "Ngày sinh: ${person.birthDate}, ${person.location}"
//    }
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvName: TextView = itemView.findViewById(R.id.tvName)
//        val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
//    }
//}
class AccountSearchAdapter(
    private val onItemClick: (SearchProfile) -> Unit
) : RecyclerView.Adapter<AccountSearchAdapter.ViewHolder>() {

    private var data = listOf<SearchProfile>()

    fun setData(newData: List<SearchProfile>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)

        init {
            view.setOnClickListener {
                onItemClick(data[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = data[position]
        holder.tvName.text = person.name
        holder.tvDetails.text = "Ngày sinh: ${person.birthDate}, ${person.location}"
    }

    override fun getItemCount() = data.size
}
