package com.android.greenapp.presentation.main.address

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.android.greenapp.domain.entity.Address
import com.example.common.tools.VLog


class AddressAdapter(
    private val openEditListener: EditedOpenListener,
    private val anim: com.android.greenapp.presentation.custom.AnimationManager,
    private val shouldBeClickable: Boolean
) :
    RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    private val addressList = mutableListOf<Address>()
//    private val viewBinderHelper = ViewBinderHelper()

    fun updateAddressList(list: List<Address>) {
        addressList.clear()
        addressList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
//        viewBinderHelper.setOpenOnlyOne(true)
//        viewBinderHelper.bind(holder.swipeRevealLayout, "$position")
//        viewBinderHelper.closeLayout("$position")
        holder.onBind(addressList[position])

    }

    override fun getItemCount() = addressList.size


    inner class AddressViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val txtName = v.findViewById<TextView>(R.id.txt_name)
        private val txtAddress = v.findViewById<TextView>(R.id.txtAddress)
        private val img_delete = v.findViewById<ImageView>(R.id.img_delete)
        private val img_edit = v.findViewById<ImageView>(R.id.img_edit)
        private val img_send = v.findViewById<ImageView>(R.id.img_send)
//        val swipeRevealLayout = v.findViewById<SwipeRevealLayout>(R.id.rootSwipeLayout)
        private val rel_swipe_left = v.findViewById<RelativeLayout>(R.id.rel_swiped_left)
        private val relAddressName = v.findViewById<RelativeLayout>(R.id.rel_address_name)


        @SuppressLint("SetTextI18n")
        fun onBind(addressItem: Address) {

            txtName.text = addressItem.name

            kotlin.runCatching {
                txtAddress.text = addressItem.address.substring(0, 33) + "..."
            }.onFailure {
                txtAddress.text = addressItem.address
            }

            img_delete.setOnClickListener {
                openEditListener.onDelete(addressItem)
            }

            img_edit.setOnClickListener {
                openEditListener.onEdit(addressItem)
            }

            img_send.setOnClickListener {
                openEditListener.onSend(itemAddress = addressItem)
            }

            if (shouldBeClickable) {
                relAddressName.setOnClickListener {
                    VLog.d("Address Item clicked")
                    openEditListener.onAddressClicked(addressItem.address)
                }
            }

        }

    }


    interface EditedOpenListener {
        fun onDelete(itemAddress: Address)
        fun onEdit(itemAddress: Address)
        fun onSend(itemAddress: Address)
        fun onAddressClicked(address: String)
    }


}