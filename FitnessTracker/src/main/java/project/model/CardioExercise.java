package project.model;

import java.util.concurrent.atomic.AtomicLong;

public class CardioExercise implements Exercise {
	private Integer id;
	private String description;
	private String name;
	private static int idCounter = 1;
	
	public CardioExercise(String name, String description) {
		this.name = name;
		this.description = description;
		
		generateId();
	}
	
	public CardioExercise(String name, String description, int id) {
		this.name = name;
		this.description = description;
		this.id = id;
	}
	
	private void generateId() {
		id = 12000 + idCounter;
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
