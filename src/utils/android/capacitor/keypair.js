import { registerPlugin } from '@capacitor/core';

const KeyPairGen = registerPlugin('KeyPair');




const KeyPair = {
  async generateCSR(alias = 'ficlub-key', subject = 'CN=wifiuser,O=Ficlub,C=ZA') {
    const plugin = window.Capacitor?.Plugins?.KeyPair;
    if (!plugin?.generateCSR) {
      throw new Error('❌ KeyPair plugin not available or not registered.');
    }

    const { csr } = await plugin.generateCSR({ alias, subject });
    return csr;
  }
};

export default { KeyPair, KeyPairGen }
