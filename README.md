# 🤖 Task Extraction API

Intelligent API for automated task extraction using Google Gemini AI. Perfect for integration with n8n, Zapier, and other automation tools.

## 🌟 Features

- ✅ **Smart task extraction** using Google Gemini AI
- 🔗 **n8n ready** with specific webhook endpoints
- 📧 **Automatic email processing** 
- 🎯 **Multiple formats** (Todoist, Notion, ClickUp)
- 🔒 **Database-free** - completely stateless
- ⚡ **Single executable** - just need Java and your API key

## 🚀 Quick Start

### Requirements
- **Java 11 or higher** ([download here](https://adoptopenjdk.net/))
- **Google Gemini API Key** ([get it here](https://aistudio.google.com/app/apikey))

### Installation & Setup

#### Option 1: Use Pre-compiled JAR (Recommended)
1. **Download** the latest JAR from [releases/](releases/)
2. **Configure API Key**:
   ```bash
   cp application.properties.template application.properties
   # Edit application.properties and replace YOUR_API_KEY_HERE with your real key
   ```
3. **Run**:
   ```bash
   ./start-api.sh
   ```

#### Option 2: Build from Source
1. **Clone** this repository
2. **Configure API Key** in `src/main/resources/application.properties`
3. **Build & Run**:
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/tasks-0.0.1-SNAPSHOT.jar
   ```

### Verification
Visit: http://localhost:8080/api/v1/health
You should see: `{"success":true,"message":"API working correctly"}`

## 📡 API Endpoints

### 🎯 Extract tasks from simple text
```bash
curl -X POST http://localhost:8080/api/v1/analyze-text \
  -H "Content-Type: application/json" \
  -d '{"text": "Tomorrow I need to buy milk and call the dentist"}'
```

### 📧 Process emails (ideal for n8n)
```bash
curl -X POST http://localhost:8080/api/v1/webhook/email \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Pending tasks",
    "content": "Review project code and prepare presentation",
    "sender": "boss@company.com"
  }'
```

### 👁️ Quick preview
```bash
curl -X POST http://localhost:8080/api/v1/analyze-preview \
  -H "Content-Type: application/json" \
  -d '{"text": "List: 1. Buy bread 2. Study 3. Exercise"}'
```

### 💚 Health check
```bash
curl http://localhost:8080/api/v1/health
```

## 🔧 n8n Integration

### Ready-to-Use Workflows 🚀

We provide **pre-built n8n workflows** in the `n8n-workflows/` directory:

- **📧 Gmail to Todoist** - Automatically extract tasks from emails and create them in Todoist
- **📝 Simple Text-to-Tasks** - Basic workflow for testing the API
- **🔄 More workflows coming soon** - Notion, ClickUp, Slack integrations

**Quick Start:**
1. Download workflow JSON from `n8n-workflows/`
2. Import into your n8n instance
3. Configure credentials (Gmail, Todoist, etc.)
4. Update API URL to your instance
5. Activate and enjoy automated task creation!

📚 **Full setup guide:** See `n8n-workflows/README.md`

### Manual Integration

### Typical workflow:
1. **Gmail Trigger** → captures new emails
2. **HTTP Request** → sends to this API 
3. **Todoist/Notion** → creates extracted tasks

### Example HTTP Request node in n8n:
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

## 📋 Response Format

All responses follow this format:
```json
{
  "success": true,
  "message": "Tasks extracted successfully",
  "data": [
    {
      "title": "Buy milk",
      "dueDate": "2025-07-18",
      "priority": "MEDIUM",
      "completed": false,
      "category": "general"
    }
  ],
  "count": 1,
  "preview": false
}
```

## 🛠️ Development

### Build from source:
```bash
./mvnw clean package -DskipTests
```

### Run in development mode:
```bash
./mvnw spring-boot:run
```

### Project structure:
```
src/
├── main/java/tom/example/tasks/
│   ├── controller/          # REST API endpoints
│   ├── service/            # Business logic & Gemini AI integration
│   ├── model/              # Data models
│   └── dto/                # Data transfer objects
└── main/resources/         # Configuration files
```

## 🔐 Security

- ⚠️ **IMPORTANT**: Never share your Gemini API key
- 🌐 CORS enabled for external tool integration
- 🔒 For production, consider adding authentication
- 🛡️ API keys are never included in this repository

## 🐛 Troubleshooting

### API won't start
- Verify Java 11+ is installed: `java -version`
- Ensure port 8080 is available
- Check logs in terminal output

### API Key errors
- Verify key is configured in `application.properties`
- Ensure key is valid at [Google AI Studio](https://aistudio.google.com/)

### Poor task extraction
- Use clear, structured text
- Try `/analyze-preview` endpoint first
- Ensure text has at least 10 characters

## 📄 License

This project is licensed under the **BSD 3-Clause License** - see the [LICENSE](LICENSE) file for details.

### ⚠️ Important terms:
- ✅ **You can use, modify and distribute** this code
- ✅ **Commercial use is allowed**
- ⚠️ **You MUST include the copyright notice** in any redistribution
- ⚠️ **You CANNOT use my name** to promote derivatives without permission

## 👨‍💻 Author

**Tomas Beboshvili** - Initial development

## 🙏 Acknowledgments

- Google Gemini AI for natural language processing capabilities
- Spring Boot for the web framework
- The n8n community for automation inspiration

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

**Built for intelligent automation with n8n** 🚀
