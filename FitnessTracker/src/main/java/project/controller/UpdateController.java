package project.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import project.model.CardioExercise;
import project.model.Exercise;
import project.model.FlexibleExercise;
import project.model.StrengthExercise;

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
			defineCommand(update.getMessage());
		} else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            
            SendMessage response = new SendMessage();

            if(callbackData.equals("YES_BUTTON")){
            	response.setText("(Тестовое) Вы нажали \"да\"");
            	response.setChatId(update.getCallbackQuery().getMessage().getChatId());
                bot.sendAnswerMessage(response);
            }
            else if(callbackData.equals("NO_BUTTON")){
            	response.setChatId(update.getCallbackQuery().getMessage().getChatId());
            	response.setText("(Тестовое) Вы нажали \"нет\"");
                bot.sendAnswerMessage(response);
            } else {
			log.error("Unsupported message type is received: " + update);
            }
		}
	}
	
	private void describeExercise(String command, Message msg)
	{
		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId().toString());
		response.setText(bot.getExercises().getExerciseMap().get(Integer.valueOf(command)).getName() + "\n" + bot.getExercises().getExerciseMap().get(Integer.valueOf(command)).getDescription() + "\n\nДобавить упражнение в вашу тренировку?");
		
		InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();

        yesButton.setText("Да");
        yesButton.setCallbackData("YES_BUTTON");

        InlineKeyboardButton noButton = new InlineKeyboardButton();

        noButton.setText("Нет");
        noButton.setCallbackData("NO_BUTTON");

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        response.setReplyMarkup(markupInLine);
		
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

	public void viewExercises(Message msg) {
		  SendMessage sendMessage = new SendMessage();
		  String helpStr = new String();
		  sendMessage.setChatId(msg.getChatId());
		  for (HashMap.Entry<Integer, Exercise> entry : bot.getExercises().getExerciseMap().entrySet()) {
			  helpStr += "<a href='" + "https://t.me/FitTrackDomovonokBot?start=" + entry.getKey().toString() + "'>" + entry.getValue().getName() + "</a>\n";
		  }
		  sendMessage.setText(helpStr);
		  sendMessage.enableHtml(true);
		  bot.sendAnswerMessage(sendMessage);
		  log.debug(msg.getText());
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

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void registerUser(Message msg) {
		String chatId = msg.getChatId().toString();
		// Проверяем, зарегистрирован ли уже пользователь
		Integer userId;
		try {
			userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE chat_id = ?", new Object[]{chatId}, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			userId = null; // Пользователь не найден
		}

		if (userId == null) {
			// Пользователь не найден, регистрируем его
			jdbcTemplate.update("INSERT INTO users (chat_id) VALUES (?)", chatId);
			// Отправляем сообщение о регистрации
			SendMessage response = new SendMessage();
			response.setChatId(chatId);
			response.setText("Вы успешно зарегистрированы!");
			bot.sendAnswerMessage(response);
		} else {
			// Пользователь уже зарегистрирован, предлагаем начать тренировку
			SendMessage response = new SendMessage();
			response.setChatId(chatId);
			response.setText("Вы уже зарегистрированы!");
			bot.sendAnswerMessage(response);
		}
	}

}
