

<template>
  <q-page class="q-pa-md">
    <q-card>
      <q-card-section>
        <div class="text-h6">Android Certificate Enrollment</div>
      </q-card-section>
      <pre>{{ csr }}</pre>

<!--      <q-card-section>-->
<!--        <p v-if="loading">🔐Generating key pair...</p>-->
<!--        <p v-else-if="alias">✅ Key pair created: <code>{{ alias }}</code></p>-->
<!--        <p v-if="error" class="text-negative">{{ error }}</p>-->
<!--      </q-card-section>-->

      <q-card-actions>
        <q-btn
          color="primary"
          label="Install Configuration Profile"
          @click="requestCSR"
        />
<!--                <q-btn-->
<!--          color="primary"-->
<!--          label="Sign Data"-->
<!--          @click="signData"-->
<!--        />-->


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

import {defineComponent} from "vue";
import KeyPair from "src/utils/android/capacitor/keypair.js";

export default defineComponent({
  name: 'GetCertificateAndroid',
  data(){
    return {
      csr:'',
      error:''
  }},
  methods: {
    async generatePrivateKey() {
      try {
        await KeyPair.generateKeyPair();
        console.log('✅ Key pair generated.');
      } catch (error) {
        console.error('❌ Key generation failed:', error);
      }
    },

    async getPublicKey() {
      try {
        // Ensure key exists first
        await this.generatePrivateKey();

        // Now retrieve the public key
        const result = await KeyPair.getPublicKey();
        console.log('🔑 Public Key (Base64):', result.publicKey);
      } catch (error) {
        console.error('❌ Failed to get public key:', error);
      }
    },

    async requestCSR() {
    try {

      await KeyPair.KeyPairGen.generateKeyPair();


      const result = await KeyPair.KeyPairGen.getPublicKey()
      console.log('🔑 Public Key (Base64):', result.publicKey);

      const generated = await KeyPair.KeyPair.generateCSR();

      this.csr = generated;
      console.log('📜 CSR PEM:\n', this.csr);
  } catch (err) {
      this.error = err.message;
      console.error('❌ Failed to generate CSR:', err);
  }
}

  //   async signData() {
  //     const message = 'Hello from Capacitor plugin!';
  //     try {
  //      await this.generatePrivateKey(); // Ensure key exists
  //      const result = await KeyPair.signData({ data: message });
  //      console.log('✍️ Signed message:', message);
  //      console.log('🔏 Signature (Base64):', result.signature);
  //   } catch (error) {
  //      console.error('❌ Signing failed:', error);
  //   }
  // }
  }
})

</script>


// import { ref } from 'vue'
// import { generateAndroidKeyPair } from 'src/utils/android/key-pair-generator'
//
// const loading = ref(false)
// const error = ref(null)
// const alias = ref(null)
//
// async function installProfile() {
//   loading.value = false
//   error.value = null
//
//   try {
//     alias.value = await generateAndroidKeyPair('wifi-client-key')
//     console.log('✅ Key pair generated. Alias:', alias.value)
//   } catch (err) {
//     console.error('❌ Failed to generate key pair:', err)
//     error.value = 'Key generation failed: ' + err.message
//   } finally {
//     loading.value = false
//   }
// }

