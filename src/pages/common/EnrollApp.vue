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

async function goToGetCertificate() {
  const platform = getPlatform();
  console.log('Detected platform:', platform); // Log to confirm 'android'

  if (platform === 'ios') {
    console.log('Redirecting to iOS certificate route');
    await router.push('/certificate')
      .then(() => console.log('iOS navigation successful'))
      .catch(err => {
        console.error('Navigation error (iOS):', err);
        alert('Navigation failed: ' + err.message);
      });
  } else if (platform === 'android') {
    console.log('Redirecting to Android setup'); // Confirm this logs
    await router.push('/android_connect_ssid')
      .then(() => console.log('Android navigation successful'))
      .catch(err => {
        console.error('Navigation error (Android):', err);
        alert('Navigation failed: ' + err.message);
      });
  } else {
    console.error('Unsupported platform:', platform);
    alert('Unsupported platform: ' + platform);
  }
}
</script>

<style scoped>
.text-center {
  text-align: center;
}
</style>
