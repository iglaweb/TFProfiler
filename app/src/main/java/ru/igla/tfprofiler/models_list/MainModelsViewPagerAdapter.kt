package ru.igla.tfprofiler.models_list

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.igla.tfprofiler.reports_list.ReportsListFragment

class MainModelsViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        if (position == 0) return NeuralModelsListFragment()
        return ReportsListFragment()
    }
}
