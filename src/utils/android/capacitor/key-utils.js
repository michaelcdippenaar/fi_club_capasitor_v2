// import { registerPlugin } from '@capacitor/core';
//
// export const KeyPairPlugin = registerPlugin('KeyUtils');
//
// const checkPluginAvailability = (method) => {
//   const plugin = window.Capacitor?.Plugins?.KeyUtils;
//   if (!plugin?.[method]) {
//     throw new Error(`❌ KeyUtils plugin not available or ${method} not registered.`);
//   }
//   return plugin;
// };
//
// export const KeyUtils = {
//   async generateKeyPair(alias = 'ficlub-key') {
//     const plugin = checkPluginAvailability('generateKeyPair');
//     await plugin.generateKeyPair({ alias });
//     return true;
//   },
//
//   async generateCSR(alias = 'ficlub-key', subject = 'CN=wifiuser,O=Ficlub,C=ZA') {
//     const plugin = checkPluginAvailability('generateCSR');
//     const { csr } = await plugin.generateCSR({ alias, subject });
//     return csr;
//   },
//
//   async installCertificate(alias = 'ficlub-key', certificate) {
//     const plugin = checkPluginAvailability('installCertificate');
//     await plugin.installCertificate({ alias, certificate });
//     return true;
//   },
//
//    async getCertificateDetails(alias = 'ficlub-key') {
//     const plugin = checkPluginAvailability('getCertificateDetails');
//     const result = await plugin.getCertificateDetails({ alias });
//     console.log('Certificate details:', JSON.stringify(result, null, 2));
//      console.log('PEM (Base64):', result.pem);
//     return result;
//   },
//      async installSystemCertificate(alias, certificate) {
//         const plugin = checkPluginAvailability('installSystemCertificate');
//         await plugin.installSystemCertificate({ alias }, { certificate });
//         return true;
//   }
//
// };

// import { Plugins } from '@capacitor/core'
// const { KeyPair: KeyUtils } = Plugins

import { registerPlugin } from '@capacitor/core';
export const KeyUtils = registerPlugin('KeyPair');
/**
 * Generates a key pair securely in the Android Keystore.
 *
 * @param {string} alias - Alias under which the key pair will be stored.
 * @returns {Promise<void>}
 */
export async function generateKeyPair(alias) {
  if (!alias) {
    throw new Error("Alias is required to generate a key pair.")
  }

  await KeyUtils.generateKeyPair({
    alias
  })
}



/**
 * Creates a CSR using the key stored under the given alias.
 *
 * @param {string} alias - Alias of the existing key pair in the Keystore.
 * @param {string} commonName - Common Name (CN) to include in the CSR (e.g., "wifiuser@domain.com").
 * @returns {Promise<string>} - Base64 DER-encoded CSR string.
 */
export async function exportPublicKeyToCSR(alias, commonName) {
  if (!alias || !commonName) {
    throw new Error("Both alias and commonName are required to export CSR.")
  }

  const result = await KeyUtils.generateCsr({
    alias,
    commonName
  })

  if (!result?.csr) {
    throw new Error("Failed to generate CSR.")
  }

  return result.csr
}

export default { generateKeyPair, exportPublicKeyToCSR };

