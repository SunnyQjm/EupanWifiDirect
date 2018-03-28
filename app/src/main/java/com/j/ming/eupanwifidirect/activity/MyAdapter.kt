package com.j.ming.eupanwifidirect.activity

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.j.ming.eupanwifidirect.R


class MyAdapter(private val mList: MutableList<WifiP2pDevice>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = convertView
        var viewHolder: ViewHolder? = null
        if (view == null) {
            with(LayoutInflater.from(parent?.context)
                    .inflate(R.layout.my_string_item, parent, false)) {
                view = this
                viewHolder = ViewHolder(this)
                tag = viewHolder
            }
        } else {
            viewHolder = view!!.tag as ViewHolder?
        }
        viewHolder?.let { vh->
            mList[position].let {
                vh.tv.text = "${it.deviceName}\n${it.deviceAddress}"
            }
        }
        return view!!
    }

    override fun getItem(position: Int): Any {
        return mList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mList.size
    }

    class ViewHolder(val view: View) {
        val tv: TextView = view.findViewById(R.id.textView)
    }
}