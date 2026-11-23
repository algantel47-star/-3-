package com.edu.grade3guard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("GuardPrefs", Context.MODE_PRIVATE)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }

        val title = TextView(this).apply {
            text = "حارس المذاكرة - الصف الثالث الابتدائي"
            textSize = 22f
            setPadding(0, 0, 0, 32)
        }

        val btnOverlay = Button(this).apply {
            text = "1. تفعيل إذن الظهور فوق التطبيقات"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "الإذن مفعل بالفعل ✅", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val btnBattery = Button(this).apply {
            text = "2. السماح بالعمل المستمر في الخلفية"
            setOnClickListener {
                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "مفعل بالفعل ✅", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val btnStart = Button(this).apply {
            text = "3. تشغيل المراقبة الآن (كل 5 دقائق)"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    Toast.makeText(this@MainActivity, "يرجى تفعيل إذن الظهور أولاً!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val serviceIntent = Intent(this@MainActivity, QuizGuardService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                Toast.makeText(this@MainActivity, "تم تفعيل الحارس بنجاح! سيظهر سؤال كل 5 دقائق.", Toast.LENGTH_LONG).show()
                moveTaskToBack(true)
            }
        }

        val btnStop = Button(this).apply {
            text = "إيقاف المراقبة (يتطلب الرقم السري)"
            setOnClickListener {
                val input = EditText(this@MainActivity).apply { hint = "أدخل الرقم السري (الافتراضي 1234)" }
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("إيقاف الحماية")
                    .setView(input)
                    .setPositiveButton("تأكيد") { _, _ ->
                        val savedPin = prefs.getString("parent_pin", "1234")
                        if (input.text.toString() == savedPin) {
                            stopService(Intent(this@MainActivity, QuizGuardService::class.java))
                            Toast.makeText(this@MainActivity, "تم إيقاف الخدمة", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "رقم سري خاطئ!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("إلغاء", null)
                    .show()
            }
        }

        layout.addView(title)
        layout.addView(btnOverlay)
        layout.addView(btnBattery)
        layout.addView(btnStart)
        layout.addView(btnStop)

        setContentView(layout)
    }
}
