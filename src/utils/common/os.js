import { Capacitor } from '@capacitor/core'

export function getPlatform() {
  return Capacitor.getPlatform() // returns 'old-android', 'ios', or 'web'
}
