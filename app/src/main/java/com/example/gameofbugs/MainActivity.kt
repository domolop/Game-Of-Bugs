package com.example.gameofbugs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(), PageNavigationHost {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = MainPagerAdapter(this)
        viewPager.offscreenPageLimit = Pages.TITLES.size - 1

        val tabLayout = findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = Pages.TITLES[position]
        }.attach()

        viewPager.setCurrentItem(Pages.GAME, false)
    }

    override fun navigateToPage(page: Int) {
        if (page in Pages.TITLES.indices) {
            viewPager.setCurrentItem(page, true)
        }
    }
}
