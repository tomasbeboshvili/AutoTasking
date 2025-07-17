package tom.example.tasks.dto;

public class TextAnalysisRequest {
    private String text;
    private String context;
    
    public TextAnalysisRequest() {}
    
    public TextAnalysisRequest(String text, String context) {
        this.text = text;
        this.context = context;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
}
