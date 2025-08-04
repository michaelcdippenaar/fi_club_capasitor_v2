package co.mcdippenaar.testapp;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.ficlub.keypair.KeyPairPlugin; // 👈 import your plugin class
import com.ficlub.certificate_auth.CertificateAuthPlugin;
import com.ficlub.network.WifiConfiguratorPlugin;

public class MainActivity extends BridgeActivity {
 @Override
 public void onCreate(Bundle savedInstanceState) {
   registerPlugin(KeyPairPlugin.class); // 👈 register your plugin
   registerPlugin(CertificateAuthPlugin.class);
   registerPlugin(WifiConfiguratorPlugin.class);
   super.onCreate(savedInstanceState);
 }
}