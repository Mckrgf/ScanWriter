package com.supcon.scanwriter

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.Button
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * @author : yaobing
 * @date   : 2020/1/1 15:27
 * @desc   : 网络获取的
 */
@Suppress("UNCHECKED_CAST")
class CharacterAdapter(activity: DeviceControlActivity) : BaseQuickAdapter<HashMap<String, String>, BaseViewHolder>(R.layout.item_ble_charactere) {
    private val mActivity = activity
    private var dataAll: ArrayList<HashMap<String, String>> = ArrayList()
    override fun convert(holder: BaseViewHolder, item: HashMap<String, String>) {
        holder.getView<TextView>(R.id.tv_character_name).text = item["NAME"]
        var aaa = item["VALUE"]?.trim()
        aaa = aaa?.replace(" ","")
        if (item["NAME"]?.equals("设备ID")!!) {
            holder.getView<TextView>(R.id.tv_character_value).text = item["VALUE"]?.trim()
            holder.getView<TextView>(R.id.tv_character_value).inputType = InputType.TYPE_CLASS_TEXT
            holder.getView<TextView>(R.id.tv_character_get).isEnabled = true
        }else {
            holder.getView<TextView>(R.id.tv_character_get).isEnabled = false
        }
        holder.getView<TextView>(R.id.tv_character_value).text = aaa
        holder.getView<Button>(R.id.tv_character_get).setOnClickListener {
            mActivity.getValue(holder.adapterPosition)
        }
        holder.getView<TextView>(R.id.tv_character_value).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                data[holder.adapterPosition]["VALUE"] = s.toString()


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun refresh(name: String) {

    }


    fun setdata(data: ArrayList<HashMap<String, String>>) {

    }

}