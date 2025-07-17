package tom.example.tasks.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskExportDto {
    private String title;
    private String description;
    private LocalDate dueDate;
    private String priority;
    private boolean completed;
    private String context;
    private String category;
    
    // Formatos específicos para diferentes plataformas
    private Object todoistFormat;
    private Object notionFormat;
    private Object clickUpFormat;
    
    public TaskExportDto() {}
    
    // Getters y Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Object getTodoistFormat() {
        return todoistFormat;
    }
    
    public void setTodoistFormat(Object todoistFormat) {
        this.todoistFormat = todoistFormat;
    }
    
    public Object getNotionFormat() {
        return notionFormat;
    }
    
    public void setNotionFormat(Object notionFormat) {
        this.notionFormat = notionFormat;
    }
    
    public Object getClickUpFormat() {
        return clickUpFormat;
    }
    
    public void setClickUpFormat(Object clickUpFormat) {
        this.clickUpFormat = clickUpFormat;
    }
}
