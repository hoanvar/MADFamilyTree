package com.dung.madfamilytree.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dung.madfamilytree.R
import com.dung.madfamilytree.adapters.AlbumItemAdapter
import com.dung.madfamilytree.databinding.FragmentAlbumBinding
import com.dung.madfamilytree.models.Album
import com.dung.madfamilytree.views.activities.AlbumDetailActivity
import com.dung.madfamilytree.views.activities.CreateNewAlbumActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlbumFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlbumFragment : Fragment() {
    private var _binding: FragmentAlbumBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlbumBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        val adapter = AlbumItemAdapter {
            if(it == 0)
            activity?.startActivity(Intent(requireContext(),CreateNewAlbumActivity::class.java))
            else{
                activity?.startActivity(Intent(requireContext(),AlbumDetailActivity::class.java))
            }
        }
        adapter.data = listOf(Album("hello"),Album("Hello"))
        binding.albumRecycleView.adapter = adapter
        return binding.root
    }

}