import { Capacitor } from '@capacitor/core'
import { Filesystem, Directory } from '@capacitor/filesystem';
import { Share } from '@capacitor/share';


// ✅ Detect live dev mode (Capacitor container but running from localhost)
export function isLiveDevMode() {
  return Capacitor.isNativePlatform() && !!window.Capacitor?.config?.server?.url
}

// ✅ Detect real native bundled build (Capacitor native + NO dev server)
export function isRealNativeBuild() {
  return Capacitor.isNativePlatform() && !window.Capacitor?.config?.server?.url
}

// ✅ Universal saveToken
export async function saveToken(token) {
  console.log(Capacitor.getPlatform())
  console.log(isLiveDevMode())
  if (! isLiveDevMode()) {
    console.log('➡️ Using localStorage (web or live dev mode)')
    localStorage.setItem('token', token)
  } else if (isLiveDevMode()) {
    console.log('➡️ Using Capacitor Preferences (real native build)')
    const { Preferences } = await import('@capacitor/preferences')
    await Preferences.set({ key: 'token', value: token })
  } else {
    console.warn('⚠️ Unknown platform → fallback to localStorage')
    localStorage.setItem('token', token)
  }
}

// ✅ Universal getToken
export async function getToken() {
  if (! isLiveDevMode()) {
    console.log('➡️ Reading token from localStorage')
    return localStorage.getItem('token')
  } else if (isRealNativeBuild()) {
    console.log('➡️ Reading token from Capacitor Preferences')
    const { Preferences } = await import('@capacitor/preferences')
    const { value } = await Preferences.get({ key: 'token' })
    return value
  } else {
    console.warn('⚠️ Unknown platform → fallback to localStorage')
    return localStorage.getItem('token')
  }
}

// ✅ Universal removeToken
export async function removeToken() {
  if (Capacitor.getPlatform() === 'web' || isLiveDevMode()) {
    console.log('➡️ Removing token from localStorage')
    localStorage.removeItem('token')
  } else if (isRealNativeBuild()) {
    console.log('➡️ Removing token from Capacitor Preferences')
    const { Preferences } = await import('@capacitor/preferences')
    await Preferences.remove({ key: 'token' })
  } else {
    console.warn('⚠️ Unknown platform → fallback to localStorage')
    localStorage.removeItem('token')
  }
}



export async function openMobileConfig(blob, fileName = 'profile.mobileconfig') {
  // Convert blob → base64
  const base64 = await blobToBase64(blob);

  // Save it as a temp file
  const result = await Filesystem.writeFile({
    path: fileName,
    data: base64,
    directory: Directory.Cache
  });

  const fileUri = result.uri;
  console.log('[MobileConfig] Saved profile at:', fileUri);

  // Open it with iOS share sheet
  await Share.share({
    title: 'Install Configuration Profile',
    url: fileUri,
    dialogTitle: 'Install Configuration Profile'
  });

  // Optional: If you want to directly open instead of share
  // import { App } from '@capacitor/app';
  // await App.openUrl({ url: fileUri });
}

/**
 * Helper: converts a Blob to base64 string
 */
function blobToBase64(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onloadend = () => {
      const base64data = reader.result.split(',')[1]; // remove "data:*/*;base64,"
      resolve(base64data);
    };
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
}
