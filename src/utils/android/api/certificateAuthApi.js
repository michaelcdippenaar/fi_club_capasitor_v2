// cert-api-client.js
import axios from 'axios';

const API_BASE = '/api';

/**
 * Submit a PEM-encoded CSR to the Django backend.
 *
 * @param {string} csr - The CSR string (PEM format).
 * @param {string} [email] - Optional email for tracking.
 * @param {string} [deviceId] - Optional device identifier.
 * @returns {Promise<{ certificate: string, certificate_chain: string, valid_until: string }>}
 */
export async function submitCsr(csr, email, deviceId) {
  const res = await axios.post(`${API_BASE}/csr/submit/`, { csr, email, device_id: deviceId });
  return res.data;
}

/**
 * Check the status of a certificate by device ID.
 *
 * @param {string} deviceId
 * @returns {Promise<{ issued: boolean, valid_until?: string, revoked?: boolean }>}
 */
export async function getCertStatus(deviceId) {
  const res = await axios.get(`${API_BASE}/certs/status/`, { params: { device_id: deviceId } });
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
  const res = await axios.post(`${API_BASE}/certs/revoke/`, { device_id: deviceId, reason });
  return res.data;
}

/**
 * Fetch the root CA certificate (PEM).
 *
 * @returns {Promise<{ ca_cert: string }>}
 */
export async function getCaRootCert() {
  const res = await axios.get(`${API_BASE}/ca/root/`);
  return res.data;
}
