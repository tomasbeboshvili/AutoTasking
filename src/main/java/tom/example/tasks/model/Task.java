package tom.example.tasks.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {
	
	private Integer id;
	
	private String title;

	private String description;
	
	private LocalDate dueDate;
	
	private boolean completed;
	
	private Priority priority = Priority.MEDIA;
	
	private String category;
	
	private String context; // student, work, personal, mixed
}
