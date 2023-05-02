package com.green.wallet.domain.domainmodel

import android.os.Parcel
import android.os.Parcelable


data class NFTInfo(
	val nft_coin_hash: String,
	val nft_id: String,
	val launcher_id: String,
	val owner_did: String,
	val minter_did: String,
	val royalty_percentage: Int,
	val mint_height: Int,
	val data_url: String,
	val data_hash: String,
	val meta_hash: String,
	val meta_url: String,
	val description: String,
	val collection: String,
	val properties: HashMap<String, String>,
	val name: String
) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readInt(),
		parcel.readInt(),
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readHashMap(String::class.java.classLoader) as HashMap<String, String>,
		parcel.readString()!!
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(nft_coin_hash)
		parcel.writeString(nft_id)
		parcel.writeString(launcher_id)
		parcel.writeString(owner_did)
		parcel.writeString(minter_did)
		parcel.writeInt(royalty_percentage)
		parcel.writeInt(mint_height)
		parcel.writeString(data_url)
		parcel.writeString(data_hash)
		parcel.writeString(meta_hash)
		parcel.writeString(meta_url)
		parcel.writeString(description)
		parcel.writeString(collection)
		parcel.writeMap(properties)
		parcel.writeString(name)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<NFTInfo> {
		override fun createFromParcel(parcel: Parcel): NFTInfo {
			return NFTInfo(parcel)
		}

		override fun newArray(size: Int): Array<NFTInfo?> {
			return arrayOfNulls(size)
		}
	}

}
