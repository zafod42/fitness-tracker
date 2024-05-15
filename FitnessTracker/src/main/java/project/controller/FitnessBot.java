package project.controller;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.model.TrainingLibrary;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;

@Component
public class FitnessBot extends TelegramLongPollingBot {
	@Value("${bot.name}")
	private String name = "FitTrackDomovonokBot";
	@Value("${bot.token}")
	private String token = "7033585733:AAGXgDOBCO3R9lz2XX1HVVGOWR_hcfThNds";
	private final UpdateController controller;

	@Getter
	private TrainingLibrary Exercises = new TrainingLibrary();

	private static final Logger log = org.apache.log4j.Logger.getLogger(FitnessBot.class);

	public FitnessBot(UpdateController controller) {
		this.controller = controller;
		Exercises.initialize();
		List<BotCommand> listOfCommands = new ArrayList<>();
	        listOfCommands.add(new BotCommand("/start", "запустить фитнес-трекер"));
		listOfCommands.add(new BotCommand("/register", "зарегистрироваться"));
	        listOfCommands.add(new BotCommand("/tren", "вывод списка доступных упражнений"));
	        listOfCommands.add(new BotCommand("/test", "тестовая команда"));
	        listOfCommands.add(new BotCommand("/stat", "ваша статистика"));
		listOfCommands.add(new BotCommand("/delete", "удаление аккаунта"));

	        try {
	            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
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
                log.error(e);
            }
        }
    }
}
