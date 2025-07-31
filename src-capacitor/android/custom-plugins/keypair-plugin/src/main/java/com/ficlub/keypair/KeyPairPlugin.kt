
package com.ficlub.keypair


import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

import java.security.interfaces.RSAPrivateKey
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import com.getcapacitor.JSObject

import java.security.PrivateKey
import java.security.Signature

import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder
import org.spongycastle.pkcs.PKCS10CertificationRequest
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder
import org.spongycastle.operator.ContentSigner

import java.security.Security
import java.security.interfaces.RSAPublicKey
import org.spongycastle.util.io.pem.PemObject
import java.io.StringWriter
import org.spongycastle.openssl.jcajce.JcaPEMWriter
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo
import org.spongycastle.asn1.ASN1Sequence
import java.io.OutputStream

import java.math.BigInteger
import java.util.Calendar
import java.util.Date
import javax.security.auth.x500.X500Principal



import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.security.KeyChain


import android.app.Activity

import android.content.Context
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiNetworkSpecifier
import android.net.NetworkRequest
import android.net.NetworkCapabilities
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Suppress("DEPRECATION")
@CapacitorPlugin(name = "KeyPair")
class KeyPairPlugin : Plugin() {

    private val alias = "ficlub-key"

    @PluginMethod
    fun generateKeyPair(call: PluginCall) {
        try {
            val alias = call.getString("alias") ?: "ficlub-key"
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (!keyStore.containsAlias(alias)) {
                val keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA,
                    "AndroidKeyStore"
                )

                val parameterSpec = KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                )
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setKeySize(2048)
                    .setCertificateSubject(X500Principal("CN=wifiuser,O=Ficlub,C=ZA"))
                    .setCertificateSerialNumber(BigInteger.ONE)
                    .setCertificateNotBefore(Date())
                    .setCertificateNotAfter(Calendar.getInstance().apply {
                        add(Calendar.YEAR, 10)
                    }.time)
                    .build()

                keyPairGenerator.initialize(parameterSpec)
                keyPairGenerator.generateKeyPair()

                Log.i("KeyPairPlugin", "🔐 New signing key pair generated.")
            } else {
                Log.i("KeyPairPlugin", "🔐 Key already exists")
            }

            call.resolve()
        } catch (e: Exception) {
            Log.e("KeyPairPlugin", "❌ Key generation failed", e)
            call.reject("Key generation failed: ${e.message}")
        }
    }


    @PluginMethod
    fun getPublicKey(call: PluginCall) {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val cert = keyStore.getCertificate(alias)
            val publicKey = cert.publicKey
            val publicKeyEncoded = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)

            val result = JSObject()
            result.put("publicKey", publicKeyEncoded)
            call.resolve(result)

        } catch (e: Exception) {
            Log.e("KeyPairPlugin", "❌ Failed to retrieve public key", e)
            call.reject("Public key retrieval failed: ${e.message}")
        }
    }


    @PluginMethod
    fun generateCSR(call: PluginCall) {
            val alias = call.getString("alias") ?: "ficlub-key"
            val subject = call.getString("subject") ?: "CN=wifiuser,O=Ficlub,C=ZA"

            try {
                Security.addProvider(BouncyCastleProvider())
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)

                val privateKey = keyStore.getKey(alias, null) as? PrivateKey
                    ?: throw IllegalStateException("Private key not found or invalid type")

                val publicKey = keyStore.getCertificate(alias).publicKey as RSAPublicKey

                val x500Name = X500Name(subject)

                val signature = Signature.getInstance("SHA256withRSA")
                signature.initSign(privateKey)

                val signer = object : ContentSigner {
                    override fun getAlgorithmIdentifier() =
                        org.spongycastle.asn1.x509.AlgorithmIdentifier(
                            org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers.sha256WithRSAEncryption
                        )

                    override fun getOutputStream(): OutputStream = object : OutputStream() {
                        private val buffer = mutableListOf<Byte>()

                        override fun write(b: Int) {
                            buffer.add(b.toByte())
                        }

                        override fun close() {
                            signature.update(buffer.toByteArray())
                        }
                    }

                    override fun getSignature(): ByteArray = signature.sign()
                }

                val builder = JcaPKCS10CertificationRequestBuilder(x500Name, publicKey)
                val csr = builder.build(signer)

                val csrEncoded = Base64.encodeToString(csr.encoded, Base64.NO_WRAP)
                val ret = JSObject().apply {
                    put("csr", csrEncoded)
                }

                Log.i("KeyPairPlugin", "✅ CSR generated successfully")
                call.resolve(ret)

            } catch (e: Exception) {
                Log.e("KeyPairPlugin", "❌ Failed to generate CSR", e)
                call.reject("CSR generation failed: ${e.message}")
            }
        }


    @PluginMethod
    fun installCertificate(call: PluginCall) {
        try {
            val alias = call.getString("alias") ?: return call.reject("Missing alias")
            val pemCert = call.getString("certificate") ?: return call.reject("Missing certificate")

            Log.i("KeyPairPlugin", "📥 Installing certificate for alias: $alias")

            // Load the certificate from PEM
            val certBytes = Base64.decode(
                pemCert.replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replace("\\s".toRegex(), ""),
                Base64.DEFAULT
            )

            val certificate = CertificateFactory.getInstance("X.509")
                .generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate

            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }

            // Debug: List all aliases
            val aliases = keyStore.aliases().toList()
            Log.i("KeyPairPlugin", "Available aliases: $aliases")

            // Find private key entry
            val privateKeyEntry = keyStore.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
                ?: return call.reject("Private key not found for alias: $alias")

            Log.i("KeyPairPlugin", "✅ Private key found, proceeding to install certificate.")

            // Replace certificate chain
            val certificateChain = arrayOf(certificate)
            keyStore.setEntry(
                alias,
                KeyStore.PrivateKeyEntry(privateKeyEntry.privateKey, certificateChain),
                null
            )

            Log.i("KeyPairPlugin", "✅ Certificate installed successfully for alias: $alias")
            call.resolve()

        } catch (e: Exception) {
            Log.e("KeyPairPlugin", "❌ Failed to install certificate", e)
            call.reject("Certificate installation failed: ${e.message}")
        }
    }

    @PluginMethod
    fun getCertificateDetails(call: PluginCall) {
        val alias = call.getString("alias") ?: run {
            call.reject("Missing alias")
            return
        }

        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (!keyStore.containsAlias(alias)) {
                call.reject("Alias $alias not found")
                return
            }

            val cert = keyStore.getCertificate(alias) as X509Certificate

            val result = JSObject().apply {
                put("subject", cert.subjectDN.name)
                put("issuer", cert.issuerDN.name)
                put("validFrom", cert.notBefore.time)
                put("validTo", cert.notAfter.time)
                put("serialNumber", cert.serialNumber.toString())
                put("pem", Base64.encodeToString(cert.encoded, Base64.NO_WRAP))
            }

            call.resolve(result)
        } catch (e: Exception) {
            call.reject("Error retrieving certificate: ${e.message}", e)
        }
    }

    @PluginMethod
    fun installSystemCertificate(call: PluginCall) {
        try {
            val alias = call.getString("alias") ?: "ficlub-key"
            val pemCert = call.getString("certificate") ?: return call.reject("Missing certificate")

            // Clean PEM content and decode to bytes
            val certBytes = Base64.decode(
                pemCert
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replace("\\s".toRegex(), ""),
                Base64.DEFAULT
            )

            // Parse X.509 certificate
            val cert = CertificateFactory.getInstance("X.509")
                .generateCertificate(ByteArrayInputStream(certBytes))

            // Create intent to install the certificate
            val intent = Intent("android.credentials.INSTALL").apply {
                putExtra("name", alias)
                putExtra("CERT", cert.encoded)
            }

            // Launch the intent using the activity context
            bridge.activity?.let {
                it.startActivity(intent)
                call.resolve()
            } ?: call.reject("Activity context is null")

        } catch (e: Exception) {
            Log.e("KeyPairPlugin", "❌ System certificate install failed", e)
            call.reject("System certificate install failed: ${e.message}")
        }
    }

    @PluginMethod
    fun installP12Certificate(call: PluginCall) {
        val alias = call.getString("alias") ?: return call.reject("Alias is required")
        val p12Base64 = call.getString("p12") ?: return call.reject("Missing P12")
        val password = call.getString("password") ?: ""

        try {
            val p12Bytes = Base64.decode(p12Base64, Base64.DEFAULT)

            val intent = Intent("android.credentials.INSTALL").apply {
                putExtra("name", alias)
                putExtra("PFX", p12Bytes)
                putExtra("PFX_PASSWORD", password)
            }

            bridge.activity?.let {
                it.startActivity(intent)
                call.resolve()
            } ?: call.reject("Activity context is null")

        } catch (e: Exception) {
            Log.e("KeyPairPlugin", "❌ Failed to launch certificate install intent", e)
            call.reject("Failed to install certificate: ${e.message}")
        }
    }




    @PluginMethod
    fun forWifiInstallCertificates(call: PluginCall) {
        try {
            val alias = call.getString("alias") ?: return call.reject("Alias is required")
            val certPem = call.getString("certificate") ?: return call.reject("Certificate PEM is required")

            // Decode PEM to X509Certificate
            val certBytes = Base64.decode(
                certPem.replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replace("\\s".toRegex(), ""),
                Base64.DEFAULT
            )
            val cert = CertificateFactory.getInstance("X.509")
                .generateCertificate(ByteArrayInputStream(certBytes))

            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)

            // Check if private key exists
            val privateKey = ks.getKey(alias, null) ?: return call.reject("Private key not found for alias: $alias")

            // Create cert chain and install
            val certChain = arrayOf(cert)
            ks.setEntry(alias,
                KeyStore.PrivateKeyEntry(privateKey as PrivateKey, certChain),
                null
            )

            call.resolve()
        } catch (e: Exception) {
            Log.e("WifiManager", "Certificate installation failed", e)
            call.reject("Certificate install error: ${e.localizedMessage}")
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    @PluginMethod
    fun setupWifi(call: PluginCall) {

        try {
            val ssid = call.getString("ssid") ?: return call.reject("SSID is required")
            val alias = call.getString("alias") ?: return call.reject("Alias is required")
            val identity = call.getString("identity") ?: "wifiuser"

            val wifiConfig = WifiEnterpriseConfig().apply {
                eapMethod = WifiEnterpriseConfig.Eap.TLS
                phase2Method = WifiEnterpriseConfig.Phase2.NONE
                caCertificateAlias = alias
                clientCertificateAlias = alias
                identity = identity
            }

            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2EnterpriseConfig(wifiConfig)
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build()

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    cm.bindProcessToNetwork(network)
                    call.resolve()
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    call.reject("Wi-Fi network unavailable")
                }
            })
        } catch (e: Exception) {
            Log.e("WifiManager", "Wi-Fi setup failed", e)
            call.reject("Wi-Fi setup error: ${e.localizedMessage}")
        }
    }
}

