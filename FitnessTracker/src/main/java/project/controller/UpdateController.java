package project.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import project.model.CardioExercise;
import project.model.Exercise;
import project.model.FlexibleExercise;
import project.model.StrengthExercise;

@Component
public class UpdateController {
	private FitnessBot bot;
	private static final Logger log = org.apache.log4j.Logger.getLogger(FitnessBot.class);
	
	private static List<Exercise> ExerciseList = new ArrayList<>();
	
	public void registerBot(FitnessBot bot) {
        this.bot = bot;
        
        ExerciseList.add(new FlexibleExercise("Прижки в длинну", "очень полезное упражнение - мамой клянусь", 3001));
        ExerciseList.add(new StrengthExercise("Планка на прямых руках", "статическая нагрузка мышц груди", 1001));
        ExerciseList.add(new StrengthExercise("Отжимания", "упражнение для верхней части тела. Выполняется, когда лицо опущено вниз, и руки отталкивают тело от земли.", 1002));
        ExerciseList.add(new StrengthExercise("Приседания", "упражнение для ног и ягодиц, выполняется, опускаясь в положение, как будто вы садитесь на стул, а затем поднимаетесь.", 1003));
        ExerciseList.add(new CardioExercise("Бег на месте", "кардиоупражнение, которое можно выполнять дома, бегая на месте.", 2001));
        ExerciseList.add(new StrengthExercise("Подтягивания", "упражнение для верхней части тела, выполняется подтягиванием тела вверх, держась за перекладину.", 1004));
        ExerciseList.add(new FlexibleExercise("Махи ногами", "упражнение для ног и ягодиц, выполняется махая ногой вперед и назад.", 3002));
        ExerciseList.add(new FlexibleExercise("Лодка", "упражнение для спины и пресса, выполняется, лежа на животе и поднимая туловище и ноги от пола, формируя форму лодки.", 3003));
        ExerciseList.add(new CardioExercise("Прыжки на скакалке", "кардиоупражнение, которое также тренирует координацию, выполняется с прыжками через скакалку.", 2002));
    }
	
	
	public void processUpdate(Update update) {
		if (update == null) {
			log.error("Received update is null");
            return;
		}
		
		if (update.hasMessage()) {
			defineCommand(update.getMessage());
		} else {
			log.error("Unsupported message type is received: " + update);
		}
	}
	
	private void describeExercise(String command, Message msg)
	{
		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId().toString());
		for (Exercise exercise : ExerciseList) {
	        if (exercise.getExerciseId().toString().equals(command)) {
	        	response.setText(exercise.getName() + "\n" + exercise.getDescription());
	        }
		}
		
		bot.sendAnswerMessage(response);
		log.debug(msg.getText());
	}
	
	private void defineCommand(Message msg) {
        
		String command = msg.getText();
		
		if (command.contains("/start ")) {
			command = command.replaceAll("/start ", "");
			describeExercise(command, msg);
		}
		else {
			switch(command)
			{
			case "/start":
				registerUser(msg);
				break;
			case "/tren":
				viewExercises(msg);
				break;
			case "/test":
				testOutput(msg);
				break;
			case "/stat":
				viewStat();
				break;
			}
		}
		
	}
	
	public void registerUser(Message msg) {
		//тут должна быть функция, проверяющая зарегестрирован ли уже пользователь
		//если нет -> позволить зарегестрировваться
		//если да -> вывести сообщение настройки даты тренировок/возможность начать тренировку прямо сейчас

	}
	
	public void viewExercises(Message msg) {
		//тут должна быть функция, выводящая доступные упражнения
		//пользователь должен иметь возможность тыкать на них прямо в сообщении, чтоб узнать о них подробнее/добавить упражнение в тренировку
		  SendMessage sendMessage = new SendMessage();
		  String helpStr = new String();
		  sendMessage.setChatId(msg.getChatId());
		  for (Exercise exercise : ExerciseList) {
			  helpStr += "<a href='" + "https://t.me/FitTrackDomovonokBot?start=" + exercise.getExerciseId().toString() + "'>" + exercise.getName() + "</a>\n";
		  }
		  //sendMessage.setText("<a href='" + "https://t.me/FitTrackDomovonokBot?start=" + "'>URL</a>\n");
		  sendMessage.setText(helpStr);
		  sendMessage.enableHtml(true);
		  bot.sendAnswerMessage(sendMessage);
		  
	}
	
	public void testOutput(Message msg) {
		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId().toString());
		response.setText("Тестовое сообщение от бота");
		bot.sendAnswerMessage(response);
		log.debug(msg.getText());
	}
	
	public void viewStat() {
		//тут должна быть функция, что ищет в базе данных Id чата 
		//и по этому id выводит статистику пользователя из БД
	}
	
}
