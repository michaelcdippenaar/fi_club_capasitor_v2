<template>
  <q-page class="q-pa-md">
    <q-card>
      <q-card-section>
        <div class="text-h6">TEST Android Certificate Enrollment</div>
      </q-card-section>
      <div>CSR: {{ csr }}</div>
      <div>Public Key: {{ publicKey }}</div>
      <div>{{pem509x}}</div>
      <div v-if="error" class="text-negative">{{ error }}</div>

      <q-card-actions>
        <q-btn
          color="primary"
          label="Generate CSR"
          @click="requestCSR"
        />
        <q-btn
          color="primary"
          label="Post Cert"
          @click="postSignRequest"
        />

          <q-btn
          color="primary"
          label="Install System Certificate"
          @click="installSystemCertificate"
        />

      </q-card-actions>
    </q-card>
  </q-page>
</template>

<style scoped>
code {
  font-family: monospace;
  font-size: 0.95em;
  background-color: #f2f2f2;
  padding: 2px 4px;
  border-radius: 4px;
}
</style>

<script>
import { defineComponent } from 'vue';
import { KeyPair, KeyPairPlugin } from 'src/utils/android/capacitor/key-utils.js';

export default defineComponent({
  name: 'GetCertificateAndroid',
  data() {
    return {
      csr: '',
      error: '',
      publicKey: '',
      pem509x: {}

    };
  },
  methods: {
      async fetchServerCrt(){
        try {
          const response = await fetch('http://192.168.1.51:8000/quasar/api/csr/submit/');
          const certPem = await response.text();

          console.log('📥 Received certificate:', certPem);

          await Capacitor.Plugins.KeyPair.installSystemCertificate({
            alias: 'ficlub-key',
            certificate: certPem,
          });

          console.log('✅ Certificate installation triggered');
        } catch (error) {
          console.error('❌ Error installing certificate:', error);
        }
      },
    async requestCSR() {
      this.csr = '';
      this.publicKey = '';
      this.error = '';
      try {
        // Generate a key pair if not exists
        await KeyPair.generateKeyPair();

        // Get public key
        const publicKeyResult = await KeyPairPlugin.getPublicKey();
        this.publicKey = publicKeyResult.publicKey;


        // Generate CSR
        const csrResult = await KeyPair.generateCSR();
        this.csr = csrResult;
        console.log('📜 CSR (Base64):', this.csr);
      } catch (err) {
        this.error = err.message;
        console.error('❌ Failed to generate CSR:', err);
      }
    },

    async postSignRequest() {
      try {
        if (!this.csr || !this.publicKey) {
          throw new Error('CSR or Public Key not generated');
        }

        // Send raw Base64-encoded CSR and public key (no PEM wrapping)
        console.log('Sending CSR (Base64):', this.csr);
        console.log('Sending Public Key (Base64):', this.publicKey);

        // Try JSON payload
        let response = await fetch('http://192.168.1.51:8000/quasar/api/wifi/android/sign_request/', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ csr: this.csr, public_key: this.publicKey })
        });

        if (!response.ok) {
          // Fallback to FormData
          const formData = new FormData();
          formData.append('csr', this.csr);
          formData.append('public_key', this.publicKey);
          response = await fetch('http://192.168.1.51:8000/quasar/api/wifi/android/sign_request/', {
            method: 'POST',
            body: formData
          });
        }

        if (!response.ok) {
          throw new Error(`Server error: ${response.statusText} (${response.status})`);
        }

        let certificatePem;
        const rawResponse = await response.text();
        console.log('Raw server response:', rawResponse);
        try {
          const data = JSON.parse(rawResponse);
          certificatePem = data.certificate || data.cert || data.pem; // Adjust based on server response
        } catch (jsonErr) {
          console.log(jsonErr);
          certificatePem = rawResponse; // Fallback to plain PEM
        }

        if (!certificatePem || !certificatePem.includes('BEGIN CERTIFICATE')) {
          throw new Error('Invalid or no certificate returned from server');
        }

        console.log('📜 Certificate PEM:\n', certificatePem);

        const alias = 'ficlub-key';
        await KeyPair.installCertificate(alias, certificatePem);

        this.pem509x = {
          alias: alias,
          pem: certificatePem
        };


        const check = await this.checkIfCertificateExists
        if (check) {
          console.log(JSON.stringify(check));
          this.pem509x.certificate = JSON.stringify(check);
        } else {
          console.error('❌ Certificate does not exist in KeyStore');
        }
        this.error = '';
      } catch (err) {
        this.error = err.message;
        console.error('❌ Failed to install certificate:', err);
      }
    },

    checkIfCertificateExists() {
      const alias = 'ficlub-key';
      KeyPair.getCertificateDetails(alias)
      console.log('✅ Check Done');
    },

    async installSystemCertificate(){
      console.log('Installing Certificate', this.pem509x.pem);
  try {
    if (!this.pem509x.pem) {
      console.error("❌ No certificate to install");
      return;
    }
    await KeyPairPlugin.installSystemCertificate({
      alias: 'ficlub-key',
      certificate: this.pem509x.pem,
    });
    console.log("✅ Certificate installation triggered.");
  } catch (err) {
    console.error("❌ Failed to install certificate:", err);
  }


    }
  }
}
);
</script>
