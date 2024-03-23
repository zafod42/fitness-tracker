package project.model;

import java.util.ArrayList;
import java.util.List;

public class ExerciseList {
	private static List<Exercise> ExerciseList = new ArrayList<>();
	
	public void addExercise(Exercise exercise) {
		ExerciseList.add(exercise);
	}
	
	public void print() {
		
	}
}
