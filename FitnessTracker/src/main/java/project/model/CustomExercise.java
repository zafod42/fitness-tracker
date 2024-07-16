package project.model;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.controller.FitnessBot;

public class CustomExercise extends Exercise{

    private Integer id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private int sets; // Количество подходов
    @Getter
    @Setter
    private int repetitions; // Количество повторений
    private static int idCounter = 1;
    private int currentSet = 0;
    @Setter
    private boolean isRunning = false; // Статус выполнения упражнения

    private void generateId() {
        id = idCounter;
        idCounter++;
    }

    public CustomExercise()
    {

    }
    // КОЛ-ВО ПОВТОРЕНИЙ + КОЛ-ВО ПОДХОДОВ
    public CustomExercise(String name, String description, int sets, int repetitions) {
        this.name = name;
        this.description = description;
        this.sets = sets;
        this.repetitions = repetitions;

        generateId();
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

    public Integer getExerciseId() {
        return id;
    }
    public float getTimeInSeconds() {
        return 0;
    }
    public float getWeightPerRep() {
        return 0;
    }
}
