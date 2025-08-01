package co.mcdippenaar.testapp;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.ficlub.keypair.KeyPairPlugin; // 👈 import your plugin class

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    registerPlugin(KeyPairPlugin.class); // 👈 register your plugin
    super.onCreate(savedInstanceState);
  }
}