

<template>
  <q-page class="q-pa-md">
    <q-card>
      <q-card-section>
        <div class="text-h6">Android Certificate Enrollment</div>
      </q-card-section>

<!--      <q-card-section>-->
<!--        <p v-if="loading">🔐Generating key pair...</p>-->
<!--        <p v-else-if="alias">✅ Key pair created: <code>{{ alias }}</code></p>-->
<!--        <p v-if="error" class="text-negative">{{ error }}</p>-->
<!--      </q-card-section>-->

      <q-card-actions>
        <q-btn
          color="primary"
          label="Install Configuration Profile"
          @click="testCustomPlugin"
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

import {defineComponent} from "vue";
import KeyPair from "src/utils/android/capacitor/keypair.js";

export default defineComponent({
  name: 'GetCertificateAndroid',
  methods: {
    testCustomPlugin(){
      KeyPair.sayHello()
    }
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

