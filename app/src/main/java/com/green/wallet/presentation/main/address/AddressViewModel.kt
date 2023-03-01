package com.green.wallet.presentation.main.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.domain.interact.AddressInteract
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class AddressViewModel @Inject constructor(private val addressInteract: AddressInteract) :
	ViewModel() {

	private val _address_items = MutableStateFlow<List<Address>?>(null)
	val address_items: Flow<List<Address>?> = _address_items
	private var job: Job? = null


	init {
		getAddressList()
	}

	private fun getAddressList() {
		job?.cancel()
		job = viewModelScope.launch {
			try {
				val flowOfAddress = addressInteract.getAllSavedAddressList()
				flowOfAddress.collect {
					val sorted = it.sortedWith(object : Comparator<Address> {
						override fun compare(p0: Address, p1: Address): Int {
							if (p0.updated_time < p1.updated_time)
								return -1
							return 1
						}
					})
					_address_items.emit(sorted)
				}
			} catch (ex: Exception) {
				VLog.e("Exception in getting flowAddressList : $ex")
			}
		}
	}

	fun updateAddressItem(address: Address) {
		viewModelScope.launch {
			addressInteract.updateAddressEntity(address)
		}
	}

	fun searchAddressByName(name: String) {
		viewModelScope.launch {
			val res = addressInteract.searchAddressByName(name)
			res.collect {
				_address_items.emit(it)
			}
		}
	}

	fun insertAddressItem(address: Address) {
		viewModelScope.launch {
			addressInteract.insertAddressEntity(address)
		}
	}

	fun deleteAddressItem(address: Address) {
		viewModelScope.launch {
			delay(500)
			addressInteract.deleteAddressEntity(address)
		}
	}

	suspend fun checkIfAddressExistInDb(address: String) =
		addressInteract.checkIfAddressAlreadyExist(address)

}
