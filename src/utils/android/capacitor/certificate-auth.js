// keypair-helpers.js

/**
 * Sends alias and commonName to the native plugin to generate a CSR and private key.
 *
 * @param {string} alias - Alias to name the key pair.
 * @param {string} commonName - Common Name for the CSR subject (e.g. user@domain.com).
 * @returns {Promise<{ privateKeyPem: string, csrPem: string }>} PEM-encoded CSR and private key.
 */
export async function generateCsrWithKey(alias, commonName) {
  const plugin = window.Capacitor?.Plugins?.CertificateAuthPlugin

  if (!plugin) {
    throw new Error('[CertificateAuthPlugin] function generateCsrWithKey - CertificateAuthPlugin not available');
  }

  const result = await plugin.generateCsrWithExportableKey({ alias, commonName });

  return result;
}

/**
 * Takes a PEM-encoded private key and certificate chain and builds a Base64-encoded .p12 bundle.
 *
 * @param {string} alias - Alias name to assign to the key entry.
 * @param {string} privateKey - PEM-formatted private key.
 * @param {string} certificate - PEM-formatted certificate chain.
 * @param {string} password - Optional password to protect the .p12 file.
 * @returns {Promise<{ p12: string }>} Base64-encoded PKCS#12 file content.
 */
export async function buildP12(alias, privateKey, certificate, password = '') {
  const plugin = window.Capacitor?.Plugins?.CertificateAuthPlugin
  if (!plugin) {
    throw new Error('[CertificateAuthPlugin] function buildP12 - CertificateAuthPlugin not available');
  }

  const result = await plugin.buildP12Bundle({ alias: alias,
    privateKeyPem: privateKey,
    certificatePem: certificate,
    password: password });
  return result;
}

/**
 * Installs a previously generated .p12 file (Base64 format) into Android KeyChain.
 * Launches the OS install dialog.
 *
 * @param {string} alias - Name under which to install the certificate.
 * @param {string} p12 - Base64-encoded .p12 file.
 * @param {string} password - Password that protects the .p12 bundle.
 * @returns {Promise<void>} Resolves when the install intent is triggered.
 */
export async function installP12(alias, p12, password = '') {
  const plugin = window.Capacitor?.Plugins?.CertificateAuthPlugin

  if (!plugin) {
    throw new Error('[CertificateAuthPlugin] function installP12 - CertificateAuthPlugin not available');
  }

  await plugin.installP12Certificate({ alias, p12, password });
}
