package com.green.wallet.presentation.main.nft.nftdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.green.wallet.R

class NftPropertiesAdapter(val nftPropertyMap: HashMap<String, String>) :
	Adapter<NftPropertiesAdapter.NFTPropertyVH>() {

	var propertyList = listOf<String>()

	fun updateNFTPropertyList(data: List<String>) {
		val diffResult = DiffUtil.calculateDiff(NFTPropertiesUtilCallBack(propertyList, data))
		propertyList = data
		diffResult.dispatchUpdatesTo(this)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NFTPropertyVH {
		val view =
			LayoutInflater.from(parent.context).inflate(R.layout.item_properties_nft, parent, false)
		return NFTPropertyVH(view)
	}

	override fun onBindViewHolder(holder: NFTPropertyVH, position: Int) {
		holder.onBind(propertyList[position])
	}

	override fun getItemCount(): Int {
		return propertyList.size
	}

	inner class NFTPropertyVH(v: View) : ViewHolder(v) {

		val txtProperty = v.findViewById<TextView>(R.id.edtPropertyKey)
		val txtValue = v.findViewById<TextView>(R.id.edtPropertyValue)

		fun onBind(proper: String) {
			val value = nftPropertyMap[proper] ?: ""
			txtProperty.text = proper
			txtValue.text = value
		}

	}

	inner class NFTPropertiesUtilCallBack(
		val oldPropertyList: List<String>,
		val newPropertyList: List<String>
	) :
		DiffUtil.Callback() {

		override fun getOldListSize(): Int {
			return oldPropertyList.size
		}

		override fun getNewListSize(): Int {
			return newPropertyList.size
		}

		override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
			return oldPropertyList[oldItemPosition] == newPropertyList[newItemPosition]
		}

		override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
			return oldPropertyList[oldItemPosition] == newPropertyList[newItemPosition]
		}

	}

}
