package com.dung.madfamilytree.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dung.madfamilytree.views.fragments.PersonalInfoFragment
import com.dung.madfamilytree.views.fragments.RelationInfoFragment

class ViewPagerAdapter(
    fa: FragmentManager,
    lifecycle: Lifecycle,
    private val profileId: String
) : FragmentStateAdapter(fa, lifecycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0)
            PersonalInfoFragment.newInstance(profileId)
        else
            RelationInfoFragment.newInstance(profileId)
    }
}