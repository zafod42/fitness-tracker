package project.model;

import project.controller.FitnessBot;

//NOTE: Паттерн Стратегия?

public abstract class Exercise {

	public abstract String getName();
	public abstract String getDescription();
	abstract Integer getExerciseId();
	abstract int getSets();
	abstract int getRepetitions();
	abstract float getWeightPerRep();
	abstract float getTimeInSeconds();

	// IDEA:
	// Вставить обычный вызов статистики в startExercise
	// И расширить метод вызовом его стандартной + спец. версии
	abstract void startExercise(long chatId, FitnessBot bot);


	// Либо придется вызывать статистику в каждом BlablaExercise отдельно
}
