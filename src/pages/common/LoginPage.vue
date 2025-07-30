<template>
  <q-page class="q-pa-md">

    <!-- Header -->
    <div class="text-h4 text-center q-my-xl">
      Welcome to Ficlub
    </div>

    <!-- Login Form -->
    <div class="row">
      <div class="col-12">
        <q-form @submit.prevent="login" class="q-gutter-md">

          <!-- Username -->
          <q-input
            v-model="form.username"
            outlined
            square
            label="Username"
            type="text"
            required
            lazy-rules
            :rules="[(val) => !!val || 'Username is required']"
            @focus="onFocus"
            class="rounded-input text-body1"
          />

          <!-- Password -->
          <q-input
            v-model="form.password"
            outlined
            square
            label="Password"
            type="password"
            required
            lazy-rules
            :rules="[(val) => !!val || 'Password is required']"
            @focus="onFocus"
            class="rounded-input text-body1"
          />

          <!-- Sign In Button -->
          <div class="row q-mt-lg">
            <div class="col-12">
              <q-btn
                class="full-width text-body1"
                type="submit"
                color="primary"
                label="Sign In"
                :loading="loading"
                :disable="loading"
              />
            </div>
          </div>

          <!-- Forgot Password -->
          <div class="row justify-end">
            <q-btn flat class="q-mt-sm">
              <div class="text-caption text-grey-7">Forgot password?</div>
            </q-btn>
          </div>

        </q-form>
      </div>
    </div>

    <!-- Divider -->
    <div class="flex flex-center">
      <div class="q-my-xl" style="width: 85%; height: 2px; background: rgba(108,100,100,0.38);"></div>
    </div>

    <!-- Social Login -->
    <div class="text-subtitle2 text-center q-mb-sm">Or login with</div>
    <div class="q-pb-md flex flex-center q-gutter-md">
      <q-btn round color="blue" icon="fa-brands fa-facebook-f" />
      <q-btn round color="red" icon="fa-brands fa-google" />
      <q-btn round color="purple" icon="fa-brands fa-instagram" />
    </div>

    <!-- Sign Up CTA -->
    <div class="fixed-bottom flex flex-center q-pa-md q-mb-md">
      <q-btn flat>
        <div class="text-caption q-pr-sm text-black">Don’t have an account?</div>
        <div class="text-caption text-primary">Create an Account</div>
      </q-btn>
    </div>

  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from 'src/boot/axios'
import { Keyboard } from '@capacitor/keyboard'
import { saveToken } from 'src/utils/ios/storage'

const router = useRouter()
const form = ref({ username: 'mc@tremly.com', password: 'Number55dip' })
const loading = ref(false)

async function login() {
  loading.value = true
  try {
    const response = await api.post('/login/', form.value, {
      headers: { 'Content-Type': 'application/json' },
    })

    await saveToken(response.data.token)
    router.push('/enroll')
  } catch (error) {
    console.error('❌ Login failed', error)
  } finally {
    loading.value = false
  }
}

async function onFocus(event) {
  event.target.focus()
  if (window.Capacitor) {
    try {
      await Keyboard.show()
    } catch (error) {
      console.error('Failed to show keyboard:', error)
    }
  }
}
</script>

<style scoped>
.rounded-input .q-field__control {
  border-radius: 8px;
  border: 1px solid #42a5f5;
  transition: border-color 0.3s;
}

.rounded-input.q-field--focused .q-field__control {
  border-color: #1976d2;
}
</style>
