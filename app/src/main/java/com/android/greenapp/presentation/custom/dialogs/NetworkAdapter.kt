package com.android.greenapp.presentation.custom.dialogs

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.android.greenapp.presentation.custom.getShortNetworkType
import com.android.greenapp.presentation.custom.trimNetwork
import com.android.greenapp.presentation.tools.getDrawableResource

/**
 * Created by bekjan on 05.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class NetworkAdapter(private val activity: Activity, val clicker: ChooseNetworkListener) :
    RecyclerView.Adapter<NetworkAdapter.ItemNetworkViewHolder>() {

    private val networkItems = mutableListOf<String>()

    fun updateNetworkList(list: List<String>) {
        networkItems.clear()
        networkItems.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemNetworkViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.item_network, parent, false)
        return ItemNetworkViewHolder(view, activity, clicker)
    }

    override fun onBindViewHolder(holder: ItemNetworkViewHolder, position: Int) {
        holder.onBind(networkItems[position], networkItems.size)
    }

    override fun getItemCount(): Int {
        return networkItems.size
    }


    class ItemNetworkViewHolder(
        v: View,
        private val activity: Activity,
        private val clicker: ChooseNetworkListener
    ) :
        RecyclerView.ViewHolder(v) {

        private val imgNetwork = v.findViewById<ImageView>(R.id.imgChia)
        private val txtNetworkName = v.findViewById<TextView>(R.id.txtNameNetwork)
        private val txtNetworkNameShort = v.findViewById<TextView>(R.id.txtNameNetworkShort)
        private val relItemNetwork = v.findViewById<RelativeLayout>(R.id.rel_item_network)
        private val item_divider = v.findViewById<View>(R.id.item_divider)

        fun onBind(item: String, maxCount: Int) {

            if (item.lowercase().contains("chia")) {
                imgNetwork.setImageDrawable(activity.getDrawableResource(R.drawable.ic_chia_white))
                txtNetworkName.setText(trimNetwork(item))
                txtNetworkNameShort.setText(getShortNetworkType(item))
            } else {
                imgNetwork.setImageDrawable(activity.getDrawableResource(R.drawable.ic_chives_white))
                txtNetworkName.setText(trimNetwork(item))
                txtNetworkNameShort.setText(getShortNetworkType(item))
            }

            relItemNetwork.setOnClickListener {
                clicker.onNetworkClicked(adapterPosition)
            }

            if (adapterPosition + 1 == maxCount) {
                item_divider.visibility = View.GONE
            }

        }

    }

    interface ChooseNetworkListener {
        fun onNetworkClicked(position: Int)
    }


}