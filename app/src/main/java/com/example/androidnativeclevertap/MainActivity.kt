package com.example.androidnativeclevertap

import android.app.DatePickerDialog
import android.app.NotificationManager
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.clevertap.android.sdk.*
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.android.sdk.variables.Var
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), CTInboxListener, DisplayUnitListener {

    private var cleverTapDefaultInstance: CleverTapAPI? = null
    private var isInboxInitialized: Boolean = false

    // Product Experience / Remote Config variables
    private lateinit var showBanner: Var<Boolean>
    private lateinit var bannerUrl: Var<String>
    private lateinit var homeTitle: Var<String>

    // CleverTap Logout Function
    private fun logoutCleverTap() {

        // Clear CleverTap stored data
        val prefs = applicationContext.getSharedPreferences("WizRocket", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Reset instances
        CleverTapAPI.setInstances(null)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        // debug new ID
        cleverTapDefaultInstance?.getCleverTapID {
            android.util.Log.d("CT_LOGOUT", "New CleverTapID: $it")
        }
    }

    // Charged Event
    private fun nextChargeId(): Int {
        val prefs = getSharedPreferences("clevertap_demo_prefs", MODE_PRIVATE)
        val next = prefs.getInt("charged_id_counter", 0) + 1
        prefs.edit().putInt("charged_id_counter", next).apply()
        return next
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        if (cleverTapDefaultInstance == null) {
            Toast.makeText(this, "CleverTap initialization failed", Toast.LENGTH_SHORT).show()
            return
        }

        // ------------------ Remote Config Variables ------------------
        showBanner = cleverTapDefaultInstance!!.defineVariable(
            "show_home_banner",
            true
        )
        bannerUrl = cleverTapDefaultInstance!!.defineVariable(
            "home_banner_url",
            "https://via.placeholder.com/600x200"
        )
        homeTitle = cleverTapDefaultInstance!!.defineVariable(
            "home_title_text",
            "Welcome to CleverTap Android"
        )

        // DEV only
        cleverTapDefaultInstance?.syncVariables()
        // -------------------------------------------------------------

        cleverTapDefaultInstance?.enableDeviceNetworkInfoReporting(true)
        cleverTapDefaultInstance?.ctNotificationInboxListener = this
        cleverTapDefaultInstance?.initializeInbox()
        cleverTapDefaultInstance?.setDisplayUnitListener(this)
        cleverTapDefaultInstance?.recordScreen("Native Android Home Page Viewed")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CleverTapAPI.createNotificationChannel(
                applicationContext,
                "nativeandroid",
                "Android Native",
                "Android Native",
                NotificationManager.IMPORTANCE_MAX,
                true,
                "gameofthrones.mp3"
            )
        }

        val location = Location("clevertap-provider").apply {
            latitude = 12.9716
            longitude = 77.5946
        }
        cleverTapDefaultInstance?.setLocation(location)

        val bannerImage = findViewById<ImageView>(R.id.bannerImage)
        Glide.with(this)
            .asGif()
            .load(R.raw.clevertap_logo)
            .into(bannerImage)

        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val identityEditText = findViewById<EditText>(R.id.editTextIdentity)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
        val dobEditText = findViewById<EditText>(R.id.editTextDOB)
        val genderEditText = findViewById<EditText>(R.id.editTextGender)

        val onUserLoginBtn = findViewById<Button>(R.id.btnOnUserLogin)
        val customEventBtn = findViewById<Button>(R.id.btn_custom_event)
        val eventWithPropsBtn = findViewById<Button>(R.id.btn_event_with_props)
        val chargedEventBtn = findViewById<Button>(R.id.btn_charged_event)
        val logoutBtn = findViewById<Button>(R.id.btn_logout)
        val inboxIcon = findViewById<ImageView>(R.id.inboxIcon)

        logoutBtn.setOnClickListener {
            logoutCleverTap()
            Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show() // debug
        }

        dobEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    val cal = Calendar.getInstance()
                    cal.set(y, m, d)
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dobEditText.setText(sdf.format(cal.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        inboxIcon.setOnClickListener { openInbox() }

        onUserLoginBtn.setOnClickListener {
            val profileUpdate = hashMapOf<String, Any>(
                "Name" to nameEditText.text.toString(),
                "Identity" to identityEditText.text.toString(),
                "Email" to emailEditText.text.toString(),
                "Phone" to phoneEditText.text.toString(),
                "Gender" to genderEditText.text.toString(),
                "MSG-whatsapp" to true
            )

            val dob = try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .parse(dobEditText.text.toString())
            } catch (_: Exception) {
                null
            }
            dob?.let { profileUpdate["DOB"] = it }

            cleverTapDefaultInstance?.onUserLogin(profileUpdate)

            cleverTapDefaultInstance?.fetchVariables { success ->
                if (success) {
                    applyRemoteConfig()
                }
            }

            Toast.makeText(this, "User profile updated", Toast.LENGTH_SHORT).show()
        }

        customEventBtn.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Native Android Platform")
        }

        eventWithPropsBtn.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent(
                "Native Android Platform Properties",
                mapOf("Platform" to "Android", "Date" to Date())
            )
        }

        chargedEventBtn.setOnClickListener {
            val chargeDetails = hashMapOf<String, Any>(
                "Charged ID" to nextChargeId(),
                "Amount" to 399.0,
                "Charged Date" to Date()
            )

            val items: ArrayList<HashMap<String, Any>> = ArrayList()

            val item1: HashMap<String, Any> = hashMapOf(
                "Category" to "Books",
                "Book Name" to "The Hobbit",
                "Quantity" to 1
            )

            val item2: HashMap<String, Any> = hashMapOf(
                "Category" to "Electronics",
                "Product" to "Headphones",
                "Quantity" to 1
            )

            items.add(item1)
            items.add(item2)

            cleverTapDefaultInstance?.pushChargedEvent(chargeDetails, items)
        }

        handleDeepLink(intent)
    }


    override fun inboxDidInitialize() {
        isInboxInitialized = true
    }

    override fun inboxMessagesDidUpdate() {}

    private fun openInbox() {
        if (!isInboxInitialized) return
        cleverTapDefaultInstance?.showAppInbox(CTInboxStyleConfig())
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        val bannerImage = findViewById<ImageView>(R.id.bannerImage)

        runOnUiThread {
            if (!showBanner.value()) {
                bannerImage.visibility = ImageView.GONE
                return@runOnUiThread
            }

            bannerImage.visibility = ImageView.VISIBLE

            if (!units.isNullOrEmpty()) {
                val unit = units[0]
                Glide.with(this)
                    .load(unit.contents[0].media)
                    .into(bannerImage)

                cleverTapDefaultInstance?.pushDisplayUnitViewedEventForID(unit.unitID)
            } else {
                Glide.with(this)
                    .load(bannerUrl.value())
                    .into(bannerImage)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let {
            Toast.makeText(this, "Deep link opened: $it", Toast.LENGTH_LONG).show()
        }
    }

    // ------------------ Remote Config Apply ------------------
    private fun applyRemoteConfig() {
        val bannerImage = findViewById<ImageView>(R.id.bannerImage)

        runOnUiThread {
            bannerImage.visibility =
                if (showBanner.value()) ImageView.VISIBLE else ImageView.GONE

            if (showBanner.value()) {
                Glide.with(this)
                    .load(bannerUrl.value())
                    .into(bannerImage)
            }

            title = homeTitle.value()
        }
    }
}