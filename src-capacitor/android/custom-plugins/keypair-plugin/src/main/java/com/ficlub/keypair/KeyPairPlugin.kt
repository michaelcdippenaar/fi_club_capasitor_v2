
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
    }


//    @PluginMethod
//    fun signData(call: PluginCall) {
//        val dataToSign = call.getString("data")?.toByteArray()
//
//        if (dataToSign == null) {
//            call.reject("Missing data to sign")
//            return
//        }
//
//        try {
//            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
//            val entry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
//            val privateKey = entry.privateKey
//
//            val signature = Signature.getInstance("SHA256withRSA")
//            signature.initSign(privateKey)
//            signature.update(dataToSign)
//
//            val signatureBytes = signature.sign()
//            val signatureBase64 = Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
//
//            val result = JSObject()
//            result.put("signature", signatureBase64)
//            call.resolve(result)
//
//        } catch (e: Exception) {
//            Log.e("KeyPairPlugin", "❌ Signing failed", e)
//            call.reject("Signing failed: ${e.message}")
//        }
//    }

