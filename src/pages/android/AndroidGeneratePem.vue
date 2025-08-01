<template>
  <q-page class="q-pa-md">

      <q-btn
        color="primary"
        label="Generate CSR"
        @click="executeCertificateEnrollment"
      />

    <q-card>
      <q-card-section>
        <div class="text-h6">Android Certificate Enrollment</div>
          </q-card-section>

</q-card>
      <div class="q-pa-md" style="max-width: 350px">
    <q-list>
      <q-item>
        <q-item-section>
          <q-item-label>Generate Private Key</q-item-label>
        </q-item-section>

        <q-item-section side top>
          <q-icon v-if="isValidCSR && isValidPublicKey" name="check" color="green" />
        </q-item-section>
      </q-item>
      <q-separator spaced inset />
            <q-item>
              <q-item-section>
                <q-item-label>Get Valid X509 Certificate From Server</q-item-label>
              </q-item-section>

              <q-item-section side top>
                 <q-icon v-if="isValidPem509x" name="check" color="green" />
              </q-item-section>
      </q-item>
                  <q-separator spaced inset />
            <q-item>
              <q-item-section>
                <q-item-label>Store Certificate in Android Key Store </q-item-label>
              </q-item-section>
                    <q-separator spaced inset />
              <q-item-section side top>
                 <q-icon v-if="certificateExistsLocally" name="check" color="green" />
              </q-item-section>
          </q-item>
      <q-separator spaced inset />

            <q-item>
              <q-item-section>
                <q-item-label>Configure FiClub Wifi Settings </q-item-label>
              </q-item-section>
                  <q-separator spaced inset />
              <q-item-section side top>
              </q-item-section>
          </q-item>
      <q-separator spaced inset />
          <q-item>
              <q-item-section>
                <q-item-label>Connect to FiClub </q-item-label>
              </q-item-section>

              <q-item-section side top>
              </q-item-section>
          </q-item>
    </q-list>
        </div>

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
import { KeyPair, KeyPairPlugin } from 'src/utils/android/capacitor/keypair.js';
import WifiUtils from 'src/utils/android/capacitor/wifi';
import { fileToBase64 } from 'src/utils/common/base64FileUtils';


export default defineComponent({
  name: 'GetCertificateAndroid',
  data() {
    return {
      alias: 'ficlub-key',
      csr: '',
      publicKey: '',
      pem509x: {},
      serverCrt:{},
      certificateExistsLocally: false,
      certificateExistsPublic: false,
      p12File: null,

    }
  },
  computed:{
    isValidCSR() {
      if (!this.csr) return false;
      else{
        return true
      }
    },
    isValidPublicKey() {
      if (!this.publicKey) return false;
        else{
          return true
      }
    },
    isValidPublicPrivateKeyPair() {
      if (this.publicKey && this.csr) return true;
        else{
          return false
      }
    },
    isDoneRequestCSR() {
      return this.isValidCSR && this.isValidPublicKey;
    },
    isValidPem509x() {
      return this.pem509x && this.pem509x.pem && this.pem509x.pem.includes('BEGIN CERTIFICATE') && this.pem509x.pem.includes('END CERTIFICATE');
    }
  },
  methods: {
    async executeCertificateEnrollment(){
      // await this.fetchServerCrt()
      await this.requestCSR()
      await this.postSignRequest()
      await this.installCert()
      console.log('[KeyPairPlugin] ✅ setupWifiFlow');
      await this.setupWifiFlow()
      console.log('[KeyPairPlugin] ✅ Process Completed');

    },
    async installCert() {

      try {

        console.log('[KeyPairPlugin] Installing Certificate', this.pem509x.pem);


        const base64EncodedP12 = await fileToBase64(this.p12File);
        await KeyPair.installP12Certificate({
          alias: 'ficlub-key',
          p12: base64EncodedP12,
          password: '' // optional
        });
        await WifiUtils.installCertificate('ficlub-key', this.pem509x.pem);
        this.certificateExistsLocally = true;
      } catch (err) {
        this.certificateExistsLocally = false;
        console.error('[KeyPairPlugin] ❌ Failed to install certificate:', err);
      }
    },

    async setupWifiFlow() {
      console.log('[KeyPairPlugin] Start SetupWifiFlow');
    try {
      await WifiUtils.connectToWifi(
        '#FiClub', 'wifiuser', 'ficlub-key'
      );
    } catch (err) {
      console.error("[KeyPairPlugin] ❌ Setup failed", err);
    }
},
    async fetchServerCrt(){
        // Only for Development
        try {
          const response = await fetch('http://192.168.1.51:8000/quasar/api/wifi/android/private_crt/');
          const certPem = await response.text();

          console.log('📥 Received certificate:', certPem);
          this.serverCrt = {
            alias: 'ficlub-key',
            certificate: certPem,
          }
          await Capacitor.Plugins.KeyPair.installSystemCertificate(this.serverCrt);

          console.log('✅ Certificate installation triggered');
        } catch (error) {
          console.error('❌ Error installing certificate:', error);
        }
      },

    async requestCSR() {
      this.csr = '';
      this.publicKey = '';
      try {
        // Generate a key pair if not exists
        await KeyPair.generateKeyPair();

        // Get public key
        const publicKey = await KeyPairPlugin.getPublicKey();
        this.publicKey = publicKey.publicKey;

        // Generate CSR
        const csrResult = await KeyPair.generateCSR();
        this.csr = csrResult;

      } catch (err) {
        this.error = err.message;
        console.error('❌ requestCS:', err);
      }
    },

    async postSignRequest() {
      try {


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
        try {
          const data = JSON.parse(rawResponse);
          certificatePem = data.certificate || data.cert || data.pem; // Adjust based on server response
        } catch (jsonErr) {
          console.log(jsonErr);
          certificatePem = rawResponse; // Fallback to plain PEM
        }


        await KeyPair.installCertificate(this.alias, certificatePem);

        this.pem509x = {
          alias: this.alias,
          pem: certificatePem
        };


        const check = await this.checkIfCertificateExists
        if (check) {
          this.certificateExistsLocally = true
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
      const alias = this.alias;
      KeyPair.getCertificateDetails(alias)
      console.log('✅ Check Done');
    },

  //   async installSystemCertificate(){
  //
  // try {
  //   await KeyPairPlugin.installSystemCertificate({
  //     alias: this.alias,
  //     certificate: this.pem509x.pem,
  //   });
  //   this.certificateExistsPublic = true
  //   console.log("✅ Certificate installation triggered.");
  // } catch (err) {
  //   this.certificateExistsPublic = false
  //   console.error("❌ Failed to install certificate:", err);
  // }
  //
  //   }
  }
}
);
</script>
