#!/bin/bash

# Task Extraction API - Script de inicio
# Uso: ./start-api.sh

echo "🚀 Iniciando Task Extraction API..."

# Verificar que Java está instalado
if ! command -v java &> /dev/null; then
    echo "❌ Java no está instalado. Por favor instala Java 11 o superior."
    echo "💡 Descarga desde: https://adoptopenjdk.net/"
    exit 1
fi

# Verificar versión de Java
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ Se requiere Java 11 o superior. Versión actual: $JAVA_VERSION"
    exit 1
fi

# Verificar que existe el JAR
JAR_FILE="task-extraction-api.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ No se encontró el archivo JAR: $JAR_FILE"
    exit 1
fi

# Verificar configuración
CONFIG_FILE="application.properties"
if [ ! -f "$CONFIG_FILE" ]; then
    echo "❌ No se encontró el archivo de configuración: $CONFIG_FILE"
    echo "💡 Copia application.properties.template y configura tu API key"
    exit 1
fi

# Verificar API key
if grep -q "TU_API_KEY_AQUI" "$CONFIG_FILE"; then
    echo "⚠️  ATENCIÓN: Necesitas configurar tu API key de Gemini"
    echo "📝 Pasos para configurar:"
    echo "   1. Obtén tu clave en: https://aistudio.google.com/app/apikey"
    echo "   2. Edita $CONFIG_FILE"
    echo "   3. Reemplaza 'TU_API_KEY_AQUI' con tu clave real"
    echo "   4. ⚠️  NUNCA compartas tu clave API con nadie"
    exit 1
fi

# Verificar que la API key no esté vacía
if grep -q "^gemini.api.key=$" "$CONFIG_FILE"; then
    echo "⚠️  La API key está vacía en $CONFIG_FILE"
    echo "🔑 Configura tu clave de Gemini antes de continuar"
    exit 1
fi

# Verificar puerto disponible
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  El puerto 8080 ya está en uso"
    echo "🔍 Para ver qué proceso lo usa: lsof -i :8080"
    echo "🛑 Para detener la API anterior: killall java"
    exit 1
fi

# Iniciar la aplicación
echo "✅ Todo configurado correctamente"
echo "🌐 La API estará disponible en: http://localhost:8080"
echo "💚 Health check: http://localhost:8080/api/v1/health"
echo "📖 Documentación: README.md"
echo "🛑 Para detener: Ctrl+C o killall java"
echo ""
echo "🚀 Iniciando..."

java -jar "$JAR_FILE" --spring.config.location=file:./$CONFIG_FILE
