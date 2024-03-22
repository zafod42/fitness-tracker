package project.model;

public class BalanceExercise implements Exercise {
	private Integer id;
	private String description;
	private String name;
	private static int idCounter = 1;
	
	public BalanceExercise(String name, String description) {
		this.name = name;
		this.description = description;
		
		generateId();
	}
	
	public BalanceExercise(String name, String description, int id) {
		this.name = name;
		this.description = description;
		this.id = id;
	}
	
	private void generateId() {
		id = 14000 + idCounter;
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
