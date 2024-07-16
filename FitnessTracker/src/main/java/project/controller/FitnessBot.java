package project.controller;
import project.model.*;

import lombok.Getter;
import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

@Component
public class FitnessBot extends TelegramLongPollingBot {
	@Value("${bot.name}")
	private String name;
	@Value("${bot.token}")
	private String token;
	@Value("${bot.url}")
	@Getter
	private String url;

	private final UpdateController controller;
	@Getter
	private TrainingLibrary Exercises = new TrainingLibrary();

	static final Logger log = org.apache.log4j.Logger.getLogger(FitnessBot.class);

	public FitnessBot(UpdateController controller) {
		this.controller = controller;
        Exercises.initialize();

		List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "запустить фитнес-трекер"));
		listOfCommands.add(new BotCommand("/register", "зарегистрироваться"));
		listOfCommands.add(new BotCommand("/delete_user","удаление профиля"));
		listOfCommands.add(new BotCommand("/tren", "вывод списка доступных упражнений"));
		listOfCommands.add(new BotCommand("/start_exercise", "начать упражнение"));
		listOfCommands.add(new BotCommand("/finish_set", "закончить подход"));
		listOfCommands.add(new BotCommand("/stop_exercise", "досрочно завершить упражнение"));
		listOfCommands.add(new BotCommand("/create_exercise", "создать упражнение"));
		listOfCommands.add(new BotCommand("/update_exercise", "изменить упражнение"));
		listOfCommands.add(new BotCommand("/delete_exercise", "удалить упражнение"));
		listOfCommands.add(new BotCommand("/toggle_notification", "переключить уведомления"));
		try {
			this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
		}
		catch (TelegramApiException e) {
			log.error("Error setting bot's command list: " + e.getMessage());
		}
	}

	@PostConstruct
	public void init () {
		controller.registerBot(this);
	}

	@Override
	public String getBotUsername() {
		return name;
	}

	@Override
	public String getBotToken() {
		return token;
	}

	@Override
	public void onUpdateReceived(Update update) {
		controller.processUpdate(update);
	}

    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            }
			catch (TelegramApiException e) {
				e.printStackTrace();
                log.error(e);
            }
        }
    }

    public void sendMsg(String chatId, String msg)
	{
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(chatId);
		sendMessage.setText(msg);
		try {
			execute(sendMessage);
		} catch(TelegramApiException e) {
			log.error(e);
		}
	}
}
