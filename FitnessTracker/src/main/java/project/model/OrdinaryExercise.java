package project.model;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.controller.FitnessBot;


public class OrdinaryExercise implements Exercise {
	private Integer id;
	@Getter
	private String name;
	@Getter
	private String description;
	@Getter
	private int sets; // Количество подходов
	@Getter
	private int repetitions; // Количество повторений
	private static int idCounter = 1;
	private int currentSet = 0;
	private boolean isRunning = false; // Статус выполнения упражнения


	// КОЛ-ВО ПОВТОРЕНИЙ + КОЛ-ВО ПОДХОДОВ
	public OrdinaryExercise(String name, String description, int sets, int repetitions) {
		this.name = name;
		this.description = description;
		this.sets = sets;
		this.repetitions = repetitions;

		generateId();
	}

	public OrdinaryExercise(String name, String description, int sets, int repetitions, int id) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.sets = sets;
		this.repetitions = repetitions;

	}

	public void startExercise(long chatId, FitnessBot bot) {
		isRunning = true;
		currentSet = 1;

		try {
			SendMessage startMessage = new SendMessage();
			startMessage.setChatId(String.valueOf(chatId));
			startMessage.setText("Начало упражнения: " + name);
			bot.execute(startMessage);
			SendMessage setStartMessage = new SendMessage();
			setStartMessage.setChatId(String.valueOf(chatId));
			setStartMessage.setText("Подход " + currentSet + " из " + sets);
			bot.execute(setStartMessage);
		}
		catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

	public void finishSet(long chatId, FitnessBot bot) {
		if (isRunning && currentSet > 0 && currentSet <= sets) {
			try {
				SendMessage setEndMessage = new SendMessage();
				setEndMessage.setChatId(String.valueOf(chatId));
				setEndMessage.setText("Подход " + currentSet + " из " + sets + " завершён");
				bot.execute(setEndMessage);
				currentSet++;
				if (currentSet <= sets) {
					SendMessage nextSetMessage = new SendMessage();
					nextSetMessage.setChatId(String.valueOf(chatId));
					nextSetMessage.setText("Подход " + currentSet + " из " + sets);
					bot.execute(nextSetMessage);
				}
				else {
					isRunning = false;
					SendMessage endMessage = new SendMessage();
					endMessage.setChatId(String.valueOf(chatId));
					endMessage.setText("Упражнение " + name + " завершено");
					bot.execute(endMessage);
				}
			}
			catch (TelegramApiException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void stopExercise(long chatId, FitnessBot bot) {
		if (isRunning) {
			isRunning = false;
			try {
				SendMessage stopMessage = new SendMessage();
				stopMessage.setChatId(String.valueOf(chatId));
				stopMessage.setText("Упражнение " + name + " досрочно завершено");
				bot.execute(stopMessage);
			}
			catch (TelegramApiException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setIsRunning(boolean running) {
		isRunning = running;
	}

	public Integer getExerciseId() {
		return id;
	}

	private void generateId() {
		id = 11000 + idCounter;
		idCounter++;
	}

	public float getTimeInSeconds() {
		return 0;
	}

	public float getWeightPerRep() {
		return 0;
	}
}


