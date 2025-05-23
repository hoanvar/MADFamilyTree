package com.dung.madfamilytree.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dung.madfamilytree.R
import com.dung.madfamilytree.adapters.LinkResultAdapter
import com.dung.madfamilytree.dtos.LinkResult
import com.dung.madfamilytree.utility.Utility

class LinkResultFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LinkResultAdapter
    private val resultList = mutableListOf<LinkResult>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_link_result, container, false)
        recyclerView = view.findViewById(R.id.recyclerLinkResults)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LinkResultAdapter(resultList)
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.btnBackHome).setOnClickListener {
            requireActivity().onBackPressed()
        }

        loadLinkResults()
        return view
    }

    private fun loadLinkResults() {
        val currentUserId = Utility.accountId ?: return

        Utility.db?.collection("LinkRequests")
            ?.whereEqualTo("fromId", currentUserId)
            ?.get()
            ?.addOnSuccessListener { documents ->
                resultList.clear()

                if (documents.isEmpty) {
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                val pendingTasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                for (doc in documents) {
                    val toId = doc.getString("toId") ?: continue
                    val status = doc.getString("status") ?: "pending"

                    val task = Utility.db?.collection("Account")?.document(toId)?.get()
                        ?.addOnSuccessListener { accountDoc ->
                            val username = accountDoc.getString("username") ?: "Không rõ"
                            resultList.add(LinkResult(username, status))
                        }

                    if (task != null) pendingTasks.add(task)
                }

                com.google.android.gms.tasks.Tasks.whenAllComplete(pendingTasks)
                    .addOnSuccessListener {
                        adapter.notifyDataSetChanged()
                    }
            }
    }

}
