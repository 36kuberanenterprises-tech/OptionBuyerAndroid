package com.kuberan.optionbuyer

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var strategy: Spinner
    private lateinit var index: Spinner
    private lateinit var spot: EditText
    private lateinit var rsi: EditText
    private lateinit var upper: EditText
    private lateinit var lower: EditText
    private lateinit var otm: EditText
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        scroll.addView(root)

        root.addView(TextView(this).apply {
            text = "Option Buyer"
            textSize = 28f
            gravity = Gravity.CENTER_HORIZONTAL
        })

        root.addView(TextView(this).apply {
            text = "Fresh Android build based on the supplied option buyer strategy rules"
            textSize = 14f
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 8, 0, 24)
        })

        strategy = addSpinner(root, "Strategy", listOf("Indicator", "Level Breakout"))
        index = addSpinner(root, "Index", listOf("NIFTY", "BANKNIFTY", "FINNIFTY"))
        spot = addNumber(root, "Spot price")
        rsi = addNumber(root, "RSI 14")
        upper = addNumber(root, "Upper level")
        lower = addNumber(root, "Lower level")
        otm = addSignedNumber(root, "OTM points, 0 ATM, negative ITM")
        otm.setText("0")

        val analyse = Button(this).apply {
            text = "ANALYSE"
            setOnClickListener { analyse() }
        }
        root.addView(analyse, matchWrap())

        result = TextView(this).apply {
            text = "WAIT"
            textSize = 22f
            setPadding(0, 28, 0, 12)
        }
        root.addView(result, matchWrap())

        root.addView(TextView(this).apply {
            text = "Current build works as a manual strategy calculator. Live broker feed and live order placement are not enabled in this APK."
            textSize = 13f
            setPadding(0, 18, 0, 24)
        })

        setContentView(scroll)
    }

    private fun analyse() {
        val s = spot.text.toString().toDoubleOrNull()
        if (s == null || s <= 0) {
            result.text = "Enter a valid spot price"
            return
        }

        val selectedIndex = index.selectedItem.toString()
        val otmPoints = otm.text.toString().toIntOrNull() ?: 0
        val step = if (selectedIndex == "BANKNIFTY") 100 else 50
        val atm = (s / step).roundToInt() * step
        val ceStrike = atm + otmPoints
        val peStrike = atm - otmPoints

        val signal = if (strategy.selectedItem.toString() == "Indicator") {
            val r = rsi.text.toString().toDoubleOrNull()
            when {
                r == null -> "WAIT"
                r > 50.0 -> "BUY CE"
                r < 50.0 -> "BUY PE"
                else -> "WAIT"
            }
        } else {
            val u = upper.text.toString().toDoubleOrNull()
            val l = lower.text.toString().toDoubleOrNull()
            when {
                u == null || l == null -> "WAIT"
                s > u -> "BUY CE"
                s < l -> "BUY PE"
                else -> "WAIT"
            }
        }

        val strike = when (signal) {
            "BUY CE" -> ceStrike
            "BUY PE" -> peStrike
            else -> atm
        }

        result.text = buildString {
            append("Signal: $signal\n")
            append("Index: $selectedIndex\n")
            append("Spot: $s\n")
            append("ATM: $atm\n")
            append("Selected strike: $strike\n")
            append("CE strike: $ceStrike\n")
            append("PE strike: $peStrike")
        }
    }

    private fun addSpinner(root: LinearLayout, label: String, items: List<String>): Spinner {
        root.addView(label(label))
        return Spinner(this).also { sp ->
            sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
            root.addView(sp, matchWrap())
        }
    }

    private fun addNumber(root: LinearLayout, hint: String): EditText {
        return EditText(this).also {
            it.hint = hint
            it.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            root.addView(it, matchWrap())
        }
    }

    private fun addSignedNumber(root: LinearLayout, hint: String): EditText {
        return EditText(this).also {
            it.hint = hint
            it.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            root.addView(it, matchWrap())
        }
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text
        textSize = 14f
        setPadding(0, 16, 0, 4)
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
