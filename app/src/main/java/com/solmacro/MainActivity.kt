package com.solmacro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad * 2)
        }
        root.addView(tv("Sol Macro", 26f))
        root.addView(tv("Auto fishing and auto selling for Sol’s RNG using screen taps. Not affiliated with Roblox. Macros may break game rules and can get accounts banned.", 13f))
        status = tv("", 14f)
        root.addView(status)
        root.addView(btn("1. Turn on the accessibility service") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
        root.addView(btn("2. Open Roblox") {
            val i = packageManager.getLaunchIntentForPackage(Cfg.ROBLOX)
            if (i != null) startActivity(i) else Toast.makeText(this, "Roblox is not installed", Toast.LENGTH_SHORT).show()
        })
        root.addView(tv("3. In Roblox, tap SET and tap the 5 buttons it asks for. For detection: cast once, and while the bite sign is showing tap COLOR, mark the area and the sign, then tap TEST to check it. 4. Tap START. Drag the buttons to move them.", 14f))

        root.addView(tv("Presets", 16f))
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(btn("Fast") { preset(700, 3000, 15, 80) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(btn("Balanced") { preset(900, 4000, 12, 120) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(btn("Safe") { preset(1000, 5500, 10, 160) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(row)

        field(root, "Cast hold (ms)", "cast")
        field(root, "Bite wait (ms)", "wait")
        field(root, "Reel taps", "taps")
        field(root, "Tap gap (ms)", "gap")
        field(root, "Menu delay (ms)", "mdelay")
        field(root, "Sell every N catches", "every")
        field(root, "Stop after N catches (0 = never)", "maxcatch")
        root.addView(tv("Screen detection (needs Android 11 or newer)", 16f))
        field(root, "Color match tolerance (0-255)", "tol")
        field(root, "Minimum matching pixels", "minpx")
        field(root, "Max seconds to wait for a bite", "maxwait")
        val dsw = Switch(this).apply {
            text = "Use screen detection instead of timing"
            isChecked = Cfg.get(this@MainActivity, "detect") == 1
            setOnCheckedChangeListener { _, on -> Cfg.put(this@MainActivity, "detect", if (on) 1 else 0) }
        }
        root.addView(dsw)
        val sw = Switch(this).apply {
            text = "Auto sell"
            isChecked = Cfg.get(this@MainActivity, "sell") == 1
            setOnCheckedChangeListener { _, on -> Cfg.put(this@MainActivity, "sell", if (on) 1 else 0) }
        }
        root.addView(sw)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        val on = (Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "")
            .contains("$packageName/")
        val pts = Cfg.keys.joinToString("\n") { k -> "${Cfg.label[k]}: ${Cfg.get(this, k + "_x")}, ${Cfg.get(this, k + "_y")}" }
        status.text = "Service: " + (if (on) "ON" else "OFF") + "\n" + pts
    }

    private fun preset(cast: Int, wait: Int, taps: Int, gap: Int) {
        Cfg.put(this, "cast", cast); Cfg.put(this, "wait", wait); Cfg.put(this, "taps", taps); Cfg.put(this, "gap", gap)
        recreate()
    }

    private fun tv(t: String, size: Float) = TextView(this).apply {
        text = t
        textSize = size
        setPadding(0, 12, 0, 12)
    }

    private fun btn(t: String, f: () -> Unit) = Button(this).apply {
        text = t
        isAllCaps = false
        setOnClickListener { f() }
    }

    private fun field(root: LinearLayout, label: String, key: String) {
        root.addView(tv(label, 12f))
        val e = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(Cfg.get(this@MainActivity, key).toString())
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    Cfg.put(this@MainActivity, key, s.toString().toIntOrNull() ?: 0)
                }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        root.addView(e)
    }
}
