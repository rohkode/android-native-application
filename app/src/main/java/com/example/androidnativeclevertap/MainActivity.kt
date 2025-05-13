package com.example.androidnativeclevertap;

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.clevertap.android.sdk.CleverTapAPI
import com.example.androidnativeclevertap.ui.theme.AndroidNativeClevertapTheme
import com.google.type.Date

class MainActivity : ComponentActivity() {
    private var cleverTapDefaultInstance: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        // Check if CleverTap is initialized correctly
        if (cleverTapDefaultInstance == null) {
            Toast.makeText(this, "CleverTap initialization failed", Toast.LENGTH_SHORT).show()
            return
        }

        setContent {
            AndroidNativeClevertapTheme {

                var name by remember { mutableStateOf("") }
                var identity by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var phone by remember { mutableStateOf("") }
                var gender by remember { mutableStateOf("") }

                // Login button click action
                val onLoginClick = {
                    if (name.isNotEmpty() && identity.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && gender.isNotEmpty()) {
                        try {
                            val profileUpdate = hashMapOf<String, Any>(
                                "Name" to name,
                                "Identity" to identity,
                                "Email" to email,
                                "Phone" to phone,
                                "Gender" to gender,
                            ) as HashMap<String, Any>  // Cast the map to match expected type

                            cleverTapDefaultInstance?.onUserLogin(profileUpdate)
                            Toast.makeText(this, "User data sent to CleverTap!", Toast.LENGTH_SHORT)
                                .show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                }

                val onCustomEventClick = {
                    cleverTapDefaultInstance?.pushEvent("Native Android Platform")
                    Toast.makeText(this, "Custom Event Button", Toast.LENGTH_SHORT).show()
                }
                val onEventWithPropsClick = {
                    val eventProps = mapOf(
                        "Platform" to "Android system",
                        "Broadcast Receiver" to "FCM Service",
                        "ActionBar" to 3.99,
                        "Date" to java.util.Date()
                    )
                    cleverTapDefaultInstance?.pushEvent("Native Android Platform Properties", eventProps)
                    Toast.makeText(this, "Event with Properties Sent", Toast.LENGTH_SHORT).show()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") })
                        TextField(
                            value = identity,
                            onValueChange = { identity = it },
                            label = { Text("Identity") })
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") })
                        TextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") })
                        TextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") })

                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text("Login")
                        }
                        Button(
                            onClick = onCustomEventClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text("Track Custom Event")
                        }

                        Button(
                            onClick = onEventWithPropsClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text("Track Event With Properties")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserLoginScreen() {
    AndroidNativeClevertapTheme {
        // You can directly use the UI logic inside the preview as well if needed
        var name by remember { mutableStateOf("") }
        var identity by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("") }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = identity, onValueChange = { identity = it }, label = { Text("Identity") })
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                TextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") })

                Button(
                    onClick = { /* Login action if needed */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Login")
                }
            }
        }
    }
}