// CertificateAuthUtils.kt
package com.ficlub.certificate_auth

import java.io.StringReader
import java.io.StringWriter
import java.io.ByteArrayOutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Security
import java.security.cert.X509Certificate
import java.security.KeyStore
import javax.security.auth.x500.X500Principal

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder



object CertificateAuthUtils {

    /**
     * Generates an exportable RSA key pair (2048 bits) using BouncyCastle provider.
     * @return KeyPair object containing public and private keys.
     */
    fun generateExportableKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * Generates a Certificate Signing Request (CSR) using a given Common Name and key pair.
     * @param commonName The CN to use for the CSR (e.g. user@domain.com).
     * @param keyPair The RSA KeyUtils (private + public) used to sign the request.
     * @return A PKCS10CertificationRequest (raw CSR object).
     */
    fun generateCSR(commonName: String, keyPair: KeyPair): PKCS10CertificationRequest {
        val subject = X500Principal("CN=$commonName")
        val signer = JcaContentSignerBuilder("SHA256withRSA")
            .setProvider(BouncyCastleProvider())
            .build(keyPair.private)
        return JcaPKCS10CertificationRequestBuilder(subject, keyPair.public)
            .build(signer)
    }

    /**
     * Encodes a private key to PEM format.
     * @param privateKey The PrivateKey object to encode.
     * @return PEM-encoded private key as a String.
     */
    fun pemEncodePrivateKey(privateKey: PrivateKey): String {
        val writer = StringWriter()
        JcaPEMWriter(writer).use { it.writeObject(privateKey) }
        return writer.toString()
    }

    /**
     * Encodes a PKCS#10 CSR into PEM format.
     * @param csr The PKCS10CertificationRequest to encode.
     * @return PEM-encoded CSR as a String.
     */
    fun pemEncodeCsr(csr: PKCS10CertificationRequest): String {
        val writer = StringWriter()
        JcaPEMWriter(writer).use { it.writeObject(csr) }
        return writer.toString()
    }

    /**
     * Parses a PEM-encoded private key string into a PrivateKey object.
     * @param pem PEM-formatted private key string.
     * @return PrivateKey object.
     */
    fun parsePrivateKey(pem: String): PrivateKey {
        val parser = PEMParser(StringReader(pem))
        val obj = parser.readObject()
        val converter = JcaPEMKeyConverter()
        return when (obj) {
            is PEMKeyPair -> converter.getKeyPair(obj).private
            is PrivateKeyInfo -> converter.getPrivateKey(obj)
            else -> throw IllegalArgumentException("Unsupported private key format")
        }
    }

    /**
     * Parses a PEM string containing one or more certificates and returns them as an array.
     * @param pem PEM-formatted certificate chain string (one or more certs).
     * @return Array of X509Certificate objects.
     */
    fun parseCertificateChain(pem: String): Array<X509Certificate> {
        val certs = mutableListOf<X509Certificate>()
        val parser = PEMParser(StringReader(pem))
        val converter = JcaX509CertificateConverter()

        var obj = parser.readObject()
        while (obj != null) {
            if (obj is X509CertificateHolder) {
                certs.add(converter.getCertificate(obj))
            }
            obj = parser.readObject()
        }
        return certs.toTypedArray()
    }

    /**
     * Creates a PKCS#12 (.p12) bundle from a private key and certificate chain.
     * @param alias Alias to store the key entry under.
     * @param privateKey The private key associated with the certificate.
     * @param chain Array of X509Certificate forming the certificate chain.
     * @param password Password to protect the .p12 file.
     * @return ByteArray of the .p12 binary content.
     */
    fun buildPkcs12(alias: String, privateKey: PrivateKey, chain: Array<X509Certificate>, password: String): ByteArray {
        val ks = KeyStore.getInstance("PKCS12")
        ks.load(null, null)
        ks.setKeyEntry(alias, privateKey, password.toCharArray(), chain)

        val out = ByteArrayOutputStream()
        ks.store(out, password.toCharArray())
        return out.toByteArray()
    }

}

