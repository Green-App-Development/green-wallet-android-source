package com.greenwallet.core.base

import androidx.fragment.app.Fragment


inline fun <reified T> Fragment.getParentOfType(): T {
    return requireActivity() as T
}