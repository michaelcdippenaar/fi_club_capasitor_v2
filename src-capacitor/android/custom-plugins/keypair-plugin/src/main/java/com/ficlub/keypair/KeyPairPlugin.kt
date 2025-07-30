package com.ficlub.keypair

import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin
//import com.getcapacitor.annotation.PluginMethod
import android.util.Log

@CapacitorPlugin(name = "KeyPair")
class KeyPairPlugin : Plugin() {

    @PluginMethod
    fun sayHello(call: PluginCall) {
        Log.i("KeyPairPlugin", "🔊 Hello from KeyPairPlugin!")
        call.resolve()
    }
}