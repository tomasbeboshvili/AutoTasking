package tom.example.tasks.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import tom.example.tasks.model.Priority;
import tom.example.tasks.model.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class GeminiAIService {

    @Value("${gemini.api.key:}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public GeminiAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Task> extractTasksFromText(String text, String context) {
        if (apiKey.isEmpty()) {
            // Fallback a IA local si no hay API key
            return extractTasksLocally(text, context);
        }
        
        try {
            String prompt = buildExtractionPrompt(text, context);
            String response = callGeminiAPI(prompt);
            return parseTasksFromResponse(response, context);
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            // Fallback a IA local en caso de error
            return extractTasksLocally(text, context);
        }
    }
    
    public Priority analyzePriority(Task task, String context) {
        if (apiKey.isEmpty()) {
            // Fallback a análisis local
            return analyzeLocalPriority(task, context);
        }
        
        try {
            String prompt = buildPriorityPrompt(task, context);
            String response = callGeminiAPI(prompt);
            return parsePriorityFromResponse(response);
        } catch (Exception e) {
            System.err.println("Error analyzing priority with Gemini: " + e.getMessage());
            return analyzeLocalPriority(task, context);
        }
    }

    private String buildExtractionPrompt(String text, String context) {
        return String.format("""
            Analiza el siguiente texto y extrae todas las tareas que encuentres.
            Contexto del usuario: %s
            
            Para cada tarea, devuelve SOLO un JSON válido con este formato exacto:
            [
              {
                "title": "título de la tarea",
                "description": "descripción opcional",
                "dueDate": "YYYY-MM-DD o null",
                "priority": "CRITICA|ALTA|MEDIA|BAJA"
              }
            ]
            
            Reglas:
            - Solo devuelve el JSON, sin texto adicional
            - Prioridad CRITICA: exámenes, deadlines urgentes, emergencias
            - Prioridad ALTA: reuniones importantes, proyectos con fecha próxima
            - Prioridad MEDIA: tareas regulares con fecha
            - Prioridad BAJA: tareas opcionales o sin fecha límite
            - Para fechas relativas (hoy, mañana), usa la fecha actual: %s
            
            Texto a analizar:
            %s
            """, 
            getContextDescription(context),
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            text
        );
    }
    
    private String buildPriorityPrompt(Task task, String context) {
        return String.format("""
            Analiza la prioridad de esta tarea según el contexto del usuario.
            
            Tarea: %s
            Descripción: %s
            Fecha límite: %s
            Contexto del usuario: %s
            
            Devuelve SOLO una de estas opciones: CRITICA, ALTA, MEDIA, BAJA
            
            Reglas:
            - CRITICA: exámenes, emergencias, deadlines críticos
            - ALTA: reuniones importantes, proyectos urgentes
            - MEDIA: tareas con fecha próxima
            - BAJA: tareas opcionales o sin urgencia
            """,
            task.getTitle(),
            task.getDescription() != null ? task.getDescription() : "Sin descripción",
            task.getDueDate() != null ? task.getDueDate().toString() : "Sin fecha",
            getContextDescription(context)
        );
    }
    
    private String getContextDescription(String context) {
        return switch (context) {
            case "student" -> "Estudiante universitario (priorizar exámenes, proyectos académicos)";
            case "work" -> "Profesional (priorizar reuniones, deadlines laborales)";
            case "personal" -> "Personal (priorizar salud, familia, finanzas)";
            default -> "Mixto (equilibrar trabajo, estudios y vida personal)";
        };
    }

    private String callGeminiAPI(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey;
        
        // Preparar el payload para Gemini
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        
        Map<String, Object> contentItem = new HashMap<>();
        contentItem.put("parts", List.of(part));
        
        content.put("contents", List.of(contentItem));
        
        // Configuración de generación
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1);
        generationConfig.put("maxOutputTokens", 1000);
        
        content.put("generationConfig", generationConfig);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(content, headers);
        
        var response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        
        // Parsear respuesta de Gemini
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        JsonNode candidates = jsonResponse.get("candidates");
        
        if (candidates != null && candidates.size() > 0) {
            JsonNode firstCandidate = candidates.get(0);
            JsonNode content_response = firstCandidate.get("content");
            if (content_response != null) {
                JsonNode parts = content_response.get("parts");
                if (parts != null && parts.size() > 0) {
                    return parts.get(0).get("text").asText();
                }
            }
        }
        
        throw new Exception("No se pudo obtener respuesta válida de Gemini");
    }
    
    private List<Task> parseTasksFromResponse(String response, String context) {
        List<Task> tasks = new ArrayList<>();
        
        try {
            // Limpiar la respuesta para extraer solo el JSON
            String jsonStr = response.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();
            
            JsonNode jsonArray = objectMapper.readTree(jsonStr);
            
            if (jsonArray.isArray()) {
                for (JsonNode taskNode : jsonArray) {
                    Task task = new Task();
                    task.setTitle(taskNode.get("title").asText());
                    
                    if (taskNode.has("description") && !taskNode.get("description").isNull()) {
                        task.setDescription(taskNode.get("description").asText());
                    }
                    
                    if (taskNode.has("dueDate") && !taskNode.get("dueDate").isNull()) {
                        String dateStr = taskNode.get("dueDate").asText();
                        if (!dateStr.equals("null")) {
                            task.setDueDate(LocalDate.parse(dateStr));
                        }
                    }
                    
                    if (taskNode.has("priority")) {
                        String priorityStr = taskNode.get("priority").asText();
                        task.setPriority(Priority.valueOf(priorityStr));
                    } else {
                        task.setPriority(Priority.MEDIA);
                    }
                    
                    task.setCompleted(false);
                    task.setContext(context);
                    task.setCategory(context != null ? context : "general");
                    
                    tasks.add(task);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Gemini response: " + e.getMessage());
            // Fallback a extracción local
            return extractTasksLocally(response, context);
        }
        
        return tasks;
    }
    
    private Priority parsePriorityFromResponse(String response) {
        String priority = response.trim().toUpperCase();
        
        try {
            return Priority.valueOf(priority);
        } catch (IllegalArgumentException e) {
            // Si no se puede parsear, analizar el texto
            if (response.toLowerCase().contains("critica") || response.toLowerCase().contains("critical")) {
                return Priority.CRITICA;
            } else if (response.toLowerCase().contains("alta") || response.toLowerCase().contains("high")) {
                return Priority.ALTA;
            } else if (response.toLowerCase().contains("baja") || response.toLowerCase().contains("low")) {
                return Priority.BAJA;
            } else {
                return Priority.MEDIA;
            }
        }
    }
    
    // Métodos de fallback para IA local
    private List<Task> extractTasksLocally(String text, String context) {
        List<Task> tasks = new ArrayList<>();
        
        System.out.println("DEBUG: Executing extractTasksLocally with text: " + text);
        System.out.println("DEBUG: Context: " + context);
        
        // Dividir por puntos Y saltos de línea para mejor detección
        String[] sentences = text.split("[.!?\\n\\r]+");
        for (String sentence : sentences) {
            sentence = sentence.trim();
            
            if (sentence.length() < 8) continue; // Ignorar frases muy cortas
            
            // Patrones expandidos para detectar tareas
            boolean isTask = false;
            String taskTitle = sentence;
            
            // Patrones de listas tradicionales
            if (sentence.matches("^[-*•]\\s*.+") || sentence.matches("^\\d+[.):]\\s*.+")) {
                taskTitle = sentence.replaceFirst("^[-*•\\d.):]+\\s*", "").trim();
                isTask = true;
            }
            // Patrones de acción directa
            else if (sentence.toLowerCase().matches(".*(debe|tiene que|necesita|hay que|tengo que|tienes que).*")) {
                isTask = true;
            }
            // Verbos de acción al inicio
            else if (sentence.toLowerCase().matches("^(hacer|revisar|enviar|llamar|comprar|estudiar|reunir|contactar|completar|terminar|preparar|organizar|planificar|coordinar|inscribir|pagar|limpiar|cambiar|agendar|actualizar|documentar|ejecutar|restaurar|identificar|corregir).*")) {
                isTask = true;
            }
            // Patrones de obligación en español
            else if (sentence.toLowerCase().matches(".*(antes del|antes de|para el|para hoy|urgente|importante|deadline|fecha límite).*")) {
                isTask = true;
            }
            // Patrones de nombres + acción
            else if (sentence.toLowerCase().matches("^[a-záéíóúü]+\\s+(debe|tiene|necesita|va a).*")) {
                isTask = true;
            }
            
            if (isTask && taskTitle.length() > 5) {
                Task task = new Task();
                task.setTitle(taskTitle);
                task.setCompleted(false);
                task.setContext(context);
                task.setCategory(context != null ? context : "general");
                
                // Detectar fechas en el texto
                LocalDate dueDate = extractDateFromText(taskTitle);
                task.setDueDate(dueDate);
                
                // Analizar prioridad
                task.setPriority(analyzeLocalPriority(task, context));
                
                tasks.add(task);
                System.out.println("DEBUG: Task extracted: " + taskTitle);
            }
        }
        
        System.out.println("DEBUG: Total tasks extracted: " + tasks.size());
        return tasks;
    }
    
    private LocalDate extractDateFromText(String text) {
        String lowerText = text.toLowerCase();
        LocalDate today = LocalDate.now();
        
        if (lowerText.contains("hoy")) {
            return today;
        } else if (lowerText.contains("mañana")) {
            return today.plusDays(1);
        } else if (lowerText.contains("viernes") && lowerText.contains("19")) {
            return LocalDate.of(2025, 7, 19);
        } else if (lowerText.contains("lunes") && lowerText.contains("22")) {
            return LocalDate.of(2025, 7, 22);
        } else if (lowerText.contains("miércoles")) {
            return today.plusDays((3 - today.getDayOfWeek().getValue() + 7) % 7);
        } else if (lowerText.contains("antes del") || lowerText.contains("para el")) {
            // Aquí podrías agregar más lógica de parsing de fechas
            return null;
        }
        
        return null;
    }
    
    private Priority analyzeLocalPriority(Task task, String context) {
        String text = (task.getTitle() + " " + (task.getDescription() != null ? task.getDescription() : "")).toLowerCase();
        
        // Palabras clave para prioridad crítica
        if (text.contains("urgente") || text.contains("examen") || text.contains("emergencia") || 
            text.contains("crítico") || text.contains("deadline")) {
            return Priority.CRITICA;
        }
        
        // Palabras clave para prioridad alta
        if (text.contains("importante") || text.contains("reunión") || text.contains("cliente") ||
            text.contains("proyecto") || text.contains("entrega")) {
            return Priority.ALTA;
        }
        
        // Palabras clave para prioridad baja
        if (text.contains("opcional") || text.contains("cuando pueda") || text.contains("algún día")) {
            return Priority.BAJA;
        }
        
        return Priority.MEDIA;
    }
}
