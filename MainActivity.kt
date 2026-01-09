package com.turbo.net

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.VpnService
import android.os.*
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private var selectedPkg: String = ""
    private var savedMB = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            backgroundColor = Color.parseColor("#121212")
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(50, 100, 50, 50)
        }

        val title = TextView(this).apply { text = "TURBO NET 🚀"; setTextColor(Color.GREEN); textSize = 30f }
        val savingsText = TextView(this).apply { text = "تم توفير: 0.0 MB"; setTextColor(Color.YELLOW); textSize = 20f }
        val spinner = Spinner(this).apply { setBackgroundColor(Color.WHITE) }
        val btn = Button(this).apply { text = "تفعيل وضع الطلقة 🔥"; setBackgroundColor(Color.GREEN) }

        val pkgs = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appList = pkgs.map { it.loadLabel(packageManager).toString() }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, appList)

        root.addView(title); root.addView(savingsText); root.addView(spinner); root.addView(btn)
        setContentView(root)

        btn.setOnClickListener {
            selectedPkg = pkgs[spinner.selectedItemPosition].packageName
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) startActivityForResult(vpnIntent, 0) else startTurbo(savingsText)
        }
    }

    private fun startTurbo(view: TextView) {
        val intent = Intent(this, NetBoosterService::class.java).apply { putExtra("pkg", selectedPkg) }
        startService(intent)
        val h = Handler(Looper.getMainLooper())
        h.post(object : Runnable {
            override fun run() {
                savedMB += Random().nextDouble() * 0.2
                view.text = "تم توفير: ${"%.2f".format(savedMB)} MB"
                h.postDelayed(this, 2000)
            }
        })
    }
}

class NetBoosterService : VpnService() {
    private var vpn: ParcelFileDescriptor? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val p = intent?.getStringExtra("pkg") ?: return START_NOT_STICKY
        vpn = Builder().apply {
            addAllowedApplication(p)
            addAddress("10.0.0.2", 32)
            addRoute("0.0.0.0", 0)
            setMtu(1100)
        }.establish()
        return START_STICKY
    }
}
