package project.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateController {
	private FitnessBot bot;
	private static final Logger log = org.apache.log4j.Logger.getLogger(FitnessBot.class);
	
	public void registerBot(FitnessBot bot) {
        this.bot = bot;
    }
	
	
	public void processUpdate(Update update) {
		if (update == null) {
			log.error("Received update is null");
            return;
		}
		
		if (update.hasMessage()) {
			
		} else {
			log.error("Unsupported message type is received: " + update);
		}
	}
	
	public void defineCommand(Message msg) {
		String command = msg.getText();
		
		switch(command)
		{
		case "/start":
			registerUser(msg);
		case "/tren":
			viewExercises();
		case "/stat":
			viewStat();
		}
	}
	
	public void registerUser(Message msg) {
		//тут должна быть функция, проверяющая зарегестрирован ли уже пользователь
		//если нет -> позволить зарегестрировваться
		//если да -> вывести сообщение настройки даты тренировок/возможность начать тренировку прямо сейчас

	}
	
	public void viewExercises() {
		//тут должна быть функция, выводящая доступные упражнения
		//пользователь должен иметь возможность тыкать на них прямо в сообщении, чтоб узнать о них подробнее/добавить упражнение в тренировку
	}
	
	public void viewStat() {
		//тут должна быть функция, что ищет в базе данных Id чата 
		//и по этому id выводит статистику пользователя из БД
	}
	
}
