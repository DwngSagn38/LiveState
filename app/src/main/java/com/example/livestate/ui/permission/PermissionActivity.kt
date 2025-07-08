package  com.example.livestate.ui.permission

import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityPermissionBinding
import com.example.livestate.sharePreferent.SharePrefUtils
import com.example.livestate.ui.main.MainActivity
import com.example.livestate.utils.helper.Default.ACCESS_COARSE_LOCATION
import com.example.livestate.utils.helper.Default.ACCESS_FINE_LOCATION
import com.example.livestate.utils.helper.Default.CAMERA_PERMISSION
import com.example.livestate.widget.gone
import com.example.livestate.widget.tap
import com.example.livestate.widget.visible


class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {

    private lateinit var arrayPermission: Array<String>

    override fun setViewBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        arrayPermission = arrayOf(CAMERA_PERMISSION,ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION)
        if (checkPermission(arrayPermission)) {
            allowCameraPermission()
        }

    }

    override fun viewListener() {
        binding.apply {
            ivSetCameraPermission.tap {
                showDialogPermission(arrayOf(CAMERA_PERMISSION,ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION))
            }
            tvContinue.tap {
                SharePrefUtils.forceGoToMain(this@PermissionActivity)
                showActivity(MainActivity::class.java)
                finishAffinity()
            }
        }

    }

    override fun dataObservable() {
    }

    override fun onPermissionGranted() {
        if (checkPermission(arrayPermission)) {
            allowCameraPermission()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        if (checkPermission(arrayPermission)) {
            allowCameraPermission()
        }
        super.onResume()
    }

    private fun allowCameraPermission() {
        binding.ivSetCameraPermission.gone()
        binding.ivSelectCameraPermission.visible()
    }

}