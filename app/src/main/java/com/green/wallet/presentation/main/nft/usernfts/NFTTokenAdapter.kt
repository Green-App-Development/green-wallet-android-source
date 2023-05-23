package com.green.wallet.presentation.main.nft.usernfts

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.NFTInfo


class NFTTokenAdapter(
	private val nftTokenClicked: NFTTokenClicked,
	private val activity: Activity
) :
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
		private val imgCheckedNft = v.findViewById<ImageView>(R.id.img_nft_checked)
		private val imgNft = v.findViewById<ImageView>(R.id.img_nft)
		private val progressBar = v.findViewById<CircularProgressIndicator>(R.id.progress_bar)
		private val emptyView = v.findViewById<View>(R.id.emptyView)
		private val rootNFTToken = v.findViewById<RelativeLayout>(R.id.root_nft_token)

		fun onBind(nftInfo: NFTInfo, position: Int) {
//			emptyView.visibility = if (position <= 1) View.VISIBLE else View.GONE
			nftName.text = nftInfo.name
			nftCategory.text = nftInfo.collection
			imgCheckedNft.visibility = if (nftInfo.isVerified) View.VISIBLE else View.GONE
			Glide.with(activity).load(nftInfo.data_url)
				.listener(object : RequestListener<Drawable> {
					override fun onLoadFailed(
						e: GlideException?,
						model: Any?,
						target: Target<Drawable>?,
						isFirstResource: Boolean
					): Boolean {
						return false
					}

					override fun onResourceReady(
						resource: Drawable?,
						model: Any?,
						target: Target<Drawable>?,
						dataSource: DataSource?,
						isFirstResource: Boolean
					): Boolean {
						progressBar.visibility = View.GONE
						return false
					}

				})
				.into(imgNft)
			rootNFTToken.setOnClickListener {
				nftTokenClicked.onNFTToken(nftInfo)
			}

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
		fun onNFTToken(nft: NFTInfo)
	}

}
