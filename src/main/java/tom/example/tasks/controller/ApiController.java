package tom.example.tasks.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.example.tasks.dto.TextAnalysisRequest;
import tom.example.tasks.dto.ApiResponse;
import tom.example.tasks.dto.TaskExportDto;
import tom.example.tasks.dto.WebhookRequest;
import tom.example.tasks.model.Task;
import tom.example.tasks.service.GeminiAIService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*") // Para permitir llamadas desde n8n y otras herramientas
public class ApiController {

    private final GeminiAIService geminiAIService;

    public ApiController(GeminiAIService geminiAIService) {
        this.geminiAIService = geminiAIService;
    }

    /**
     * Endpoint principal para análisis de texto con IA
     * Perfecto para n8n workflows
     */
    @PostMapping("/analyze-text")
    public ResponseEntity<ApiResponse<List<TaskExportDto>>> analyzeText(@RequestBody TextAnalysisRequest request) {
        try {
            List<Task> extractedTasks = geminiAIService.extractTasksFromText(request.getText(), request.getContext());
            
            // Convertir a formato de exportación
            List<TaskExportDto> exportTasks = extractedTasks.stream()
                .map(this::convertToExportDto)
                .collect(Collectors.toList());
            
            ApiResponse<List<TaskExportDto>> response = new ApiResponse<>();
            response.setSuccess(true);
            response.setMessage("Tareas extraídas correctamente");
            response.setData(exportTasks);
            response.setCount(exportTasks.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<TaskExportDto>> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error al analizar texto: " + e.getMessage());
            errorResponse.setData(null);
            errorResponse.setCount(0);
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Webhook para recibir emails y procesarlos automáticamente
     * Ideal para integración con servicios de email
     */
    @PostMapping("/webhook/email")
    public ResponseEntity<ApiResponse<List<TaskExportDto>>> processEmail(@RequestBody WebhookRequest webhook) {
        try {
            String emailContent = extractEmailContent(webhook);
            String context = determineContextFromEmail(webhook);
            
            List<Task> extractedTasks = geminiAIService.extractTasksFromText(emailContent, context);
            
            List<TaskExportDto> exportTasks = extractedTasks.stream()
                .map(this::convertToExportDto)
                .collect(Collectors.toList());
            
            ApiResponse<List<TaskExportDto>> response = new ApiResponse<>();
            response.setSuccess(true);
            response.setMessage("Email procesado correctamente");
            response.setData(exportTasks);
            response.setCount(exportTasks.size());
            response.setSource("email");
            response.setSender(webhook.getSender());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<TaskExportDto>> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error al procesar email: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Endpoint para análisis rápido sin guardar en BD
     * Ideal para preview o testing
     */
    @PostMapping("/analyze-preview")
    public ResponseEntity<ApiResponse<List<TaskExportDto>>> analyzePreview(@RequestBody TextAnalysisRequest request) {
        try {
            // Usar directamente Gemini sin guardar en BD
            List<Task> extractedTasks = geminiAIService.extractTasksFromText(request.getText(), request.getContext());
            
            List<TaskExportDto> exportTasks = extractedTasks.stream()
                .map(this::convertToExportDto)
                .collect(Collectors.toList());
            
            ApiResponse<List<TaskExportDto>> response = new ApiResponse<>();
            response.setSuccess(true);
            response.setMessage("Análisis completado (preview)");
            response.setData(exportTasks);
            response.setCount(exportTasks.size());
            response.setPreview(true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<TaskExportDto>> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error en análisis preview: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Endpoint de salud para verificar estado de la API
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("API funcionando correctamente");
        response.setData("OK");
        
        return ResponseEntity.ok(response);
    }

    // Métodos auxiliares
    private TaskExportDto convertToExportDto(Task task) {
        return convertToExportDto(task, "standard");
    }
    
    private TaskExportDto convertToExportDto(Task task, String format) {
        TaskExportDto dto = new TaskExportDto();
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setPriority(task.getPriority().toString());
        dto.setCompleted(task.isCompleted());
        dto.setContext(task.getContext());
        dto.setCategory(task.getCategory());
        
        // Formatear según la plataforma de destino
        switch (format.toLowerCase()) {
            case "todoist":
                dto.setTodoistFormat(formatForTodoist(task));
                break;
            case "notion":
                dto.setNotionFormat(formatForNotion(task));
                break;
            case "clickup":
                dto.setClickUpFormat(formatForClickUp(task));
                break;
        }
        
        return dto;
    }
    
    private String extractEmailContent(WebhookRequest webhook) {
        StringBuilder content = new StringBuilder();
        
        if (webhook.getSubject() != null) {
            content.append("Asunto: ").append(webhook.getSubject()).append("\n");
        }
        
        if (webhook.getBody() != null) {
            content.append(webhook.getBody());
        }
        
        return content.toString();
    }
    
    private String determineContextFromEmail(WebhookRequest webhook) {
        String subject = webhook.getSubject() != null ? webhook.getSubject().toLowerCase() : "";
        String sender = webhook.getSender() != null ? webhook.getSender().toLowerCase() : "";
        
        // Lógica inteligente para determinar contexto
        if (subject.contains("exam") || subject.contains("tarea") || subject.contains("universidad") ||
            sender.contains("edu") || sender.contains("universidad")) {
            return "student";
        }
        
        if (subject.contains("reunión") || subject.contains("meeting") || subject.contains("proyecto") ||
            subject.contains("cliente") || sender.contains("trabajo")) {
            return "work";
        }
        
        if (subject.contains("médico") || subject.contains("cita") || subject.contains("personal")) {
            return "personal";
        }
        
        return "mixed";
    }
    
    private Map<String, Object> formatForTodoist(Task task) {
        Map<String, Object> todoistTask = new HashMap<>();
        todoistTask.put("content", task.getTitle());
        todoistTask.put("description", task.getDescription());
        todoistTask.put("due_date", task.getDueDate() != null ? task.getDueDate().toString() : null);
        todoistTask.put("priority", mapPriorityToTodoist(task.getPriority()));
        return todoistTask;
    }
    
    private Map<String, Object> formatForNotion(Task task) {
        Map<String, Object> notionTask = new HashMap<>();
        notionTask.put("title", task.getTitle());
        notionTask.put("status", task.isCompleted() ? "Done" : "Not started");
        notionTask.put("priority", task.getPriority().toString());
        notionTask.put("due", task.getDueDate() != null ? task.getDueDate().toString() : null);
        return notionTask;
    }
    
    private Map<String, Object> formatForClickUp(Task task) {
        Map<String, Object> clickUpTask = new HashMap<>();
        clickUpTask.put("name", task.getTitle());
        clickUpTask.put("description", task.getDescription());
        clickUpTask.put("status", task.isCompleted() ? "complete" : "open");
        clickUpTask.put("priority", mapPriorityToClickUp(task.getPriority()));
        return clickUpTask;
    }
    
    private int mapPriorityToTodoist(tom.example.tasks.model.Priority priority) {
        return switch (priority) {
            case CRITICA -> 4;
            case ALTA -> 3;
            case MEDIA -> 2;
            case BAJA -> 1;
        };
    }
    
    private String mapPriorityToClickUp(tom.example.tasks.model.Priority priority) {
        return switch (priority) {
            case CRITICA -> "urgent";
            case ALTA -> "high";
            case MEDIA -> "normal";
            case BAJA -> "low";
        };
    }
}
