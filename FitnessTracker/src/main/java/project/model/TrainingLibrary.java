package project.model;

import java.util.HashMap;

public final class TrainingLibrary extends Training {
	private HashMap<Integer, Exercise> exerciseMap = new HashMap<>(); 
	//private Integer TrainingId;
	
	public void initialize()
	{
		exerciseMap.put(3001, new FlexibleExercise("Прыжки в длину", "очень полезное упражнение - мамой клянусь", 3001));
		exerciseMap.put(1001, new StrengthExercise("Планка на прямых руках", "статическая нагрузка мышц груди", 1001));
		exerciseMap.put(1002, new StrengthExercise("Отжимания", "упражнение для верхней части тела. Выполняется, когда лицо опущено вниз, и руки отталкивают тело от земли.", 1002));
		exerciseMap.put(1003, new StrengthExercise("Приседания", "упражнение для ног и ягодиц, выполняется, опускаясь в положение, как будто вы садитесь на стул, а затем поднимаетесь.", 1003));
		exerciseMap.put(2001, new CardioExercise("Бег на месте", "кардиоупражнение, которое можно выполнять дома, бегая на месте.", 2001));
		exerciseMap.put(1004, new StrengthExercise("Подтягивания", "упражнение для верхней части тела, выполняется подтягиванием тела вверх, держась за перекладину.", 1004));
		exerciseMap.put(3002, new FlexibleExercise("Махи ногами", "упражнение для ног и ягодиц, выполняется махая ногой вперед и назад.", 3002));
		exerciseMap.put(3003, new FlexibleExercise("Лодка", "упражнение для спины и пресса, выполняется, лежа на животе и поднимая туловище и ноги от пола, формируя форму лодки.", 3003));
		exerciseMap.put(2002, new CardioExercise("Прыжки на скакалке", "кардиоупражнение, которое также тренирует координацию, выполняется с прыжками через скакалку.", 2002));
    
	}
	
	public HashMap<Integer, Exercise> getExerciseMap()
	{
		return exerciseMap;
	}
}
