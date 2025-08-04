import { registerPlugin } from '@capacitor/core';
// export const WifiConfiguratorPlugin = registerPlugin('WifiConfiguratorPlugin');
export const KeyPair  = registerPlugin('KeyPair');
export const CertificateAuth = registerPlugin('CertificateAuthPlugin');

// export async function connectToWifiWithCertificate({ ssid, alias, identity }) {
//   if (!ssid || !alias || !identity) {
//     throw new Error("ssid, alias, and identity are required");
//   }
//   // const result = await WifiConfiguratorPlugin.connectWithCertificate({ ssid, alias, identity });
//   // console.log('connectToWifiWithCertificate', result);
// }

// export async function suggestWifiProfile({ ssid, alias, identity }) {
//   if (!ssid || !alias || !identity) {
//     throw new Error("ssid, alias, and identity are required");
//   }
//   const result = await WifiConfiguratorPlugin.createWifiProfile({ ssid, alias, identity });
//   console.log('createWifiProfile', result);
// }

// network.capacitor.js

// import { Plugins } from '@capacitor/core';
// const { KeyPair } = Plugins;

/**
 * Suggest a Wi-Fi network using client certificate-based authentication.
 *
 * @param {Object} opts
 * @param {string} opts.ssid - Wi-Fi SSID
 * @param {string} opts.caCertBase64 - Base64-encoded CA certificate
 * @param {string} opts.clientCertBase64 - Base64-encoded client certificate
 * @param {string} opts.privateKeyBase64 - Base64-encoded private key
 * @param {string} opts.identity - Identity string (e.g. user@domain)
 */
export async function addNetworkSuggestion({ ssid, caCertBase64, clientCertBase64, privateKeyBase64, identity }) {
  if (!ssid || !caCertBase64 || !clientCertBase64 || !privateKeyBase64) {
    throw new Error("Missing required parameters for network suggestion.");
  }

  return KeyPair.addNetworkSuggestion({
    ssid,
    caCertBase64,
    clientCertBase64,
    privateKeyBase64,
    identity
  });
}

// network.capacitor.js

/**
 * Installs certificate in Android KeyChain using the Capacitor plugin.
 *
 * @param {Object} params
 * @param {string} params.alias - The alias for the installed key
 * @param {string} params.clientCertBase64 - PEM string of certificate
 * @param {string} params.caCertBase64 - Base64 string of .p12 bundle (optional)
 */
export async function storeCertificateAndKey({ alias, clientCertBase64, caCertBase64 }) {

  if (!alias || !clientCertBase64 || !caCertBase64) {
    throw new Error("Alias and client and ca certificate are required to store the certificate.");
  }
  const result =  await CertificateAuth.installClientAndCaCertificate({alias, clientCertBase64, caCertBase64});

  console.log("network.capasitor.js storeCertificateAndKey result:", result);
  return result;

}

