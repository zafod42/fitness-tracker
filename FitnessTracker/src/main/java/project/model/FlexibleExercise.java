package project.model;

import java.util.concurrent.atomic.AtomicLong;

public class FlexibleExercise implements Exercise {
	private Integer id;
	private String description;
	private String name;
	private static int idCounter = 1;
	
	public FlexibleExercise(String name, String description) {
		this.name = name;
		this.description = description;
		
		generateId();
	}
	
	public FlexibleExercise(String name, String description, int id) {
		this.name = name;
		this.description = description;
		this.id = id;
	}
	
	private void generateId() {
		id = 13000 + idCounter;
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
