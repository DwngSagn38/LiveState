package  com.example.livestate.ui.language_start

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestate.R
import com.example.livestate.databinding.ItemLanguageBinding

import com.example.livestate.model.LanguageModel
import com.example.livestate.view.base.BaseAdapter


class LanguageStartAdapter(
    val context: Context,
    val onClick: (lang: LanguageModel) -> Unit
) : BaseAdapter<ItemLanguageBinding, LanguageModel>() {


    // tạo viewbinding cho mỗi mục trong recyc
    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemLanguageBinding {
        return ItemLanguageBinding.inflate(inflater, parent, false)
    }


    // tạo viewHolder cho mỗi mục
    override fun creatVH(binding: ItemLanguageBinding): RecyclerView.ViewHolder =
        LanguageVH(binding)

    // lớp viewholder để chứa và quản lý
    inner class LanguageVH(binding: ItemLanguageBinding) : BaseVH<LanguageModel>(binding) {

        //hàm gọi mục được chọn
        override fun onItemClickListener(data: LanguageModel) {
            super.onItemClickListener(data)
            onClick.invoke(data) // gọi hàm callback được truyền vào khi một mục được nhấp
        }

        // gán dữ liệu vào viewHolder
        override fun bind(data: LanguageModel) {
            super.bind(data)
            // gán tên ngôn ngữ
            binding.txtName.text = data.name

            // đặt backGroud cho trạng thái active
            if (data.active) {
                binding.txtName.setTextColor(context.getColor(R.color.white))
               binding.layoutItem.setBackgroundResource(R.drawable.bg_item_language_select)
            }
            else {
                binding.txtName.setTextColor(context.getColor(R.color.black))
                binding.layoutItem.setBackgroundResource(R.drawable.bg_item_language_unselect)
            }

            when (data.code) {

                "en" -> binding.icLang.setImageResource(R.drawable.flag_en)
                "de" -> binding.icLang.setImageResource(R.drawable.flag_ger)
                "es" -> binding.icLang.setImageResource(R.drawable.flag_span)
                "fr" -> binding.icLang.setImageResource(R.drawable.flag_fra)
                "hi" -> binding.icLang.setImageResource(R.drawable.flag_hindi)
                "in" -> binding.icLang.setImageResource(R.drawable.flag_indonesia)
                "pt" -> binding.icLang.setImageResource(R.drawable.flag_port)
                "vi" -> binding.icLang.setImageResource(R.drawable.flag_vi)
                "ja" -> binding.icLang.setImageResource(R.drawable.flag_japan)
            }
        }
    }

    //Phương thức cập nhật trạng thái active
    fun setCheck(code: String) {
        for (item in listData) {
            item.active = item.code == code // đặt 'active' cho mục có mã tương ứng
        }
        notifyDataSetChanged()
    }
}