package com.seanghay.thashsample

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.animation.ArgbEvaluatorCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableStatusBar()

        val colorPrimary = Color.GREEN
        val evaluator = ArgbEvaluatorCompat.getInstance()

        textView.text = "${thashView.progress.toInt()}%"
        slider.isFloatingLabel = false

        slider.setOnChangeListener { slider, fraction ->

            textView.text = "${thashView.progress.roundToInt()}%"

            textView.scaleX = 1 + fraction
            textView.scaleY = 1 + fraction

            val progressColor = evaluator.evaluate(fraction, colorPrimary, Color.RED)
            thashView.progressColor = progressColor
            button.setTextColor(progressColor)
            button2.setTextColor(progressColor)

            thashView.setProgress(fraction * 100f, false)

        }


        slider.post {
            thashView.progressChanged { progress, fraction ->
                slider.value = fraction
            }
        }



        button2.setOnClickListener {
            thashView.animateAngle()
        }

        button.setOnClickListener {
            thashView.animateProgress((0..100).random().toFloat())
        }


    }

    private fun enableStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
    }
}
