#!/bin/bash

# Task Extraction API - Startup script
# Usage: ./start-api.sh

echo "🚀 Starting Task Extraction API..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 11 or higher."
    echo "💡 Download from: https://adoptopenjdk.net/"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ Java 11 or higher required. Current version: $JAVA_VERSION"
    exit 1
fi

# Check if JAR exists
JAR_FILE="releases/task-extraction-api.jar"
if [ ! -f "$JAR_FILE" ]; then
    # Try alternative location
    JAR_FILE="target/tasks-0.0.1-SNAPSHOT.jar"
    if [ ! -f "$JAR_FILE" ]; then
        echo "❌ JAR file not found. Please build the project first:"
        echo "💡 Run: ./mvnw clean package -DskipTests"
        exit 1
    fi
fi

# Check configuration file
CONFIG_FILE="application.properties"
if [ ! -f "$CONFIG_FILE" ]; then
    # Try source location
    CONFIG_FILE="src/main/resources/application.properties"
    if [ ! -f "$CONFIG_FILE" ]; then
        echo "❌ Configuration file not found: application.properties"
        echo "💡 Copy application.properties.template and configure your API key"
        exit 1
    fi
fi

# Check API key configuration
if grep -q "YOUR_API_KEY_HERE" "$CONFIG_FILE"; then
    echo "⚠️  ATTENTION: You need to configure your Gemini API key"
    echo "📝 Configuration steps:"
    echo "   1. Get your key at: https://aistudio.google.com/app/apikey"
    echo "   2. Edit $CONFIG_FILE"
    echo "   3. Replace 'YOUR_API_KEY_HERE' with your real key"
    echo "   4. ⚠️  NEVER share your API key with anyone"
    exit 1
fi

# Check if API key is empty
if grep -q "^gemini.api.key=$" "$CONFIG_FILE"; then
    echo "⚠️  API key is empty in $CONFIG_FILE"
    echo "🔑 Configure your Gemini key before continuing"
    exit 1
fi

# Check if port is available
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  Port 8080 is already in use"
    echo "🔍 To see which process is using it: lsof -i :8080"
    echo "🛑 To stop previous API: killall java"
    exit 1
fi

# Start the application
echo "✅ Everything configured correctly"
echo "🌐 API will be available at: http://localhost:8080"
echo "💚 Health check: http://localhost:8080/api/v1/health"
echo "📖 Documentation: README.md"
echo "🛑 To stop: Ctrl+C or killall java"
echo ""
echo "🚀 Starting..."

if [ -f "application.properties" ]; then
    java -jar "$JAR_FILE" --spring.config.location=file:./application.properties
else
    java -jar "$JAR_FILE"
fi
