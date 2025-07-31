import { registerPlugin } from '@capacitor/core';

const WifiManager = registerPlugin('WifiManager');

const installCertificate = async (alias, certificate) => {
  if (!alias || !certificate) throw new Error("Missing alias or certificate");
  await WifiManager.forWifiInstallCertificates({ alias, certificate });
  console.log("✅ Certificate installed");
};

const connectToWifi = async (ssid, alias, identity = 'wifiuser') => {
  if (!ssid || !alias) throw new Error("Missing SSID or alias");
  await WifiManager.setupWifi({ ssid, alias, identity });
  console.log("✅ Wi-Fi configured and connected");
};

export default {
  installCertificate,
  connectToWifi,
};
