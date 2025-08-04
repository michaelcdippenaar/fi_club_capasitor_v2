// cert-api-client.js
// import axios from 'axios';

import { api } from 'src/boot/axios'

/**
 * Submit a PEM-encoded CSR to the Django backend.
 *
 * @param {string} csr - The CSR string (PEM format). - Fed from Android GenerateP12
 * @param {string} [email] - Optional email for tracking.
 * @param {string} [deviceId] - Optional device identifier.
 * @returns {Promise<{ certificate: string, certificate_chain: string, valid_until: string }>}
 */
// export async function submitCsr(csr, email, deviceId) {
//   const res = await api.post(`/csr/submit/`, { csr: csr, email: email, device_id: deviceId });
//   const certPem = res.data?.certificate;
//
//   if (!certPem) {
//     throw new Error("No certificate returned from backend.");
//   }
//
//   return res.data;
// }

/**
 * Submits a CSR to the server and receives certificate bundle for Android Wi-Fi configuration.
 *
 * @param {string} csr - Base64 DER-encoded CSR from Android device.
 * @param {string} [email] - Optional user email.
 * @param {string} [deviceId] - Optional device identifier.
 * @returns {Promise<{
 *   certificate: string,
 *   certificate_chain: string,
 *   valid_until: string,
 *   caCertBase64: string,
 *   clientCertBase64: string,
 *   privateKeyBase64: string,
 *   p12Base64: string | null
 * }>}
 */
export async function submitCsr(csr, email, deviceId) {
  const res = await api.post("/csr/submit/", { csr, email, device_id: deviceId });
  if (!res.data?.certificate) {
    throw new Error("No certificate returned from backend.");
  }
  return res.data;
}

/**
 * Check the status of a certificate by device ID.
 *
 * @param {string} deviceId
 * @returns {Promise<{ issued: boolean, valid_until?: string, revoked?: boolean }>}
 */
export async function getCertStatus(deviceId) {
  const res = await api.get(`/certs/status/`, { params: { device_id: deviceId } });
  return res.data;
}

/**
 * Revoke a certificate by device ID.
 *
 * @param {string} deviceId
 * @param {string} [reason] - Optional reason string
 * @returns {Promise<{ status: string, revoked_at: string }>}
 */
export async function revokeCertificate(deviceId, reason = 'unspecified') {
  const res = await api.post(`/certs/revoke/`, { device_id: deviceId, reason });
  return res.data;
}

/**
 * Fetch the root CA certificate (PEM).
 *
 * @returns {Promise<{ ca_cert: string }>}
 */
export async function getCaRootCert() {
  const res = await api.get(`/ca/root/`);
  return res.data;
}
