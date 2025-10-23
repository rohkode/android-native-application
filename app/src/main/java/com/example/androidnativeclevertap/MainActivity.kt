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
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CTInboxStyleConfig
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), CTInboxListener, DisplayUnitListener {

    private var cleverTapDefaultInstance: CleverTapAPI? = null
    private var isInboxInitialized: Boolean = false

    // 🔹 Counter for Charged Event
    private var chargedIdCounter: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // CleverTap initialization
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        if (cleverTapDefaultInstance == null) {
            Toast.makeText(this, "CleverTap initialization failed", Toast.LENGTH_SHORT).show()
            return
        }

        cleverTapDefaultInstance?.enableDeviceNetworkInfoReporting(true)
        cleverTapDefaultInstance?.ctNotificationInboxListener = this
        cleverTapDefaultInstance?.initializeInbox()
        cleverTapDefaultInstance?.setDisplayUnitListener(this) // Native Display listener
        cleverTapDefaultInstance?.recordScreen("Native Android Home Page Viewed")

        // Notification channel
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

        // Set location
        val location = Location("clevertap-provider").apply {
            latitude = 12.9716
            longitude = 77.5946
        }
        cleverTapDefaultInstance?.setLocation(location)

        // 🔹 Show default CleverTap GIF in banner
        val bannerImage = findViewById<ImageView>(R.id.bannerImage)
        Glide.with(this)
            .asGif()
            .load(R.raw.clevertap_logo) // default local gif
            .into(bannerImage)

        // Bind UI
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
        val inboxIcon = findViewById<ImageView>(R.id.inboxIcon)

        // ✅ Date picker for DOB
        dobEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selYear, selMonth, selDay ->
                    val selectedCal = Calendar.getInstance()
                    selectedCal.set(selYear, selMonth, selDay)
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dobEditText.setText(sdf.format(selectedCal.time))
                },
                year, month, day
            )
            datePicker.show()
        }

        // Open Inbox
        inboxIcon.setOnClickListener { openInbox() }

        // ✅ OnUserLogin
        onUserLoginBtn.setOnClickListener {
            val name = nameEditText.text.toString()
            val identity = identityEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val dobStr = dobEditText.text.toString()
            val gender = genderEditText.text.toString()

            if (name.isNotEmpty() && identity.isNotEmpty() && email.isNotEmpty()
                && phone.isNotEmpty() && dobStr.isNotEmpty() && gender.isNotEmpty()
            ) {
                val dobDate: Date? = try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.parse(dobStr)
                } catch (e: Exception) {
                    null
                }

                val profileUpdate = hashMapOf<String, Any>(
                    "Name" to name,
                    "Identity" to identity,
                    "Email" to email,
                    "Phone" to phone,
                    "Gender" to gender,
                    "MSG-whatsapp" to true
                )

                dobDate?.let { profileUpdate["DOB"] = it }

                cleverTapDefaultInstance?.onUserLogin(profileUpdate)
                Toast.makeText(this, "User profile updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Custom event
        customEventBtn.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Native Android Platform")
            Toast.makeText(this, "Custom Event sent", Toast.LENGTH_SHORT).show()
        }

        // Event with properties
        eventWithPropsBtn.setOnClickListener {
            val eventProps = mapOf(
                "Platform" to "Android",
                "Broadcast Receiver" to "FCM",
                "ActionBar" to 3.99,
                "Date" to Date()
            )
            cleverTapDefaultInstance?.pushEvent("Native Android Platform Properties", eventProps)
            Toast.makeText(this, "Event with Properties sent", Toast.LENGTH_SHORT).show()
        }

        // ✅ Charged Event
        chargedEventBtn.setOnClickListener {
            val chargeDetails = hashMapOf<String, Any>(
                "Charged ID" to chargedIdCounter,
                "Amount" to 399.0,
                "Payment Mode" to "Credit Card",
                "Charged Date" to Date()
            )

            val items: ArrayList<HashMap<String, Any>> = ArrayList()

            val item1 = hashMapOf<String, Any>(
                "Category" to "Books",
                "Book Name" to "The Hobbit",
                "Quantity" to 1
            )
            val item2 = hashMapOf<String, Any>(
                "Category" to "Electronics",
                "Product" to "Headphones",
                "Quantity" to 1
            )

            items.add(item1)
            items.add(item2)

            cleverTapDefaultInstance?.pushChargedEvent(chargeDetails, items)
            Toast.makeText(this, "Charged Event Sent: ID $chargedIdCounter", Toast.LENGTH_SHORT).show()

            chargedIdCounter++
        }

        // Deep links
        handleDeepLink(intent)
    }

    // ✅ Inbox listeners
    override fun inboxDidInitialize() {
        runOnUiThread {
            isInboxInitialized = true
            Toast.makeText(this, "CleverTap Inbox Initialized", Toast.LENGTH_SHORT).show()
        }
    }

    override fun inboxMessagesDidUpdate() {
        runOnUiThread {
            Toast.makeText(this, "Inbox messages updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openInbox() {
        if (!isInboxInitialized) {
            Toast.makeText(this, "Inbox not ready yet", Toast.LENGTH_SHORT).show()
            return
        }

        val inboxTabs = arrayListOf("Promotions", "Offers", "Others")
        val styleConfig = CTInboxStyleConfig().apply {
            tabs = inboxTabs
            navBarTitle = "MY INBOX"
            navBarColor = "#FFFFFF"
            navBarTitleColor = "#FF0000"
            inboxBackgroundColor = "#F0F0F0"
            selectedTabColor = "#000000"
            unselectedTabColor = "#888888"
            selectedTabIndicatorColor = "#FF0000"
        }

        cleverTapDefaultInstance?.showAppInbox(styleConfig)
    }

    // ✅ Native Display listener
    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        val bannerImage = findViewById<ImageView>(R.id.bannerImage)

        runOnUiThread {
            if (units != null && units.isNotEmpty()) {
                val firstUnit = units[0]
                val content = firstUnit.contents[0]

                // Override GIF with campaign media
                Glide.with(this)
                    .load(content.media) // could be gif, jpg, png from CleverTap
                    .into(bannerImage)

                cleverTapDefaultInstance?.pushDisplayUnitViewedEventForID(firstUnit.unitID)

                bannerImage.setOnClickListener {
                    cleverTapDefaultInstance?.pushDisplayUnitClickedEventForID(firstUnit.unitID)
                    Toast.makeText(this, "Clicked Native Display", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Show fallback GIF if no campaigns
                Glide.with(this)
                    .asGif()
                    .load(R.raw.clevertap_logo)
                    .into(bannerImage)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            cleverTapDefaultInstance?.pushNotificationClickedEvent(intent?.extras)
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        data?.let {
            val message = it.toString()
            Toast.makeText(this, "Deep link opened: $message", Toast.LENGTH_LONG).show()

            when (it.host) {
                "homepage" -> { /* Handle homepage */ }
                "product" -> { /* Handle product page */ }
            }
        }
    }
}