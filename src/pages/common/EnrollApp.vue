<!-- src/pages/EnrollApp.vue -->
<template>
  <q-page class="q-pa-md flex flex-center">
    <div class="text-center">
      <h2>Welcome to Enrollment</h2>
      <p>You are now logged in. Continue your enrollment process here.</p>
      <q-btn color="primary" label="Next" @click="goToGetCertificate" />
    </div>
  </q-page>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { getPlatform } from 'src/utils/common/os.js'

const router = useRouter()

function goToGetCertificate() {
  if(getPlatform()==='ios'){
    router.push('/certificate')
  }

  else if(getPlatform()==='android') {
    router.push('/p12_android')
  }
}
</script>

<style scoped>
.text-center {
  text-align: center;
}
</style>
