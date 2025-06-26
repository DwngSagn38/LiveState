package com.example.livestate.dialog

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.Toast
import com.example.livestate.R
import com.example.livestate.base.BaseDialog
import com.example.livestate.databinding.DialogRatingBinding
import com.example.livestate.sharePreferent.SharePrefUtils
import com.example.livestate.utils.helper.Default
import com.example.livestate.widget.tap
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory


class RatingDialog(activity: Activity, private val isFinishActivity: Boolean) :
    BaseDialog<DialogRatingBinding>(activity, true) {

    override fun getContentView(): DialogRatingBinding {
        return DialogRatingBinding.inflate(LayoutInflater.from(activity))
    }

    override fun initView() {
        changeRating()
        binding.rtb.rating = 5f
    }

    override fun bindView() {
        binding.apply {
            btnRate.tap {
                if (rtb.rating == 0f) {
//                    Toast.makeText(activity, R.string.please_give_rating, Toast.LENGTH_SHORT)
//                        .show()
                    return@tap
                }
                dismiss()
                if (rtb.rating <= 4) {
                    sendMail()
                } else {
                    sendRate()
                }
            }
            btnLater.tap {
                dismiss()
            }
        }
    }

    private fun sendRate() {
        val manager: ReviewManager = ReviewManagerFactory.create(activity)
        val request: Task<ReviewInfo> = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo: ReviewInfo = task.getResult()
                val flow: Task<Void> =
                    manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnSuccessListener {
                    SharePrefUtils.forceRated(
                        activity
                    )
                    if (isFinishActivity) activity.finishAffinity()
                }
            } else {
                SharePrefUtils.forceRated(
                    activity
                )
                dismiss()
                if (isFinishActivity) activity.finishAffinity()
            }
        }
    }

    private fun sendMail() {
        val uriText =
            "mailto:${Default.EMAIL}?subject=${Default.SUBJECT}&body=Rate : ${binding.rtb.rating}" + "Content: ".trimIndent()
        val uri = Uri.parse(uriText)
        val sendIntent = Intent(Intent.ACTION_SENDTO)
        sendIntent.data = uri
        try {
            activity.startActivity(Intent.createChooser(sendIntent, "Send Email"))
            SharePrefUtils.forceRated(
                activity
            )
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                activity,
                R.string.There_is_no_email,
                Toast.LENGTH_SHORT
            ).show()
        }
        if (isFinishActivity) activity.finishAffinity()
    }

    private fun changeRating() {
        binding.rtb.onRatingBarChangeListener =
            OnRatingBarChangeListener { _, rating, _ ->
                when (rating) {
                    1f -> {
                        binding.tvTitle.text = activity.getText(R.string.oh_we_re_sorry)
                        binding.tvContent.text = activity.getText(R.string.your_feedback_is_welcome)
                        binding.ivContback.setImageResource(R.drawable.start_1)
                        binding.btnLater.visibility = View.VISIBLE
                    }

                    2f -> {
                        binding.tvTitle.text = activity.getText(R.string.oh_we_re_sorry)
                        binding.tvContent.text = activity.getText(R.string.your_feedback_is_welcome)
                        binding.ivContback.setImageResource(R.drawable.start_2)
                        binding.btnLater.visibility = View.VISIBLE
                    }

                    3f -> {
                        binding.tvTitle.text = activity.getText(R.string.oh_we_re_sorry)
                        binding.tvContent.text = activity.getText(R.string.your_feedback_is_welcome)
                        binding.ivContback.setImageResource(R.drawable.start_3)
                        binding.btnLater.visibility = View.VISIBLE
                    }

                    4f -> {
                        binding.tvTitle.text = activity.getText(R.string.much_appreciated)
                        binding.tvContent.text = activity.getText(R.string.your_support_is_our_biggest_motivation)
                        binding.ivContback.setImageResource(R.drawable.start_4)
                        binding.btnLater.visibility = View.GONE
                    }

                    5f -> {
                        binding.tvTitle.text = activity.getText(R.string.much_appreciated)
                        binding.tvContent.text = activity.getText(R.string.your_support_is_our_biggest_motivation)
                        binding.ivContback.setImageResource(R.drawable.start_5)
                        binding.btnLater.visibility = View.GONE
                    }

                    else -> {
                        binding.btnLater.visibility = View.VISIBLE
                    }
                }
            }
    }

}