package com.green.wallet.presentation.main.swap.request

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.RequestItem
import com.green.wallet.presentation.main.MainActivity

class RequestItemAdapter(val activity: MainActivity) :
	RecyclerView.Adapter<RequestItemAdapter.RequestItemViewHolder>() {

	private val data = mutableListOf<RequestItem>()

	fun updateRequestList(incoming: List<RequestItem>) {
		data.clear()
		data.addAll(incoming)
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestItemViewHolder {
		val view =
			LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
		return RequestItemViewHolder(view)
	}

	override fun onBindViewHolder(holder: RequestItemViewHolder, position: Int) {
//		holder.onBind(data[position])
	}

	override fun getItemCount() = 10

	class RequestItemViewHolder(v: View) : ViewHolder(v) {

		fun onBind(item: RequestItem) {

		}

	}


}
