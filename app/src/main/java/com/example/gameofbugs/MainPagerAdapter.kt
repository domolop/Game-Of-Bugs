package com.example.gameofbugs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = Pages.TITLES.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            Pages.GAME -> GameFragment()
            Pages.REGISTRATION -> RegistrationFragment()
            Pages.RULES -> RulesFragment()
            Pages.AUTHORS -> AuthorsFragment()
            Pages.SETTINGS -> SettingsFragment()
            else -> GameFragment()
        }
    }
}
