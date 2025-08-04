// import { Capacitor } from '@capacitor/core';
//
// const WifiConfigurator =
//   Capacitor.isNativePlatform && Capacitor.isNativePlatform()
//     ? window.WifiConfigurator || (Capacitor.Plugins && Capacitor.Plugins.WifiConfigurator)
//     : null;
//
// export default WifiConfigurator;


import { registerPlugin } from '@capacitor/core';

const WifiConfiguratorPlugin = registerPlugin('WifiConfiguratorPlugin');

/**
 * Suggests a Wi-Fi network using Android's ACTION_WIFI_ADD_NETWORKS API.
 *
 * @param {string} ssid - The SSID of the Wi-Fi network.
 * @param {string} identity - The user identity (used with EAP-TLS).
 * @param {string} alias - The Android Keystore alias for the client certificate.
 * @returns {Promise<void>}
 */
export async function configureWifi(ssid, identity, alias, caCertBase64) {
  try {
    await WifiConfiguratorPlugin.action_wifi_add_network({
      ssid,
      identity,
      alias,
      caCertBase64
    });
    console.info(`[WiFiConfigurator] ✅ Successfully suggested SSID "${ssid}"`);
  } catch (error) {
    console.error(`[WiFiConfigurator] ❌ Failed to suggest Wi-Fi network: ${error.message}`);
    throw error; // So caller can catch and handle this
  }
}
