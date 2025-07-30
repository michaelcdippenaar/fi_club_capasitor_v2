<template>
  <q-page class="q-pa-md">
    <q-card>
      <q-card-section>
        <div class="text-h6">Certificate Enrollment - Updated</div>
      </q-card-section>

      <q-card-section>
        <p v-if="loading">Preparing configuration profile...</p>
        <p v-if="error" class="text-negative">{{ error }}</p>
      </q-card-section>

      <q-card-actions>
        <q-btn
          color="primary"
          label="Install Configuration Profile"
          @click="installProfile"
          :disable="loading"
        />
      </q-card-actions>
    </q-card>
  </q-page>
</template>

<script>
import { getToken } from "src/utils/ios/storage";
import { api } from "src/boot/axios";
import { Capacitor } from "@capacitor/core";
import { Browser } from "@capacitor/browser";

export default {
  name: "GetCertificate",
  data() {
    return {
      loading: false,
      error: null,
    };
  },
  methods: {
    async installProfile() {
      console.log("🚀 Starting Safari-based profile install flow...");

      try {
        this.loading = true;
        this.error = null;

        // ✅ Get the JWT token from storage
        const token = await getToken();
        console.log("DEBUG: Retrieved token →", token);

        if (!token) {
          this.error = "No token found, please login first!";
          this.loading = false;
          return;
        }

        // ✅ API endpoint with `Token` prefix via query param
        // Your backend must accept ?auth_token= for Safari
        const profileUrl = `${api.defaults.baseURL}/serve-mobileconfig/?auth_token=${token}`;

        console.log("🌐 Opening Safari with URL:", profileUrl);

        if (Capacitor.getPlatform() === "ios") {
          // ✅ On iOS, open in Safari so it triggers the mobileconfig install
          await Browser.open({ url: profileUrl });
        } else {
          // ✅ On Android/Web, just open in a new tab
          window.open(profileUrl, "_blank");
        }

        console.log("✅ Safari opened successfully! After installing, user returns manually.");
      } catch (err) {
        console.error("❌ Error opening profile URL:", err);
        this.error = `Error installing profile: ${err.message || JSON.stringify(err)}`;
      } finally {
        this.loading = false;
      }
    },

    /*
    🔴 Legacy flow (downloading + Share Sheet) - still here for reference, but not used anymore.

    async installProfile() {
      console.log("Old Process");
      try {
        this.loading = true;
        this.error = null;

        const token = await getToken();
        if (!token) {
          this.error = "No token found, please login first!";
          this.loading = false;
          return;
        }

        console.log("✅ Using token:", token);

        const response = await api.get("/serve-mobileconfig/", {
          headers: { Authorization: `Token ${token}` },
          responseType: "arraybuffer",
        });

        console.log("✅ Got .mobileconfig file from API");

        const base64Data = btoa(
          new Uint8Array(response.data).reduce(
            (data, byte) => data + String.fromCharCode(byte),
            ""
          )
        );

        const fileName = "profile.mobileconfig";

        const result = await Filesystem.writeFile({
          path: fileName,
          data: base64Data,
          directory: Directory.Cache,
        });

        console.log("📂 File saved:", result.uri);

        let fileUrl = result.uri;
        if (Capacitor.getPlatform() === "ios") {
          fileUrl = result.uri.replace(
            "capacitor://localhost/_capacitor_file_",
            "file://"
          );
        }

        console.log("📌 Final file URL:", fileUrl);

        await Share.share({
          title: "Install Configuration Profile",
          text: "Tap to install this profile in iOS Settings",
          url: fileUrl,
          dialogTitle: "Install Profile",
        });

        console.log("✅ Share Sheet launched successfully!");
      } catch (err) {
        console.error("❌ Error installing profile:", err);
        this.error = `Error installing profile: ${
          err.message || JSON.stringify(err)
        }`;
      } finally {
        this.loading = false;
      }
    },
    */
  },
};
</script>
