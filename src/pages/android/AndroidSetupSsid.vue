
<template>
  <q-page class="q-pa-md">
    <q-card>
      <q-card-section>
        <div class="text-h6">Install Wi-Fi Certificate & Connect</div>
        <div class="text-subtitle2">Generate key pair, retrieve cert, and configure Wi-Fi access</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input v-model="alias" label="Key Alias" filled />
        <q-input v-model="commonName" label="Common Name (e.g. wifiuser@domain)" filled />
        <q-input v-model="email" label="Email (optional)" filled />
        <q-input v-model="deviceId" label="Device ID (optional)" filled />
        <q-input v-model="ssid" label="Wi-Fi SSID" filled />
      </q-card-section>

      <q-card-actions align="right">
        <q-btn label="Generate & Configure" color="primary" @click="handleProvisioning" :loading="loading" />
      </q-card-actions>

      <q-card-section v-if="result">
        <div class="text-subtitle2">✅ Success</div>
        <q-banner dense>{{ result }}</q-banner>
      </q-card-section>

      <q-card-section v-if="error">
        <div class="text-subtitle2 text-negative">❌ Error</div>
        <q-banner dense class="bg-red-2">{{ error }}</q-banner>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script>
import { submitCsr } from '../../utils/android/api/certificateAuthApi.js'
import { generateKeyPair, exportPublicKeyToCSR } from '../../utils/android/capacitor/key-utils.js'
// import { installP12 } from '../../utils/android/capacitor/certificate-auth.js'
//
import { storeCertificateAndKey } from '../../utils/android/capacitor/network.capasitor.js'
 // import { Capacitor } from '@capacitor/core';
 // import { WifiConfigurator } from 'wifi-configurator-plugin';
// import { Capacitor } from '@capacitor/core';

import { configureWifi } from '../../utils/android/capacitor/wifiConfigurator.js';


export default {
  name: 'AndroidSetupSsid',
  data () {
    return {
      alias: 'ficlub-key',
      commonName: 'wifiuser@ficlub',
      email: 'mc@tremly.com',
      deviceId: '123456789',
      ssid: '#FiClub',
      identity: 'wifiuser@ficlub',
      result: null,
      error: null,
      loading: false
    }
  },
  methods: {
    printPemToConsole(base64, label = "CERTIFICATE") {
      const header = `-----BEGIN ${label}-----\n`;
      const footer = `\n-----END ${label}-----`;
      const formatted = base64.match(/.{1,64}/g).join('\n');
      console.log(header + formatted + footer);
    },

    async ConfigerWifi() {
      try {
        await configureWifi(this.ssid, this.identity, this.alias);
        this.result = 'Wi-Fi network suggestion submitted';
      } catch (err) {
        this.error = err.message || 'Wi-Fi configuration failed';
  }
},
    async handleProvisioning () {
      this.result = null
      this.error = null
      this.loading = true
      try {
        // 1. Generate key pair
        console.error('[MC_Tag] Initiate Generate key pair')
        await generateKeyPair(this.alias)
        console.log('Key pair generated successfully')

        // 2. Generate CSR
        const csr = await exportPublicKeyToCSR(this.alias, this.commonName)
        // console.log('Generate CSR',csr)

        // 3. Submit CSR to backend
        const response = await submitCsr(csr, this.email, this.deviceId)
        // console.log('Submit CSR to backend',response)

        const {
          //certificate_chain
          caCertBase64,
          clientCertBase64,
          // privateKeyBase64,
          // p12Base64
        } = response

        await this.printPemToConsole(caCertBase64, 'caCert.pem', 'CERTIFICATE');
        await this.printPemToConsole(clientCertBase64, 'clientCert.pem', 'CERTIFICATE');


        console.log('Alias Is: --- ', this.alias)
        await storeCertificateAndKey({
          alias: this.alias,
          clientCertBase64,
          caCertBase64
        })


        // 5. Suggest Wi-Fi network using the certs
        // await this.ConfigerWifi()
        await configureWifi(this.ssid, this.identity, this.alias, caCertBase64);


      } catch (err) {
        console.error('❌ Error during provisioning:', JSON.stringify(err, null, 2))
        this.error = err?.message || 'Unknown error'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.q-card {
  max-width: 500px;
  margin: auto;
}
</style>
