package com.kuberan.optionbuyer

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import kotlin.math.roundToInt

class MainActivity : Activity() {

    private lateinit var strategy: Spinner
    private lateinit var index: Spinner
    private lateinit var spot: EditText
    private lateinit var rsi: EditText
    private lateinit var upper: EditText
    private lateinit var lower: EditText
    private lateinit var otm: EditText
    private lateinit var rsiRow: LinearLayout
    private lateinit var upperRow: LinearLayout
    private lateinit var lowerRow: LinearLayout
    private lateinit var resultCard: LinearLayout
    private lateinit var signalView: TextView
    private lateinit var resultDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE

        val page = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(247, 249, 252))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(28))
        }
        page.addView(root)

        root.addView(TextView(this).apply {
            text = "Option Buyer"
            textSize = 22f
            setTextColor(Color.rgb(27, 39, 56))
            setTypeface(typeface, Typeface.BOLD)
        })

        root.addView(TextView(this).apply {
            text = "Manual option buyer strategy calculator"
            textSize = 12.5f
            setTextColor(Color.rgb(100, 116, 139))
            setPadding(0, dp(4), 0, dp(14))
        })

        val setupCard = makeCard()
        val setupBody = makeCardBody()
        setupCard.addView(setupBody)
        root.addView(setupCard, cardParams())

        setupBody.addView(sectionTitle("Market setup"))
        strategy = addSpinner(setupBody, "Strategy", listOf("Indicator", "Level Breakout"))
        index = addSpinner(setupBody, "Index", listOf("NIFTY", "BANKNIFTY", "FINNIFTY"))

        val inputCard = makeCard()
        val inputBody = makeCardBody()
        inputCard.addView(inputBody)
        root.addView(inputCard, cardParams(dp(12)))

        inputBody.addView(sectionTitle("Strategy input"))
        spot = addNumberField(inputBody, "Spot price", false).second
        val rsiPair = addNumberField(inputBody, "RSI 14", false)
        rsiRow = rsiPair.first
        rsi = rsiPair.second
        val upperPair = addNumberField(inputBody, "Upper level", false)
        upperRow = upperPair.first
        upper = upperPair.second
        val lowerPair = addNumberField(inputBody, "Lower level", false)
        lowerRow = lowerPair.first
        lower = lowerPair.second
        otm = addNumberField(inputBody, "Strike offset", true).second
        otm.setText("0")

        inputBody.addView(TextView(this).apply {
            text = "0 = ATM     Positive = OTM     Negative = ITM"
            textSize = 11.5f
            setTextColor(Color.rgb(100, 116, 139))
            setPadding(dp(2), dp(2), 0, 0)
        })

        val analyse = Button(this).apply {
            text = "ANALYSE"
            textSize = 14f
            setTextColor(Color.WHITE)
            isAllCaps = true
            background = rounded(Color.rgb(25, 86, 180), 0, dp(12))
            setOnClickListener { analyse() }
        }
        root.addView(analyse, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(14)
        })

        resultCard = makeCard().apply {
            visibility = View.GONE
        }
        val resultBody = makeCardBody()
        resultCard.addView(resultBody)
        root.addView(resultCard, cardParams(dp(14)))

        resultBody.addView(sectionTitle("Analysis result"))
        signalView = TextView(this).apply {
            textSize = 24f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(27, 39, 56))
            setPadding(0, dp(2), 0, dp(8))
        }
        resultBody.addView(signalView)

        resultDetails = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.rgb(71, 85, 105))
            setLineSpacing(0f, 1.15f)
        }
        resultBody.addView(resultDetails)

        root.addView(TextView(this).apply {
            text = "Live broker data and live order placement are not enabled in this APK."
            textSize = 11.5f
            setTextColor(Color.rgb(120, 132, 150))
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(12), dp(18), dp(12), 0)
        })

        strategy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateFields()
                resultCard.visibility = View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        setContentView(page)
        updateFields()
    }

    private fun updateFields() {
        val indicator = strategy.selectedItem?.toString() != "Level Breakout"
        rsiRow.visibility = if (indicator) View.VISIBLE else View.GONE
        upperRow.visibility = if (indicator) View.GONE else View.VISIBLE
        lowerRow.visibility = if (indicator) View.GONE else View.VISIBLE
    }

    private fun analyse() {
        clearFieldErrors()

        val spotValue = spot.text.toString().trim().toDoubleOrNull()
        if (spotValue == null || spotValue <= 0.0) {
            spot.error = "Enter a valid spot price"
            spot.requestFocus()
            return
        }

        val selectedIndex = index.selectedItem.toString()
        val offset = otm.text.toString().trim().toIntOrNull() ?: 0
        val step = if (selectedIndex == "BANKNIFTY") 100 else 50
        val atm = (spotValue / step).roundToInt() * step
        val ceStrike = atm + offset
        val peStrike = atm - offset

        val selectedStrategy = strategy.selectedItem.toString()
        val signal = if (selectedStrategy == "Indicator") {
            val rsiValue = rsi.text.toString().trim().toDoubleOrNull()
            if (rsiValue == null) {
                rsi.error = "Enter RSI 14"
                rsi.requestFocus()
                return
            }
            when {
                rsiValue > 50.0 -> "BUY CE"
                rsiValue < 50.0 -> "BUY PE"
                else -> "WAIT"
            }
        } else {
            val upperValue = upper.text.toString().trim().toDoubleOrNull()
            val lowerValue = lower.text.toString().trim().toDoubleOrNull()

            if (upperValue == null) {
                upper.error = "Enter upper level"
                upper.requestFocus()
                return
            }
            if (lowerValue == null) {
                lower.error = "Enter lower level"
                lower.requestFocus()
                return
            }
            if (lowerValue >= upperValue) {
                lower.error = "Lower level must be below upper level"
                lower.requestFocus()
                return
            }

            when {
                spotValue > upperValue -> "BUY CE"
                spotValue < lowerValue -> "BUY PE"
                else -> "WAIT"
            }
        }

        val selectedStrike = when (signal) {
            "BUY CE" -> ceStrike
            "BUY PE" -> peStrike
            else -> atm
        }

        signalView.text = signal
        signalView.setTextColor(
            when (signal) {
                "BUY CE" -> Color.rgb(20, 125, 78)
                "BUY PE" -> Color.rgb(190, 63, 63)
                else -> Color.rgb(100, 116, 139)
            }
        )

        resultDetails.text = buildString {
            append("$selectedIndex   $selectedStrategy\n")
            append("Spot: ${formatNumber(spotValue)}\n")
            append("ATM: $atm\n")
            append("Selected strike: $selectedStrike\n")
            append("CE strike: $ceStrike\n")
            append("PE strike: $peStrike")
        }
        resultCard.visibility = View.VISIBLE
    }

    private fun clearFieldErrors() {
        spot.error = null
        rsi.error = null
        upper.error = null
        lower.error = null
        otm.error = null
    }

    private fun addSpinner(parent: LinearLayout, label: String, items: List<String>): Spinner {
        parent.addView(fieldLabel(label))
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
            minimumHeight = dp(48)
            background = rounded(Color.WHITE, Color.rgb(205, 215, 228), dp(10))
            setPadding(dp(12), 0, dp(10), 0)
        }
        parent.addView(spinner, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(48)
        ))
        return spinner
    }

    private fun addNumberField(parent: LinearLayout, hint: String, signed: Boolean): Pair<LinearLayout, EditText> {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, 0)
        }
        val edit = EditText(this).apply {
            this.hint = hint
            textSize = 15f
            setTextColor(Color.rgb(30, 41, 59))
            setHintTextColor(Color.rgb(120, 132, 150))
            setSingleLine(true)
            inputType = if (signed) {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            } else {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
            background = rounded(Color.WHITE, Color.rgb(205, 215, 228), dp(10))
            setPadding(dp(12), 0, dp(12), 0)
        }
        row.addView(edit, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(50)
        ))
        parent.addView(row, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        return Pair(row, edit)
    }

    private fun fieldLabel(value: String): TextView = TextView(this).apply {
        text = value
        textSize = 12f
        setTextColor(Color.rgb(100, 116, 139))
        setPadding(dp(2), dp(10), 0, dp(4))
    }

    private fun makeCard(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(Color.WHITE, Color.rgb(224, 231, 239), dp(16))
    }

    private fun makeCardBody(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(16))
    }

    private fun sectionTitle(value: String): TextView = TextView(this).apply {
        text = value
        textSize = 14f
        setTextColor(Color.rgb(51, 65, 85))
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, 0, 0, dp(2))
    }

    private fun cardParams(topMargin: Int = 0): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        this.topMargin = topMargin
    }

    private fun rounded(fill: Int, stroke: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fill)
            if (stroke != 0) setStroke(dp(1), stroke)
            cornerRadius = radius.toFloat()
        }
    }

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.2f", value)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
}
