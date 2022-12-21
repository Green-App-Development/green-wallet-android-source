package com.green.wallet.presentation.custom

import android.text.Editable

import android.widget.EditText

import android.text.TextWatcher




/**
 * Created by bekjan on 24.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class EditTextLinesLimiter(editText: EditText, maxLines: Int) : TextWatcher {
    private val editText: EditText
    private val maxLines: Int
    private var lastValue = ""
    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        lastValue = charSequence.toString()
    }

    override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
    override fun afterTextChanged(editable: Editable?) {
        if (editText.lineCount > maxLines) {
            var selectionStart = editText.selectionStart - 1
            editText.setText(lastValue)
            if (selectionStart >= editText.length()) {
                selectionStart = editText.length()
            }
            editText.setSelection(selectionStart)
        }
    }

    init {
        this.editText = editText
        this.maxLines = maxLines
    }
}
