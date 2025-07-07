package  com.example.livestate.ui.intro

import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityIntroBinding
import com.example.livestate.sharePreferent.SharePrefUtils
import com.example.livestate.ui.main.MainActivity
import com.example.livestate.ui.permission.PermissionActivity

import com.example.livestate.widget.tapin

class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    var isFirst = true
    private lateinit var dots: Array<ImageView?>

//    private val myPageChangeCallback: ViewPager2.OnPageChangeCallback =
//        object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                if (!isFirst) {
//                    addBottomDots(position)
//                    updateBackground(position)
//                }
//                isFirst = false
//            }
//        }
    private val countPageIntro = 4

    //thiết lâp view binding cho avtivity
    override fun setViewBinding(): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        val pagerAdapter = ViewPagerAdapter(supportFragmentManager, this)
        binding.viewPager2.adapter = pagerAdapter
        binding.viewPager2.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                addBottomDots(position)

                if (position == countPageIntro - 1) {
                    binding.btnNextTutorial.setText(R.string.start)
                } else {
                    binding.btnNextTutorial.setText(R.string.next)
                }

//                when (position) {
//                    0 -> LogEven.logEvent(this@IntroActivity, "onboarding1_view", Bundle())
//                    1 -> LogEven.logEvent(this@IntroActivity, "onboarding2_view", Bundle())
//                    2 -> LogEven.logEvent(this@IntroActivity, "onboarding3_view", Bundle())
//                    else -> LogEven.logEvent(this@IntroActivity, "onboarding4_view", Bundle())
//                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        addBottomDots(0)
    }

//    private fun updateBackground(position: Int) {
//        val backgroundResource = when (position) {
//            0 -> R.drawable.img_intro1
//            1 -> R.drawable.img_intro_2
//            2 -> R.drawable.img_intro_2
//            else -> R.drawable.img_intro3
//        }
//        val next = when (position) {
//            0 -> R.string.next
//            1 -> R.string.next
//            2 -> R.string.next
//            else -> R.string.start
//        }
//        binding.bgIntro.setBackgroundResource(backgroundResource)
//        binding.btnNextTutorial.setText(next)
//    }

    override fun viewListener() {
        binding.btnNextTutorial.tapin {
            if (binding.viewPager2.currentItem == countPageIntro - 1) {
                it?.isEnabled = false
                startNextScreen()
            } else
                binding.viewPager2.currentItem += 1
        }

    }

    private fun startNextScreen() {
        if (SharePrefUtils.isGoToMain(this)) {
                showActivity(MainActivity::class.java)
        } else {
            showActivity(PermissionActivity::class.java)
        }
            finishAffinity()
    }


    override fun dataObservable() {
        addBottomDots(0)
    }


    private fun addBottomDots(currentPage: Int) {
        binding.linearDots.removeAllViews()
        dots = arrayOfNulls(countPageIntro)
        for (i in 0 until countPageIntro) {
            dots[i] = ImageView(this)
            if (i == currentPage)
                dots[i]!!.setImageResource(R.drawable.ic_intro_selected)
            else
                dots[i]!!.setImageResource(R.drawable.ic_intro_not_select)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            binding.linearDots.addView(dots[i], params)
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}