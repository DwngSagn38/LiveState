package  com.example.livestate.ui.intro

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.livestate.ui.intro.fragment.Fragment1
import com.example.livestate.ui.intro.fragment.Fragment2
import com.example.livestate.ui.intro.fragment.Fragment3
import com.example.livestate.ui.intro.fragment.Fragment4

class ViewPagerAdapter(fragmentManager: FragmentManager, private val activity: Activity) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


    override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> Fragment1()
                1 -> Fragment2()
                2 -> Fragment3()
                3 -> Fragment4()
                else -> {
                    Fragment1()
                }
            }
        }


    override fun getCount(): Int {
        return 4

    }

}