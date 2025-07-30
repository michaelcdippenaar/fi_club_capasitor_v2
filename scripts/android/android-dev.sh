#!/bin/bash

# android-dev.sh - Syncs Capacitor, runs Quasar in dev mode with hot reload for Android
# Run from project root with: ./scripts/android/android-dev.sh

set -e  # Exit on any failure

echo "🔄 Syncing Capacitor plugins..."
cd src-capacitor
npx cap sync android || { echo "❌ Capacitor sync failed"; exit 1; }

echo "🧱 Starting Quasar dev server in Capacitor mode for Android..."
cd ..
quasar dev -m capacitor -T android || { echo "❌ Quasar dev failed"; exit 1; }