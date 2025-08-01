
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

            val privateKeyPem = CertificateAuthUtils.pemEncodePrivateKey(keyPair.private)
            val csrPem = CertificateAuthUtils.pemEncodeCsr(csr)

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
        val alias = call.getString("alias") ?: return call.reject("Missing alias")
        val privateKeyPem = call.getString("privateKey") ?: return call.reject("Missing private key")
        val certificatePem = call.getString("certificate") ?: return call.reject("Missing certificate")
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

        try {
            val p12Bytes = Base64.decode(p12Base64, Base64.NO_WRAP)
            val file = File(context.cacheDir, "$alias.p12")
            file.writeBytes(p12Bytes)

            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)

            val intent = Intent("com.android.credentials.INSTALL").apply {
                setDataAndType(uri, "application/x-pkcs12")
                putExtra("name", alias)
                putExtra("password", password)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            activity.startActivity(intent)
            call.resolve()

        } catch (e: Exception) {
            call.reject("Failed to install .p12", e)
        }
    }
}
