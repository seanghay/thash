package com.seanghay.thashsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.text = "${thashView.progress.toInt()}%"
        thashView.progressChanged { progress, fraction ->
            textView.scaleX =  1 + fraction
            textView.scaleY = 1 + fraction
            textView.text = "${progress.toInt()}%"

        }

        button2.setOnClickListener {
            thashView.animateAngle()
        }


        button.setOnClickListener {
            thashView.animateProgress((0..100).random().toFloat())
        }


    }
}
