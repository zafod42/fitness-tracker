package project.model;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.controller.FitnessBot;

import java.util.Timer;
import java.util.TimerTask;

public class TimeExercise extends Exercise {
    private Integer id;
    @Getter
    private String name;
    @Getter
    private String description;
    @Getter
    private float timeInSeconds;
    @Getter
    private int sets; // Количество подходов/повторений
    private static int idCounter = 1;
    private boolean isRunning = false;

    public TimeExercise(String name, String description, int sets, float timeInSeconds) {
        this.name = name;
        this.description = description;
        this.timeInSeconds = timeInSeconds;
        this.sets = sets;

        generateId();
    }

    public TimeExercise(String name, String description, int sets, float timeInSeconds, int id) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sets = sets;
        this.timeInSeconds = timeInSeconds;
    }

    public void startExercise(long chatId, FitnessBot bot) {

        try {
            Timer timer = new Timer();
            final int[] currentSet = {1}; // Переменная для отслеживания текущего подхода
            try {
                SendMessage startMessage = new SendMessage();
                startMessage.setChatId(String.valueOf(chatId));
                startMessage.setText("Начало упражнения: " + name);
                bot.execute(startMessage);
            }
            catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

            // Запуск одного подхода
            Runnable singleSetRunnable = () -> {
                if (currentSet[0] <= sets) {
                    SendMessage response = new SendMessage();
                    response.setChatId(String.valueOf(chatId));
                    response.setText("Выполнение подхода №" + currentSet[0]);
                    System.out.println("Выполнение подхода №" + currentSet[0]);
                    bot.sendAnswerMessage(response);

                    // Добавляем задержку перед следующим сообщением
                    try {
                        Thread.sleep((long) timeInSeconds * 1000); // Задержка в миллисекундах
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    currentSet[0]++;
                    response.setText("Время подхода №" + (currentSet[0] - 1) + " завершено.");
                    bot.sendAnswerMessage(response);
                }
            };

            // Запуск первого подхода немедленно
            singleSetRunnable.run();

            // Запуск оставшихся подходов с задержкой
            for (int i = 1; i < sets; i++) {
                long delay = (i + 1) * (long) timeInSeconds * 1000; // Время в мс
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        singleSetRunnable.run();
                    }
                }, delay);
            }
        }

        catch (Exception e) {
            System.err.println("An unexpected error occurred:");
            e.printStackTrace();
        }
    }

    public void stopExercise(long chatId, FitnessBot bot) {
        isRunning = false;
        try {
            SendMessage stopMessage = new SendMessage();
            stopMessage.setChatId(String.valueOf(chatId));
            stopMessage.setText("Упражнение " + name + " досрочно завершено");
            bot.execute(stopMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getExerciseId() {
        return id;
    }

    private void generateId() {
        id = 11000 + idCounter;
        idCounter++;
    }

    public int getRepetitions() {
        return 0;
    }

    public float getWeightPerRep() {
        return 0;
    }
}
