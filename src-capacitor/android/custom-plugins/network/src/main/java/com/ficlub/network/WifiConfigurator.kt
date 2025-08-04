package com.ficlub.network

import android.Manifest
import android.content.Context
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.net.ConnectivityManager
import com.getcapacitor.*
import com.getcapacitor.annotation.CapacitorPlugin
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.util.Log
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import android.net.wifi.WifiNetworkSuggestion
import androidx.annotation.RequiresPermission

import org.json.JSONException

import java.io.ByteArrayInputStream
import android.net.MacAddress
import android.util.Base64
import androidx.annotation.RequiresApi
import com.getcapacitor.JSObject
import com.getcapacitor.PermissionState
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import android.content.Intent
import android.provider.Settings;

@CapacitorPlugin(
    name = "WifiConfiguratorPlugin",
    permissions = [
        Permission(strings = [Manifest.permission.CHANGE_WIFI_STATE], alias = "wifiState"),
        Permission(strings = [Manifest.permission.ACCESS_FINE_LOCATION], alias = "location")
    ]
)
    class WifiConfiguratorPlugin : Plugin() {

        @PluginMethod
        fun action_wifi_add_network(call: PluginCall) {
            val ssid = call.getString("ssid") ?: return call.reject("Missing SSID")
            val identity = call.getString("identity") ?: return call.reject("Missing identity")
            val alias = call.getString("alias") ?: "ficlub-key"
            val caCertBase64 = call.getString("caCertBase64") ?: return call.reject("Missing caCertBase64")

            val activity = bridge?.activity ?: return call.reject("Missing activity context")
            Log.d("CertificateAuthPlugin", "${"ssid"}: $ssid, ${"identity"}: $identity, ${"alias"}: $alias")

            try {

                val caCertBytes = Base64.decode(caCertBase64, Base64.NO_WRAP)
                val certFactory = CertificateFactory.getInstance("X.509")
                val caCert = certFactory.generateCertificate(ByteArrayInputStream(caCertBytes)) as X509Certificate

                val enterpriseConfig = WifiEnterpriseConfig().apply {
                    eapMethod = WifiEnterpriseConfig.Eap.TLS
                    phase2Method = WifiEnterpriseConfig.Phase2.NONE
                    this.identity = identity
//                    this.clientCertificateAlias = alias // Uncomment if targeting API 31+ and cert alias is needed
                    val method = this.javaClass.getMethod("setClientCertificateAlias", String::class.java)
                    domainSuffixMatch = "ficlub.co" // ✅ Required to enforce server certificate validation
                    method.invoke(this, alias)
                    this.caCertificate = caCert

                }


                val suggestion = WifiNetworkSuggestion.Builder()
                    .setSsid(ssid)
                    .setWpa2EnterpriseConfig(enterpriseConfig)
                    .setIsAppInteractionRequired(true)
                    .build()

                val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS).apply {
                    putParcelableArrayListExtra(
                        Settings.EXTRA_WIFI_NETWORK_LIST,
                        arrayListOf(suggestion)
                    )
                }

                activity.startActivity(intent)
                call.resolve()

            } catch (e: Exception) {
                Log.e("WifiConfigIntent", "❌ Failed to launch", e)
                call.reject("Launch failed: ${e.message}")
            }
        }
    }




//    @PluginMethod
//    fun connectWithCertificate (call: PluginCall) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            call.reject("Android 10 (API 29) or higher is required.")
//            return
//        }
//
//        val ssid = call.getString("ssid")
//        val alias = call.getString("alias")
//        val identity = call.getString("identity")
//
//        Log.d("ssid", "${ssid}")
//
//        if (ssid.isNullOrBlank() || alias.isNullOrBlank() || identity.isNullOrBlank()) {
//            call.reject("Missing required parameters: ssid, alias, identity")
//            return
//        }
//
//        try {
//            val enterpriseConfig = WifiEnterpriseConfig().apply {
//                eapMethod = WifiEnterpriseConfig.Eap.TLS
//                phase2Method = WifiEnterpriseConfig.Phase2.NONE
//                this.identity = identity
//
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
////                    clientCertificateAlias = alias
////                    isCaCertificateRequired = false
////                }
//            }
//
//            val wifiSpecifier = WifiNetworkSpecifier.Builder()
//                .setSsid(ssid)
//                .setWpa2EnterpriseConfig(enterpriseConfig)
//                .build()
//
//            val networkRequest = NetworkRequest.Builder()
//                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                .setNetworkSpecifier(wifiSpecifier)
//                .build()
//
//            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            Log.d("WifiConnect", "Attempting to connect to SSID: $ssid")
//            Log.d("WifiConnect", "Alias: $alias, Identity: $identity")
//// Log enterprise config
//            Log.d("WifiConnect", "EAP Method: ${enterpriseConfig.eapMethod}")
//
//            connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback() {
//                override fun onAvailable(network: android.net.Network) {
//                    super.onAvailable(network)
//                    call.resolve()
//                }
//
//
//                override fun onUnavailable() {
//                    call.reject("Connection to $ssid failed")
//                }
//
//            })
//
//        } catch (e: Exception) {
//            call.reject("Exception while connecting", e)
//        }
//    }
//

    // ssid (type: string, required): The SSID (name) of the Wi-Fi network. If not provided, the method rejects with "SSID is required".
    // caCertBase64 (type: string, required for enterprise cert auth): Base64-encoded string of the CA (Certificate Authority) certificate used for server validation. Omit only if not using cert-based validation (but for EAP-TLS, it's essential to enable secure connections).
    // clientCertBase64 (type: string, required for client auth): Base64-encoded string of the client certificate (leaf/end-entity cert). Required for EAP-TLS to authenticate the device.
    // privateKeyBase64 (type: string, required for client auth): Base64-encoded string of the PKCS8-encoded private key corresponding to the client certificate. Essential for signing during the TLS handshake.
    // identity (type: string, optional): The identity (e.g., "user@domain.com") for the outer EAP authentication. Defaults to an empty string if not provided, but recommended for proper setup.

//    @RequiresPermission(Manifest.permission.CHANGE_WIFI_STATE)
//    @RequiresApi(Build.VERSION_CODES.Q)
//    @PluginMethod
//    fun addNetworkSuggestion(call: PluginCall) {
//        if (getPermissionState("wifiState") != PermissionState.GRANTED || getPermissionState("location") != PermissionState.GRANTED) {
//            requestPermissionForAliases(arrayOf("wifiState", "location"), call, "permissionCallback")
//            return
//        }
//
//        val ssid = call.getString("ssid") ?: return call.reject("SSID is required")
//        val caCertBase64 = call.getString("caCertBase64") // Base64-encoded CA cert
//        val clientCertBase64 = call.getString("clientCertBase64") // Base64-encoded client cert (single, leaf cert)
//        val privateKeyBase64 = call.getString("privateKeyBase64") // Base64-encoded PKCS8 private key
//        val identity = call.getString("identity") ?: ""
//
//        try {
//            // Decode certificates and key (in production, load securely)
//            val certFactory = CertificateFactory.getInstance("X.509")
//            val caCert = certFactory.generateCertificate(Base64.decode(caCertBase64, Base64.DEFAULT).inputStream()) as X509Certificate
//            val clientCert = certFactory.generateCertificate(Base64.decode(clientCertBase64, Base64.DEFAULT).inputStream()) as X509Certificate
//
//            val keyFactory = KeyFactory.getInstance("RSA") // Or "EC" if using elliptic curve
//            val privateKeySpec = PKCS8EncodedKeySpec(Base64.decode(privateKeyBase64, Base64.DEFAULT))
//            val privateKey: PrivateKey = keyFactory.generatePrivate(privateKeySpec)
//
//            // Configure enterprise config for EAP-TLS
//            val enterpriseConfig = WifiEnterpriseConfig().apply {
//                eapMethod = WifiEnterpriseConfig.Eap.TLS
//                setIdentity(identity)
//                setCaCertificate(caCert) // This enables server certificate validation
//                setClientKeyEntry(privateKey, clientCert)
//            }
//
//            // Build the suggestion
//            val builder = WifiNetworkSuggestion.Builder()
//                .setSsid(ssid)
//                .setWpa2EnterpriseConfig(enterpriseConfig)
//            // Optional: .setPriority(10)
//            // .setIsHiddenSsid(true)
//
//            val suggestion = builder.build()
//
//            // Add to WifiManager
//            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
//            val suggestions = listOf(suggestion)
//            val status = wifiManager.addNetworkSuggestions(suggestions)
//
//            val ret = JSObject()
//            ret.put("status", status) // e.g., WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS = 0
//            call.resolve(ret)
//        } catch (e: Exception) {
//            call.reject("Error adding suggestion: ${e.message}")
//        }
//    }



//@RequiresPermission(Manifest.permission.CHANGE_WIFI_STATE)
//@PermissionCallback
//fun permissionCallback(call: PluginCall) {
//    if (getPermissionState("wifiState") == PermissionState.GRANTED && getPermissionState("location") == PermissionState.GRANTED) {
//        addNetworkSuggestion(call) // Retry after permissions granted
//    } else {
//        call.reject("Permissions denied")
//    }
//
//    }
//}








