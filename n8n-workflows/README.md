# 🔄 n8n Workflow Examples

This directory contains pre-built n8n workflows that demonstrate how to integrate the Task Extraction API with popular productivity tools.

## 📧 Gmail to Todoist Workflow

**File:** `gmail-to-todoist-example.json`

### What it does:
1. **Daily Schedule** - Triggers every day at 8 AM
2. **Gmail Integration** - Fetches recent emails from your Gmail inbox
3. **Text Combination** - Combines email content for AI processing
4. **AI Task Extraction** - Sends text to our API for intelligent task extraction
5. **Task Formatting** - Converts AI response to Todoist-compatible format
6. **Todoist Creation** - Automatically creates tasks in your Todoist project

### Setup Instructions:

#### 1. Prerequisites:
- ✅ Task Extraction API running (see main README)
- ✅ n8n instance (cloud or self-hosted)
- ✅ Gmail account with API access
- ✅ Todoist account with API access

#### 2. Import Workflow:
1. In n8n, go to **Workflows** → **Import from File**
2. Upload `gmail-to-todoist-example.json`
3. Click **Import**

#### 3. Configure Credentials:

**Gmail OAuth2:**
1. Click on "Get Gmail Messages" node
2. Create new credential: **Gmail OAuth2**
3. Follow Google OAuth setup process
4. Replace `YOUR_GMAIL_CREDENTIAL_ID` with your credential ID

**Todoist OAuth2:**
1. Click on "Create Todoist Tasks" node  
2. Create new credential: **Todoist OAuth2**
3. Get your Todoist API token from [Todoist App Console](https://todoist.com/app_console)
4. Replace `YOUR_TODOIST_CREDENTIAL_ID` with your credential ID
5. Replace `YOUR_PROJECT_ID` with your target project ID

#### 4. Update API URL:
1. Click on "AI Task Extraction" node
2. Update URL to your API endpoint:
   - Local: `http://localhost:8080/api/v1/analyze-text`
   - Remote: `https://your-domain.com/api/v1/analyze-text`

#### 5. Test & Activate:
1. Click **Execute Workflow** to test
2. Check that tasks are created in Todoist
3. Click **Active** to enable daily automation

### Customization Options:

**Schedule:**
- Change trigger time in "Daily Schedule" node
- Add additional triggers (webhook, manual, etc.)

**Email Filtering:**
- Modify Gmail query in "Get Gmail Messages" node
- Add label filters, date ranges, etc.

**Task Context:**
- Change `context` parameter in API call:
  - `"work"` - for work-related emails
  - `"personal"` - for personal emails
  - `"student"` - for academic emails
  - `"mixed"` - for general content

**Output Format:**
- Duplicate "Format for Todoist" node for multiple platforms
- Connect to Notion, ClickUp, or other task managers

## 🔧 Alternative Workflows

### Simple Text-to-Tasks:
```json
Manual Trigger → HTTP Request (API) → Split Tasks → Create Tasks
```

### Email Webhook:
```json
Webhook Trigger → AI Task Extraction → Multiple Outputs
```

### Slack Integration:
```json
Slack Trigger → AI Task Extraction → Slack Response + Task Creation
```

## 🛠️ Troubleshooting:

### Common Issues:

**"Connection refused" error:**
- ✅ Ensure Task Extraction API is running
- ✅ Check URL and port in HTTP Request node
- ✅ Verify firewall/network settings

**"No tasks extracted":**
- ✅ Check email content has actionable items
- ✅ Try different context values
- ✅ Verify API key is configured correctly

**"Authentication failed":**
- ✅ Recreate OAuth credentials
- ✅ Check token permissions
- ✅ Verify API quotas aren't exceeded

**"Invalid JSON response":**
- ✅ Check API logs for errors
- ✅ Verify Gemini API key is valid
- ✅ Test API endpoint manually with curl

### Debug Tips:

1. **Enable Debug Mode:** In n8n settings, turn on "Debug Mode"
2. **Check Node Outputs:** Click on nodes to see data flow
3. **Test API Manually:** Use curl or Postman to test API endpoints
4. **Monitor Logs:** Check both n8n and API logs

## 📚 More Examples:

Want more workflow examples? Check out:
- [n8n Community](https://community.n8n.io/)
- [n8n Workflow Templates](https://n8n.io/workflows/)
- Create an issue in this repo for specific use cases!

---

**Need help?** Open an issue in the main repository or join the discussion!
