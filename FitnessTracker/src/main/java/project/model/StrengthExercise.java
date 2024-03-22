package project.model;

import java.util.concurrent.atomic.AtomicLong;

public class StrengthExercise implements Exercise {
	private Integer id;
	private String description;
	private String name;
	private static int idCounter = 1;
	
	public StrengthExercise(String name, String description) {
		this.name = name;
		this.description = description;
		
		generateId();
	}
	
	public StrengthExercise(String name, String description, int id) {
		this.name = name;
		this.description = description;
		this.id = id;
	}
	
	private void generateId() {
		id = 11000 + idCounter;
		idCounter++;
	}
	
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public Integer getExerciseId() {
		return id;
	}
}
