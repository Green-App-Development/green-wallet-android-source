package com.green.wallet.presentation.main.nft.usernfts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.NFTInfo


class NFTTokenAdapter(private val nftTokenClicked: NFTTokenClicked) :
	RecyclerView.Adapter<NFTTokenAdapter.NFTTokenViewHolder>() {

	private var nftList = listOf<NFTInfo>()

	fun updateNFTTokenList(data: List<NFTInfo>) {
		val diffResult = DiffUtil.calculateDiff(NFTTokenUtilCallBack(nftList, data))
		nftList = data
		diffResult.dispatchUpdatesTo(this)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NFTTokenViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nft, parent, false)
		return NFTTokenViewHolder(view)
	}

	override fun onBindViewHolder(holder: NFTTokenViewHolder, position: Int) {
		holder.onBind(nftList[position], position = position)
	}

	override fun getItemCount(): Int {
		return nftList.size
	}


	inner class NFTTokenViewHolder(v: View) : RecyclerView.ViewHolder(v) {
		private val nftName = v.findViewById<TextView>(R.id.txt_name_nft)
		private val nftCategory = v.findViewById<TextView>(R.id.txt_category_nft)
		private val img_checked_nft = v.findViewById<ImageView>(R.id.img_nft_checked)
		private val img_nft = v.findViewById<ImageView>(R.id.img_nft)
		private val emptyView = v.findViewById<View>(R.id.emptyView)
		private val rootNFTToken = v.findViewById<RelativeLayout>(R.id.root_nft_token)

		fun onBind(nftToken: NFTInfo, position: Int) {
//			nftName.setText(nftToken.name)
//			nftCategory.setText(nftToken.categoryType)
//			emptyView.visibility = if (position <= 1) View.VISIBLE else View.GONE
//
//			rootNFTToken.setOnClickListener {
//				nftTokenClicked.onNFTToken()
//			}

		}


	}


	inner class NFTTokenUtilCallBack(
		val oldNFTList: List<NFTInfo>,
		val newNFTToken: List<NFTInfo>
	) :
		DiffUtil.Callback() {

		override fun getOldListSize(): Int {
			return oldNFTList.size
		}

		override fun getNewListSize(): Int {
			return newNFTToken.size
		}

		override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
			return oldNFTList[oldItemPosition] == newNFTToken[newItemPosition]
		}

		override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
			return oldNFTList[oldItemPosition] == newNFTToken[newItemPosition]
		}

	}

	interface NFTTokenClicked {
		fun onNFTToken()
	}

}
