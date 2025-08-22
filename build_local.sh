#!/bin/bash

# Simple local build script for testing
set -e

echo "🔧 Starting Termux CLI Manager build..."

# Clean previous build
echo "🧹 Cleaning previous build..."
./gradlew clean || gradle clean

# Run lint check
echo "🔍 Running lint check..."
./gradlew lint || gradle lint

# Build debug APK
echo "🏗️ Building debug APK..."
./gradlew assembleDebug || gradle assembleDebug

# Check if APK was created
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ Build successful! APK created at: app/build/outputs/apk/debug/app-debug.apk"
    ls -la app/build/outputs/apk/debug/app-debug.apk
else
    echo "❌ Build failed - APK not found"
    exit 1
fi

echo "🎉 Build completed successfully!"