// src/router/routes.js
const routes = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', redirect: '/login' },
      { path: 'login', component: () => import('pages/common/LoginPage.vue') },
      { path: 'enroll', component: () => import('pages/common/EnrollApp.vue') },
      { path: 'certificate_ios', component: () => import('pages/ios/GetCertificateIOS.vue') },
      { path: 'certificate_android', component: () => import('pages/android/AndroidGeneratePem.vue') },
      { path: 'p12_android', component: () => import('pages/android/AndroidGenerateP12.vue') }

    ]
  },

  // Always leave this as last one
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue')
  }
]

export default routes
