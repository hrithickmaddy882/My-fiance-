package com.myfinance.app

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.provider.Settings

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("myfinance", Context.MODE_PRIVATE) }
    private var balance = 0.0
    private var expenses = 0.0
    private lateinit var balanceText: TextView
    private lateinit var expensesText: TextView
    private lateinit var historyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        balance = prefs.getString("balance", "0")?.toDoubleOrNull() ?: 0.0
        expenses = prefs.getString("expenses", "0")?.toDoubleOrNull() ?: 0.0
        showHome()
        createChannel()
        requestNotificationPermissionIfNeeded()
        updateNotification()
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 20)
            setBackgroundColor(Color.rgb(15, 25, 35))
        }
        val title = text("MyFinance", 28, Color.WHITE, true)
        root.addView(title)
        root.addView(text("Your personal expense tracker", 14, 0xFFB8C3CC.toInt(), false))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = rounded(0xFF202F3D.toInt(), 22)
        }
        val cardParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = 24 }
        root.addView(card, cardParams)
        card.addView(text("AVAILABLE BALANCE", 13, 0xFFCFB16B.toInt(), true))
        balanceText = text(money(balance), 34, Color.WHITE, true)
        card.addView(balanceText)
        card.addView(text("Today's recorded expenses", 13, 0xFFB8C3CC.toInt(), false))
        expensesText = text(money(expenses), 22, 0xFFFFC46B.toInt(), true)
        card.addView(expensesText)

        val setBalance = button("Set / Update bank balance") { askAmount("Enter current PNB balance") { amount ->
            balance = amount
            save()
            refresh()
        } }
        root.addView(setBalance, spaced())
        root.addView(button("＋  Add expense") { askAmount("Enter expense amount") { amount ->
            if (amount <= 0) return@askAmount
            balance -= amount
            expenses += amount
            val old = prefs.getString("history", "") ?: ""
            val line = "Spent ${money(amount)}"
            prefs.edit().putString("history", (line + "\n" + old).take(3000)).apply()
            save()
            refresh()
        } }, spaced())
        root.addView(button("Reset today's expense total") {
            AlertDialog.Builder(this).setTitle("Reset today's total?")
                .setMessage("This resets the displayed daily expense total only. It does not add money back to your balance.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset") { _, _ -> expenses = 0.0; save(); refresh() }.show()
        }, spaced())

        root.addView(text("Recent transactions", 18, Color.WHITE, true).apply {
            setPadding(0, 24, 0, 8)
        })
        historyText = text(prefs.getString("history", "No expenses recorded yet") ?: "", 14, 0xFFE0E6EA.toInt(), false)
        val scroll = ScrollView(this)
        scroll.addView(historyText)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        root.addView(text("Note: This app does not connect directly to PNB or PhonePe. Enter/update your bank balance and expenses yourself, or use transaction details you receive.", 12, 0xFFB8C3CC.toInt(), false).apply {
            setPadding(0, 16, 0, 0)
        })
        setContentView(root)
    }

    private fun refresh() {
        balanceText.text = money(balance)
        expensesText.text = money(expenses)
        historyText.text = prefs.getString("history", "No expenses recorded yet") ?: ""
        updateNotification()
    }

    private fun save() {
        prefs.edit().putString("balance", balance.toString())
            .putString("expenses", expenses.toString()).apply()
    }

    private fun askAmount(title: String, onDone: (Double) -> Unit) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Amount in ₹"
        }
        AlertDialog.Builder(this).setTitle(title).setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount == null || amount < 0) {
                    Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                } else onDone(amount)
            }.show()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("balance_channel", "Balance reminder", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Shows the balance saved in MyFinance"
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
        }
    }

    private fun updateNotification() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = getSystemService(NotificationManager::class.java)
        val builder = if (Build.VERSION.SDK_INT >= 26)
            android.app.Notification.Builder(this, "balance_channel")
        else android.app.Notification.Builder(this)
        val notification = builder.setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("MyFinance • Balance")
            .setContentText("Recorded balance: ${money(balance)}")
            .setOngoing(true)
            .build()
        manager.notify(1001, notification)
    }

    private fun text(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setTextColor(Color.WHITE)
        background = rounded(0xFF9A762E.toInt(), 14)
        setOnClickListener { action() }
        isAllCaps = false
    }

    private fun spaced() = LinearLayout.LayoutParams(-1, -2).apply { topMargin = 10 }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }
    private fun money(value: Double) = "₹" + String.format("%,.2f", value)
}
