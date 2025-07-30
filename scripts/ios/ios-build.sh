# ios.sh - Syncs Capacitor, builds Quasar, prepares native iOS, and opens Xcode

echo "🔄 Syncing Capacitor plugins..."
cd src-capacitor
npx cap sync ios || { echo "❌ Capacitor sync failed"; exit 1; }

# run quasar dev or quasar build with capacitor mode

echo "🧱 Building Quasar project for production..."
cd ..
quasar build || { echo "❌ Quasar build failed"; exit 1; }

echo "📦 Copying web assets to iOS project..."

cd src-capacitor
npx cap copy ios || { echo "❌ Capacitor copy failed"; exit 1; }

echo "🚀 Opening iOS project in Xcode..."
npx cap open ios || { echo "❌ Failed to open Xcode"; exit 1; }