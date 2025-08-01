// issueP12Certificate.js
import { generateCsrWithKey, buildP12, installP12 } from './capacitor/certificate-auth';
import { submitCsr } from './api/certificateAuthApi';

/**
 * Runs the full flow to:
 * 1. Generate a CSR and private key
 * 2. Submit to backend
 * 3. Receive cert + chain
 * 4. Build a .p12 bundle from PEM components
 * 5. Install into Android KeyChain via plugin
 *
 * Returns all data needed to configure WifiEnterpriseConfig.
 *
 * @param {string} alias - Alias to label key/cert
 * @param {string} commonName - CN for the CSR (e.g. wifiuser@domain)
 * @param {string} [email] - Optional user email for CA tracking
 * @param {string} [deviceId] - Optional device identifier
 * @param {string} [password] - Optional password to secure .p12
 * @returns {Promise<{ p12: string, privateKeyPem: string, certificatePem: string, wifiConfig: object }>}
 */
export async function issueP12Certificate({ alias, commonName, email, deviceId, password = '' }) {
  // Step 1: generate CSR and private key
  const { privateKeyPem, csrPem } = await generateCsrWithKey(alias, commonName);

  // Step 2: submit CSR to backend
  const { certificate, certificate_chain } = await submitCsr(csrPem, email, deviceId);

  console.log(certificate);

  // Step 3: build .p12 bundle
  const { p12 } = await buildP12(alias, privateKeyPem, certificate_chain, password);

  // Step 4: install into Android KeyChain
  await installP12(alias, p12, password);

  // Step 5: return wifi-related config variables
  const wifiConfig = {
    identity: commonName,
    alias,
    eap: 'TLS',
    phase2: 'NONE',
    certChain: certificate_chain,
    privateKeyPem,
    p12password: password
  };

  return {
    p12,
    privateKeyPem,
    certificatePem: certificate_chain,
    wifiConfig
  };
}
