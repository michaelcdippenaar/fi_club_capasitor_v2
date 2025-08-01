<template>
  <q-page class="q-pa-md">
    <q-card>
      <q-card-section>
        <div class="text-h6">Install Certificate</div>
        <div class="text-subtitle2">Generate a key pair, sign the CSR, and install the .p12 certificate</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input v-model="alias" label="Key Alias" filled />
        <q-input v-model="commonName" label="Common Name (e.g. wifiuser@domain)" filled />
        <q-input v-model="email" label="Email (optional)" filled />
        <q-input v-model="deviceId" label="Device ID (optional)" filled />
        <q-input v-model="password" label=".p12 Password" type="password" filled />
      </q-card-section>

      <q-card-actions align="right">
        <q-btn label="Generate & Install" color="primary" @click="handleInstall" :loading="loading" />
      </q-card-actions>

      <q-card-section v-if="result">
        <div class="text-subtitle2">✅ Installed</div>
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
import { issueP12Certificate } from '../../utils/android/issueP12Certificate.js';

export default {
  name: 'CertificateInstaller',
  data () {
    return {
      alias: 'ficlub-key',
      commonName: 'wifiuser@ficlub',
      email: '',
      deviceId: '',
      password: '',
      result: null,
      error: null,
      loading: false
    }
  },
  methods: {
    async handleInstall () {
      this.result = null
      this.error = null
      this.loading = true
      try {
        const response = await issueP12Certificate({
          alias: this.alias,
          commonName: this.commonName,
          email: this.email,
          deviceId: this.deviceId,
          password: this.password
        })
        console.log('xxxxxxxxxxxxxxxxxxxxx')
        console.log(response.data)
         this.result = response?.status
        ? `✅ Certificate installed with alias ${this.alias}`
        : `⚠️ Installed, but no status returned`

        // console.log(response)
        this.result = `✅ Certificate installed with alias ${this.alias}`
      } catch (err) {
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
