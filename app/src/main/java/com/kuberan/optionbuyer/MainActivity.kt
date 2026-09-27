package com.kuberan.optionbuyer

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var strategy: Spinner
    private lateinit var index: Spinner

    private lateinit var spotLayout: TextInputLayout
    private lateinit var spot: TextInputEditText
    private lateinit var rsiLayout: TextInputLayout
    private lateinit var rsi: TextInputEditText
    private lateinit var upperLayout: TextInputLayout
    private lateinit var upper: TextInputEditText
    private lateinit var lowerLayout: TextInputLayout
    private lateinit var lower: TextInputEditText
    private lateinit var otmLayout: TextInputLayout
    private lateinit var otm: TextInputEditText

    private lateinit var resultCard: MaterialCardView
    private lateinit var signalView: TextView
    private lateinit var resultDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE

        val page = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(247, 249, 252))
            clipToPadding = false
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
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        root.addView(TextView(this).apply {
            text = "Manual strategy calculator based on the supplied option buyer rules"
            textSize = 12.5f
            setTextColor(Color.rgb(100, 116, 139))
            setPadding(0, dp(4), 0, dp(16))
        })

        val setupCard = card()
        val setupBody = cardBody()
        setupCard.addView(setupBody)
        root.addView(setupCard, cardParams())

        setupBody.addView(sectionTitle("Market setup"))
        strategy = spinnerField(setupBody, "Strategy", listOf("Indicator", "Level Breakout"))
        index = spinnerField(setupBody, "Index", listOf("NIFTY", "BANKNIFTY", "FINNIFTY"))

        val inputsCard = card()
        val inputsBody = cardBody()
        inputsCard.addView(inputsBody)
        root.addView(inputsCard, cardParams(dp(12)))

        inputsBody.addView(sectionTitle("Strategy input"))
        spotLayout = textField(inputsBody, "Spot price", false)
        spot = spotLayout.editText as TextInputEditText

        rsiLayout = textField(inputsBody, "RSI 14", false)
        rsi = rsiLayout.editText as TextInputEditText

        upperLayout = textField(inputsBody, "Upper level", false)
        upper = upperLayout.editText as TextInputEditText

        lowerLayout = textField(inputsBody, "Lower level", false)
        lower = lowerLayout.editText as TextInputEditText

        otmLayout = textField(inputsBody, "Strike offset", true)
        otm = otmLayout.editText as TextInputEditText
        otm.setText("0")

        inputsBody.addView(TextView(this).apply {
            text = "0 = ATM   •   Positive = OTM   •   Negative = ITM"
            textSize = 11.5f
            setTextColor(Color.rgb(100, 116, 139))
            setPadding(dp(2), 0, 0, dp(2))
        })

        val analyseButton = MaterialButton(this).apply {
            text = "ANALYSE"
            textSize = 14f
            cornerRadius = dp(12)
            insetTop = 0
            insetBottom = 0
            minHeight = dp(52)
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(25, 86, 180))
            setOnClickListener { analyse() }
        }
        root.addView(analyseButton, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(14)
        })

        resultCard = card().apply {
            visibility = View.GONE
            strokeColor = Color.rgb(204, 214, 226)
        }
        val resultBody = cardBody()
        resultCard.addView(resultBody)
        root.addView(resultCard, cardParams(dp(14)))

        resultBody.addView(sectionTitle("Analysis result"))
        signalView = TextView(this).apply {
            textSize = 24f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(27, 39, 56))
            setPadding(0, dp(2), 0, dp(8))
        }
        resultBody.addView(signalView)

        resultDetails = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.rgb(71, 85, 105))
            setLineSpacing(0f, 1.2f)
        }
        resultBody.addView(resultDetails)

        root.addView(TextView(this).apply {
            text = "Live broker data and live order placement are not enabled in this APK."
            textSize = 11.5f
            setTextColor(Color.rgb(120, 132, 150))
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(12), dp(18), dp(12), 0)
        })

        strategy.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateStrategyFields()
                resultCard.visibility = View.GONE
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

        setContentView(page)
        updateStrategyFields()
    }

    private fun updateStrategyFields() {
        val indicator = strategy.selectedItem?.toString() != "Level Breakout"
        rsiLayout.visibility = if (indicator) View.VISIBLE else View.GONE
        upperLayout.visibility = if (indicator) View.GONE else View.VISIBLE
        lowerLayout.visibility = if (indicator) View.GONE else View.VISIBLE
    }

    private fun analyse() {
        clearErrors()

        val spotValue = spot.text?.toString()?.trim()?.toDoubleOrNull()
        if (spotValue == null || spotValue <= 0.0) {
            spotLayout.error = "Enter a valid spot price"
            spot.requestFocus()
            return
        }

        val selectedIndex = index.selectedItem.toString()
        val otmPoints = otm.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val step = if (selectedIndex == "BANKNIFTY") 100 else 50
        val atm = (spotValue / step).roundToInt() * step
        val ceStrike = atm + otmPoints
        val peStrike = atm - otmPoints

        val selectedStrategy = strategy.selectedItem.toString()
        val signal = if (selectedStrategy == "Indicator") {
            val rsiValue = rsi.text?.toString()?.trim()?.toDoubleOrNull()
            if (rsiValue == null) {
                rsiLayout.error = "Enter RSI 14"
                rsi.requestFocus()
                return
            }
            when {
                rsiValue > 50.0 -> "BUY CE"
                rsiValue < 50.0 -> "BUY PE"
                else -> "WAIT"
            }
        } else {
            val upperValue = upper.text?.toString()?.trim()?.toDoubleOrNull()
            val lowerValue = lower.text?.toString()?.trim()?.toDoubleOrNull()

            if (upperValue == null) {
                upperLayout.error = "Enter upper level"
                upper.requestFocus()
                return
            }
            if (lowerValue == null) {
                lowerLayout.error = "Enter lower level"
                lower.requestFocus()
                return
            }
            if (lowerValue >= upperValue) {
                lowerLayout.error = "Lower level must be below upper level"
                lower.requestFocus()
                return
            }

            when {
                spotValue > upperValue -> "BUY CE"
                spotValue < lowerValue -> "BUY PE"
                else -> "WAIT"
            }
        }

        val strike = when (signal) {
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
            append("$selectedIndex   •   $selectedStrategy\n")
            append("Spot: ${formatNumber(spotValue)}\n")
            append("ATM: $atm\n")
            append("Selected strike: $strike\n")
            append("CE strike: $ceStrike   •   PE strike: $peStrike")
        }
        resultCard.visibility = View.VISIBLE
    }

    private fun clearErrors() {
        spotLayout.error = null
        rsiLayout.error = null
        upperLayout.error = null
        lowerLayout.error = null
        otmLayout.error = null
    }

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.2f", value)
    }

    private fun spinnerField(parent: LinearLayout, label: String, items: List<String>): Spinner {
        parent.addView(TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(Color.rgb(100, 116, 139))
            setPadding(dp(2), dp(10), 0, dp(4))
        })

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, dp(6), 0)
            background = roundedBackground(Color.WHITE, Color.rgb(205, 215, 228), dp(10))
        }

        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                items
            )
            minimumHeight = dp(48)
        }
        wrapper.addView(spinner, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(48)
        ))
        parent.addView(wrapper, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(48)
        ))
        return spinner
    }

    private fun textField(parent: LinearLayout, hint: String, signed: Boolean): TextInputLayout {
        val layout = TextInputLayout(this).apply {
            this.hint = hint
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxStrokeColor = Color.rgb(154, 169, 188)
            boxBackgroundColor = Color.WHITE
            setBoxCornerRadii(
                dp(10).toFloat(), dp(10).toFloat(), dp(10).toFloat(), dp(10).toFloat()
            )
        }

        val edit = TextInputEditText(this).apply {
            textSize = 15f
            setTextColor(Color.rgb(30, 41, 59))
            setHintTextColor(Color.rgb(120, 132, 150))
            inputType = if (signed) {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            } else {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
            setSingleLine(true)
        }
        layout.addView(edit, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        parent.addView(layout, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(10)
        })
        return layout
    }

    private fun card(): MaterialCardView = MaterialCardView(this).apply {
        radius = dp(16).toFloat()
        cardElevation = dp(1).toFloat()
        strokeWidth = dp(1)
        strokeColor = Color.rgb(224, 231, 239)
        setCardBackgroundColor(Color.WHITE)
    }

    private fun cardBody(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(16))
    }

    private fun sectionTitle(textValue: String): TextView = TextView(this).apply {
        text = textValue
        textSize = 14f
        setTextColor(Color.rgb(51, 65, 85))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 0, 0, dp(2))
    }

    private fun cardParams(topMargin: Int = 0): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        this.topMargin = topMargin
    }

    private fun roundedBackground(fill: Int, stroke: Int, radius: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(fill)
            setStroke(dp(1), stroke)
            cornerRadius = radius.toFloat()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
}
