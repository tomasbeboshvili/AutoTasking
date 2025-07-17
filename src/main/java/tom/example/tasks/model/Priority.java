package tom.example.tasks.model;

public enum Priority {
	BAJA("🟢", "#4ade80", 1),
	MEDIA("🟡", "#f59e0b", 2),
	ALTA("🔴", "#ef4444", 3),
	CRITICA("🚨", "#dc2626", 4);
	
	private final String icon;
	private final String color;
	private final int level;
	
	Priority(String icon, String color, int level) {
		this.icon = icon;
		this.color = color;
		this.level = level;
	}
	
	public String getIcon() { return icon; }
	public String getColor() { return color; }
	public int getLevel() { return level; }
}
