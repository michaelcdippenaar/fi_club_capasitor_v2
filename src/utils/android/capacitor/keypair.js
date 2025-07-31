import { registerPlugin } from '@capacitor/core';

export const KeyPairPlugin = registerPlugin('KeyPair');

const checkPluginAvailability = (method) => {
  const plugin = window.Capacitor?.Plugins?.KeyPair;
  if (!plugin?.[method]) {
    throw new Error(`❌ KeyPair plugin not available or ${method} not registered.`);
  }
  return plugin;
};

export const KeyPair = {
  async generateKeyPair(alias = 'ficlub-key') {
    const plugin = checkPluginAvailability('generateKeyPair');
    await plugin.generateKeyPair({ alias });
    return true;
  },

  async generateCSR(alias = 'ficlub-key', subject = 'CN=wifiuser,O=Ficlub,C=ZA') {
    const plugin = checkPluginAvailability('generateCSR');
    const { csr } = await plugin.generateCSR({ alias, subject });
    return csr;
  },

  async installCertificate(alias = 'ficlub-key', certificate) {
    const plugin = checkPluginAvailability('installCertificate');
    await plugin.installCertificate({ alias, certificate });
    return true;
  },

   async getCertificateDetails(alias = 'ficlub-key') {
    const plugin = checkPluginAvailability('getCertificateDetails');
    const result = await plugin.getCertificateDetails({ alias });
    console.log('Certificate details:', JSON.stringify(result, null, 2));
     console.log('PEM (Base64):', result.pem);
    return result;
  },
     async installSystemCertificate(alias, certificate) {
        const plugin = checkPluginAvailability('installSystemCertificate');
        await plugin.installSystemCertificate({ alias }, { certificate });
        return true;
  }

};

export default { KeyPair, KeyPairPlugin };
