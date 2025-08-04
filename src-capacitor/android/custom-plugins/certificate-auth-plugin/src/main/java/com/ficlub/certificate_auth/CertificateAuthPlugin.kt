
// KeyPairPlugin.kt - Capacitor Plugin
package com.ficlub.certificate_auth

import android.content.Intent
import android.security.KeyChain
import androidx.core.content.FileProvider
import com.getcapacitor.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.security.Security
import android.util.Base64
import android.util.Log
import android.security.KeyChainAliasCallback


import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod

import org.json.JSONObject

import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

import com.getcapacitor.JSObject


fun jsObject(builder: JSObject.() -> Unit): JSObject {
    val obj = JSObject()
    obj.builder()
    return obj
}

@NativePlugin
class CertificateAuthPlugin : Plugin() {

    // Initializes BouncyCastle provider when the plugin loads.
    override fun load() {
        super.load()
        Security.addProvider(BouncyCastleProvider())
    }

    /**
     * Generates an RSA key pair and a Certificate Signing Request (CSR).
     * Also returns the PEM-encoded private key and CSR.
     *
     * Expects:
     * - alias: String — name for the key alias
     * - commonName: String — value for CN in CSR subject
     *
     * Returns:
     * - privateKeyPem: PEM string of the private key
     * - csrPem: PEM string of the CSR
     */
    @PluginMethod
    fun generateCsrWithExportableKey(call: PluginCall) {
        val alias = call.getString("alias") ?: return call.reject("Missing alias")
        val commonName = call.getString("commonName") ?: return call.reject("Missing commonName")

        try {
            val keyPair = CertificateAuthUtils.generateExportableKeyPair()
            val csr = CertificateAuthUtils.generateCSR(commonName, keyPair)

            Log.d("keyPair","keyPair: ${keyPair}")
            Log.d("csr","csr: ${csr}")

            val privateKeyPem = CertificateAuthUtils.pemEncodePrivateKey(keyPair.private)
            val csrPem = CertificateAuthUtils.pemEncodeCsr(csr)

            Log.d("privateKeyPem","privateKeyPem: ${privateKeyPem}")
            Log.d("csrPem","srPem : ${csrPem}")

            val result = JSObject()
            result.put("privateKeyPem", privateKeyPem)
            result.put("csrPem", csrPem)
            call.resolve(result)

        } catch (e: Exception) {
            call.reject("Failed to generate CSR", e)
        }
    }

    /**
     * Builds a PKCS#12 (.p12) certificate bundle from PEM-formatted private key and certificate.
     * Returns the .p12 as a Base64-encoded string.
     *
     * Expects:
     * - alias: String — alias name for the key entry
     * - privateKey: String — PEM-formatted private key
     * - certificate: String — PEM-formatted cert chain (single or multiple)
     * - password: String — password to secure the .p12 bundle
     *
     * Returns:
     * - p12: Base64 string representing the encoded .p12 file
     */
    @PluginMethod
    fun buildP12Bundle(call: PluginCall) {
        Log.d("CertificateAuthPlugin", "buildP12Bundle call data: ${call.data}")
        Log.d("CertificateAuthPlugin", "alias: ${call.getString("alias")}")
        Log.d("CertificateAuthPlugin", "privateKey: ${call.getString("privateKeyPem")}")// trimmed for safety
        Log.d("CertificateAuthPlugin", "certificate: ${call.getString("certificate_chain")}")// trimmed
        Log.d("CertificateAuthPlugin", "password: ${call.getString("password")}")

        val alias = call.getString("alias") ?: return call.reject("Missing alias - must be a string")
        val privateKeyPem = call.getString("privateKeyPem") ?: return call.reject("Missing private key")
        val certificatePem = call.getString("certificatePem") ?: return call.reject("Missing certificate")
        val password = call.getString("password") ?: ""

        try {
            val privateKey = CertificateAuthUtils.parsePrivateKey(privateKeyPem)
            val certChain = CertificateAuthUtils.parseCertificateChain(certificatePem)
            val p12Bytes = CertificateAuthUtils.buildPkcs12(alias, privateKey, certChain, password)
            val p12Base64 = Base64.encodeToString(p12Bytes, Base64.NO_WRAP)

            val result = JSObject()
            result.put("p12", p12Base64)
            call.resolve(result)
        } catch (e: Exception) {
            Log.d("Error", "error ${e}")
            call.reject("Failed to build .p12", e)
        }
    }

    /**
     * Installs a Base64-encoded PKCS#12 (.p12) bundle to the Android KeyChain using system UI.
     *
     * Expects:
     * - alias: String — name under which cert will appear
     * - p12: String — base64-encoded .p12 content
     * - password: String — password used to secure .p12
     */
    @PluginMethod
    fun installP12Certificate(call: PluginCall) {
        val alias = call.getString("alias") ?: return call.reject("Missing alias")
        val p12Base64 = call.getString("p12") ?: return call.reject("Missing p12")
        val password = call.getString("password") ?: ""
        Log.d("installP12Certificate", "cert: ${p12Base64}")
        try {
            val p12Bytes = Base64.decode(p12Base64, Base64.NO_WRAP)

            val installIntent = KeyChain.createInstallIntent().apply {
                putExtra(KeyChain.EXTRA_NAME, alias)
                putExtra(KeyChain.EXTRA_PKCS12, p12Bytes)
            }

            activity.startActivity(installIntent)
            call.resolve()

        } catch (e: Exception) {
            call.reject("Failed to install .p12", e)
        }
    }

    @PluginMethod
    fun installClientAndCaCertificate(call: PluginCall) {
        val alias = call.getString("alias") ?: return call.reject("Missing alias - Install Certs")
        val clientCertBase64 = call.getString("clientCertBase64") ?: return call.reject("Missing client certificate")
        val caCertBase64 = call.getString("caCertBase64") ?: return call.reject("Missing CA certificate")

        try {
            // Decode certificates
            val clientCertBytes = Base64.decode(clientCertBase64, Base64.NO_WRAP)
            val caCertBytes = Base64.decode(caCertBase64, Base64.NO_WRAP)

            val certFactory = CertificateFactory.getInstance("X.509")
            val clientCert = certFactory.generateCertificate(ByteArrayInputStream(clientCertBytes)) as X509Certificate
            val caCert = certFactory.generateCertificate(ByteArrayInputStream(caCertBytes)) as X509Certificate

            // You cannot directly install certs via KeyChain unless you're installing .p12
            // But you can suggest a Wi-Fi network with client cert if the private key exists in AndroidKeyStore

            // Optionally: Save references or handle certificate presence logic here

            Log.i("KeyPairPlugin", "✅ Certificates loaded successfully for alias $alias")

            call.resolve(jsObject {
                put("status", "certificates_loaded")
            })

        } catch (e: Exception) {
            Log.e("KeyPairPlugin", "❌ Failed to load client or CA certificate", e)
            call.reject("Certificate load failed: ${e.message}")
        }
    }
}
