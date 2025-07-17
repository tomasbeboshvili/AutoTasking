# 🤖 Task Extraction API

API inteligente para extracción automática de tareas usando Google Gemini AI. Perfecta para integración con n8n, Zapier y otras herramientas de automatización.

## 🌟 Características

- ✅ **Extracción inteligente de tareas** usando Google Gemini AI
- 🔗 **Lista para n8n** con endpoints específicos para webhooks
- 📧 **Procesamiento de emails** automático
- 🎯 **Múltiples formatos** (Todoist, Notion, ClickUp)
- 🔒 **Sin base de datos** - completamente stateless
- ⚡ **Ejecutable único** - solo necesitas Java y tu API key

## 🚀 Inicio Rápido

### Requisitos
- **Java 11 o superior** ([descargar aquí](https://adoptopenjdk.net/))
- **Clave API de Google Gemini** ([obtener aquí](https://aistudio.google.com/app/apikey))

### Instalación

1. **Descargar** la última release o clonar este repositorio
2. **Configurar API Key**:
   ```bash
   cp application.properties.template application.properties
   # Edita application.properties y reemplaza TU_API_KEY_AQUI con tu clave real
   ```
3. **Ejecutar**:
   ```bash
   ./start-api.sh
   ```
4. **Verificar**: http://localhost:8080/api/v1/health
   - Deberías ver: `{"success":true,"message":"API funcionando correctamente"}`

## 📡 Cómo Usar

### 🎯 Extraer tareas de texto simple
```bash
curl -X POST http://localhost:8080/api/v1/analyze-text \
  -H "Content-Type: application/json" \
  -d '{"text": "Mañana tengo que comprar leche y llamar al dentista"}'
```

### 📧 Procesar emails (ideal para n8n)
```bash
curl -X POST http://localhost:8080/api/v1/webhook/email \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Tareas pendientes",
    "content": "Revisar código del proyecto y preparar presentación",
    "sender": "jefe@empresa.com"
  }'
```

### 👁️ Vista previa rápida
```bash
curl -X POST http://localhost:8080/api/v1/analyze-preview \
  -H "Content-Type: application/json" \
  -d '{"text": "Lista: 1. Comprar pan 2. Estudiar 3. Hacer ejercicio"}'
```

## 🔧 Integración con n8n

### Configuración típica:

1. **Gmail Trigger** → captura emails nuevos
2. **HTTP Request** → envía a esta API 
3. **Todoist/Notion** → crea las tareas extraídas

### Ejemplo de nodo HTTP Request en n8n:
```json
{
  "url": "http://localhost:8080/api/v1/webhook/email",
  "method": "POST",
  "body": {
    "subject": "{{$node['Gmail Trigger'].json['subject']}}",
    "content": "{{$node['Gmail Trigger'].json['snippet']}}",
    "sender": "{{$node['Gmail Trigger'].json['from']}}"
  }
}
```

## 📋 Formato de Respuesta

Todas las respuestas siguen este formato:
```json
{
  "success": true,
  "message": "Tareas extraídas correctamente",
  "data": [
    {
      "title": "Comprar leche",
      "dueDate": "2025-07-18",
      "priority": "MEDIA",
      "completed": false,
      "category": "general"
    }
  ],
  "count": 1,
  "preview": false
}
```

## 🔐 Seguridad

- ⚠️ **IMPORTANTE**: No compartas tu clave API de Gemini
- 🌐 CORS habilitado para permitir llamadas desde herramientas externas
- 🔒 Para producción, considera añadir autenticación

## 🐛 Troubleshooting

### La API no inicia
- Verifica que Java 11+ esté instalado: `java -version`
- Asegúrate de que el puerto 8080 esté libre
- Revisa los logs en `app.log`

### Error de API Key
- Verifica que la clave esté configurada en `application.properties`
- Asegúrate de que la clave sea válida en [Google AI Studio](https://aistudio.google.com/)

### No extrae tareas correctamente
- Usa texto claro y estructurado
- Prueba con el endpoint `/analyze-preview` primero
- Revisa que el texto tenga al menos 10 caracteres

## 📄 Licencia

Este proyecto está licenciado bajo la **BSD 3-Clause License** - ver el archivo [LICENSE](LICENSE) para más detalles.

### ⚠️ Términos importantes:
- ✅ **Puedes usar, modificar y distribuir** este código
- ✅ **Puedes usarlo comercialmente**
- ⚠️ **DEBES incluir el aviso de copyright** en cualquier redistribución
- ⚠️ **NO puedes usar mi nombre** para promocionar derivados sin permiso

## 👨‍💻 Autor

**Tomas Beboshvili** - Desarrollo inicial

## 🙏 Reconocimientos

- Google Gemini AI por la capacidad de procesamiento de lenguaje natural
- Spring Boot por el framework web
- La comunidad de n8n por la inspiración de automatización

---

**Desarrollado para automatización inteligente con n8n** 🚀
