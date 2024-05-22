package project.model;

import project.controller.FitnessBot;

public interface Exercise {

	String getName();
	String getDescription();
	Integer getExerciseId();
	boolean isRunning();
	int getSets();
	int getRepetitions();
	float getWeightPerRep();
	float getTimeInSeconds();
	void startExercise(long chatId, FitnessBot bot);
	public void stopExercise(long chatId, FitnessBot bot);
}
