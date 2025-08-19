package com.example.m_launcher.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.m_launcher.settings.FavoritesFragment
import com.example.m_launcher.settings.GesturesFragment
import com.example.m_launcher.settings.LayoutFragment

class SettingsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val favoritesFragment = FavoritesFragment()
    private val gesturesFragment = GesturesFragment()
    private val layoutFragment = LayoutFragment()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> favoritesFragment
        1 -> gesturesFragment
        else -> layoutFragment
    }

    fun getFavoritesFragment(): FavoritesFragment = favoritesFragment
    fun getGesturesFragment(): GesturesFragment = gesturesFragment
    fun getLayoutFragment(): LayoutFragment = layoutFragment
}


