import { registerPlugin } from '@capacitor/core';

const WifiManager = registerPlugin('KeyPair');



const installCertificate = async (alias, certificate) => {
  if (!alias || !certificate) throw new Error("Missing alias or certificate Certs");
  await WifiManager.forWifiInstallCertificates({ alias, certificate });
  console.log("[KeyPairPlugin]","✅ Certificate installed from Wifi");
};

const connectToWifi = async (ssid, alias, identity = 'wifiuser') => {
  console.log("[KeyPairPlugin]",'connectToWifi',"ssid, alias, identity", ssid, alias, identity)
  if (!ssid || !alias) throw new Error("Missing SSID or alias Error");
  await WifiManager.setupWifi({ ssid, alias, identity });
  console.log("[KeyPairPlugin]","✅ Wi-Fi configured and connected");
};

export default {
  installCertificate,
  connectToWifi,
};
