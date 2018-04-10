package com.example.wxg.assreader

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView

class AssZiMuView: TextView , IZimuView {

    private lateinit var mPrestenter: AssReaderPrestenter

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)

    fun setAssZiMuPath(path: String) {
        mPrestenter = AssReaderPrestenter(this, context)
        mPrestenter.read(path)
    }

    override fun setZiMu(text: String) {
        Log.i("zimu_info", "text: $text")
        if (text == this.text) {
            return
        }
        if (text.isEmpty()) {
            visibility = View.GONE
        }else {
            this.text = text
            visibility = View.VISIBLE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPrestenter.cancle()
    }
}