# ios-dev.sh - Syncs Capacitor, builds Quasar for dev, prepares iOS, and opens Xcode
# ./scripts/ios/ios-dev.sh

set -e  # Exit on first failure

echo "🔄 Syncing Capacitor plugins..."
cd src-capacitor
npx cap sync ios || { echo "❌ Capacitor sync failed"; exit 1; }

echo "🧱 Building Quasar project in dev mode (capacitor)..."
cd ..
quasar dev -m capacitor -T ios || { echo "❌ Quasar dev failed"; exit 1; }

echo "📦 Copying web assets to iOS project..."
cd src-capacitor
npx cap copy ios || { echo "❌ Capacitor copy failed"; exit 1; }

echo "🚀 Opening iOS project in Xcode..."
npx cap open ios || { echo "❌ Failed to open Xcode"; exit 1; }