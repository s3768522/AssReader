package com.example.wxg.assreader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.example.wxg.lottery.R

class MainActivity : AppCompatActivity(){
    private lateinit var zimuView: AssZiMuView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        zimuView = findViewById(R.id.zimu)
        zimuView.setAssZiMuPath("zimu.ass")
    }
}
