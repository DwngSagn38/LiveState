package  com.example.livestate.ui.permission

import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityPermissionBinding
import com.example.livestate.sharePreferent.SharePrefUtils
import com.example.livestate.ui.main.MainActivity
import com.example.livestate.utils.helper.Default.CAMERA_PERMISSION
import com.example.livestate.widget.gone
import com.example.livestate.widget.tap
import com.example.livestate.widget.visible


class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {


    override fun setViewBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        if (checkPermission(CAMERA_PERMISSION)) {
            allowCameraPermission()
        }

    }

    override fun viewListener() {
        binding.apply {
            ivSetCameraPermission.tap {
                showDialogPermission(arrayOf(CAMERA_PERMISSION))
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
        if (checkPermission(CAMERA_PERMISSION)) {
            allowCameraPermission()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        if (checkPermission(CAMERA_PERMISSION)) {
            allowCameraPermission()
        }
        super.onResume()
    }

    private fun allowCameraPermission() {
        binding.ivSetCameraPermission.gone()
        binding.ivSelectCameraPermission.visible()
    }

}