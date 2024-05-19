package project.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.model.Exercise;

@Component
public class UpdateController {
	private FitnessBot bot;
	private static final Logger log = org.apache.log4j.Logger.getLogger(FitnessBot.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	//Used to connect bot and controller
	public void registerBot(FitnessBot bot) {
		this.bot = bot;
	}

	//This method is called every time when bot gets answer from the user
	public void processUpdate(Update update) {
		if (update == null) {
			log.error("Received update is null");
			return;
		}

		if (update.hasMessage()) {
			defineCommand(update.getMessage());
		} else if (update.hasCallbackQuery()) {
			String callbackData = update.getCallbackQuery().getData();

			if (callbackData.contains("YES_BUTTON")) {
				log.debug(callbackData.toString());
				addExerciseToTraining(update, callbackData);
			} else if (callbackData.contains("NO_BUTTON")) {
				DeleteMessage deleteMessage = new DeleteMessage();
				deleteMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
				deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
				try {
					bot.execute(deleteMessage);
				} catch (TelegramApiException e) {
					log.error("Cannot delete message: " + update.getCallbackQuery().getData());
				}
			} else {
				log.error("Unsupported message type is received: " + update.getCallbackQuery().getData());
			}
		}
	}

	//Choose command from the pool
	private void defineCommand(Message msg) {
		String command = msg.getText();
		log.debug(command);

		if (command.contains("/start ")) {
			command = command.replaceAll("/start ", "");
			describeExercise(command, msg);
		} else {
			switch (command) {
				case "/start":
					sendWelcomeMessage(msg);
					break;
				case "/register":
					registerUser(msg);
					break;
				case "/tren":
					viewExercises(msg);
					break;
				case "/test":
					sendWelcomeMessage(msg);
					break;
				case "/stat":
					viewStat(msg);
					break;
				case "/delete":
					confirmAccountDeletion(msg);
					break;
				case "ПОДТВЕРЖДАЮ УДАЛЕНИЕ":
					deleteUser(msg);
					break;
			}
		}
	}
	//commands:
	// /start implementation
	private void sendWelcomeMessage(Message msg) {
		Long chatId = msg.getChatId();

		SendMessage response = new SendMessage();
		response.setChatId(chatId);

		List<Long> userIds = jdbcTemplate.queryForList("SELECT id FROM users WHERE chat_id =?", new Object[]{chatId}, Long.class);

		if (userIds.isEmpty()) {
			response.setText("Привет! Я бот для контроля за спортивными результатами в тренировках. " +
					"Отправьте команду /tren, чтобы посмотреть доступные упражнения.");
			bot.sendAnswerMessage(response);
		} else {
			String text = new String();
			text = "Хотите начать тренировку?\nВаша тренировка состоит из:\n";
			//Integer[] exerciseIds = jdbcTemplate.queryForObject("SELECT array_agg(exercises) FROM users WHERE chat_id =?", Integer[].class, chatId);

			String sql = "SELECT exercises FROM users WHERE chat_id = ?";

			List<Integer> exerciseIds = jdbcTemplate.queryForObject(sql, new Object[]{chatId}, new RowMapper<List<Integer>>() {
				@Override
				public List<Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
					Integer[] exercises = (Integer[]) rs.getArray("exercises").getArray();
					return Arrays.asList(exercises);
				}
			});

			//Integer[] exerciseIds = exerciseIdsList.toArray(new Integer[0]);
			for (Integer Id: exerciseIds) {
				text += bot.getExercises().getExerciseMap().get(Id).getName().toString() + "\n";
			}
			response.setText(text);

			InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
			List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
			InlineKeyboardButton startButton = new InlineKeyboardButton();

			startButton.setText("Начать сейчас");
			startButton.setCallbackData("START_BUTTON");

			InlineKeyboardButton timeButton = new InlineKeyboardButton();
			timeButton.setText("Начать в заданное время");
			timeButton.setCallbackData("TIME_BUTTON");

			InlineKeyboardButton noButton = new InlineKeyboardButton();

			noButton.setText("Отмена");
			noButton.setCallbackData("NO_BUTTON");

			rowsInLine.add(new ArrayList<InlineKeyboardButton>(Collections.singletonList(startButton)));
			rowsInLine.add(new ArrayList<InlineKeyboardButton>(Collections.singletonList(timeButton)));
			rowsInLine.add(new ArrayList<InlineKeyboardButton>(Collections.singletonList(noButton)));

			markupInLine.setKeyboard(rowsInLine);
			response.setReplyMarkup(markupInLine);
			bot.sendAnswerMessage(response);
		}

	}

	// /register implementation
	private void registerUser(Message msg) {
		String chatIdStr = msg.getChatId().toString();
		Long chatId = Long.parseLong(chatIdStr);

		List<Long> userIds = jdbcTemplate.queryForList("SELECT id FROM users WHERE chat_id =?",
				new Object[]{chatId}, Long.class);

		if (userIds.isEmpty()) {
			jdbcTemplate.update("INSERT INTO users (chat_id) VALUES (?)", chatId);
			SendMessage response = new SendMessage();
			response.setChatId(chatIdStr);
			//response.setText("Регистрация прошла успешно!");
			response.setText("Счастилового путешествия в Казахстан!");
			bot.sendAnswerMessage(response);

		}
		else {
			SendMessage response = new SendMessage();
			response.setChatId(chatIdStr);
			//response.setText("Вы уже зарегистрированы!");
			response.setText("АMOGUS!");
			bot.sendAnswerMessage(response);
		}
	}

	// /tren implementation
	private void viewExercises(Message msg) {
		SendMessage sendMessage = new SendMessage();
		String helpStr = new String();
		sendMessage.setChatId(msg.getChatId());
		for (HashMap.Entry<Integer, Exercise> entry : bot.getExercises().getExerciseMap().entrySet()) {
			helpStr += "<a href='" + "https://t.me/FitTrackDomovonokBot?start=" + entry.getKey().toString() + "'>"
					+ entry.getValue().getName() + "</a>\n";
		}
		sendMessage.setText(helpStr);
		sendMessage.enableHtml(true);
		bot.sendAnswerMessage(sendMessage);
		log.debug(msg.getText());
	}
	private void describeExercise(String command, Message msg) {
		SendMessage response = new SendMessage();
		String text = new String();
		text = bot.getExercises().getExerciseMap().get(Integer.valueOf(command)).getName() + "\n";
		text += bot.getExercises().getExerciseMap().get(Integer.valueOf(command)).getDescription() + "\n\nДобавить упражнение в вашу тренировку?";
		response.setChatId(msg.getChatId().toString());
		response.setText(text);

		InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
		List<InlineKeyboardButton> rowInLine = new ArrayList<>();
		InlineKeyboardButton yesButton = new InlineKeyboardButton();

		yesButton.setText("Да");
		yesButton.setCallbackData("YES_BUTTON" + command);

		InlineKeyboardButton noButton = new InlineKeyboardButton();

		noButton.setText("Нет");
		noButton.setCallbackData("NO_BUTTON");

		rowInLine.add(yesButton);
		rowInLine.add(noButton);

		rowsInLine.add(rowInLine);

		markupInLine.setKeyboard(rowsInLine);
		response.setReplyMarkup(markupInLine);

		DeleteMessage deleteMessage = new DeleteMessage();
		deleteMessage.setChatId(msg.getChatId());
		deleteMessage.setMessageId(msg.getMessageId());
		try {
			bot.execute(deleteMessage);
		} catch (TelegramApiException e) {
			log.error("Cannot delete message: " + msg);
		}
		bot.sendAnswerMessage(response);
	}

	// /stat implementation
	private void viewStat(Message msg) {
		String chatIdStr = msg.getChatId().toString();
		Long chatId = Long.parseLong(chatIdStr);

		String sql = "SELECT exercises, info FROM users WHERE chat_id =?";

		try {
			List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, chatId);

			if (!results.isEmpty()) {
				Map<String, Object> result = results.get(0);
				Integer[] exercises = (Integer[]) result.get("exercises");
				String info = (String) result.get("info");

				StringBuilder statMessage = new StringBuilder("Ваша статистика:\n");
				statMessage.append("Информация: ").append(info).append("\n");
				statMessage.append("Выполненные упражнения: \n");

				for (Integer exerciseId : exercises) {
					// Здесь можно добавить дополнительный запрос для получения названия упражнения по ID
					// Для примера просто выводим ID упражнения
					statMessage.append("- Упражнение ID: ").append(exerciseId).append("\n");
				}

				SendMessage response = new SendMessage();
				response.setChatId(chatIdStr);
				response.setText(statMessage.toString());
				bot.sendAnswerMessage(response);
			}
			else {
				SendMessage response = new SendMessage();
				response.setChatId(chatIdStr);
				response.setText("Статистика не найдена.");
				bot.sendAnswerMessage(response);
			}
		}
		catch (Exception e) {
			log.error("Ошибка при получении статистики пользователя: " + e.getMessage());
			SendMessage response = new SendMessage();
			response.setChatId(chatIdStr);
			response.setText("Произошла ошибка при получении статистики.");
			bot.sendAnswerMessage(response);
		}
	}

	//YES_BUTTON implementation
	private void addExerciseToTraining(Update update, String callbackData) {
		Long chatId = update.getCallbackQuery().getMessage().getChatId();

		SendMessage response = new SendMessage();
		response.setChatId(chatId);

		List<Long> userIds = jdbcTemplate.queryForList("SELECT id FROM users WHERE chat_id =?", new Object[]{chatId}, Long.class);

		if (!userIds.isEmpty()) {
			Integer exerciseId = Integer.valueOf(callbackData.substring(callbackData.length()-4));
			Exercise exercise;
			Boolean isExerciseInTraining;

			try {
				exercise = bot.getExercises().getExerciseMap().get(exerciseId);
				isExerciseInTraining = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE chat_id = ? AND ? = ANY(exercises)", Boolean.class, chatId, exerciseId);
			} catch (NullPointerException e) {
				exercise = null;
				isExerciseInTraining = false;
			}

			if (exercise == null) {
				log.error("Exercise " + exerciseId.toString() + " not found");
			} else if (isExerciseInTraining) {
				response.setText("Это упражнение уже присутствует в вашей тренировке.");
				bot.sendAnswerMessage(response);
			} else {
				jdbcTemplate.update("UPDATE users SET exercises = array_append(exercises, ?) WHERE chat_id = ?", exerciseId, chatId);
				response.setText("Теперь это упражнение в вашей тренировке.");
				bot.sendAnswerMessage(response);
			}
		} else {
			response.setText("Зарегестрируйтесь (/register), чтобы добавлять упражнения.");
			response.enableHtml(true);
			bot.sendAnswerMessage(response);
		}
	}

	// /delete implementation
	private void deleteUser(Message msg) {
		String chatIdStr = msg.getChatId().toString();
		Long chatId = Long.parseLong(chatIdStr);
		jdbcTemplate.update("DELETE FROM users WHERE chat_id =?", chatId);
		SendMessage response = new SendMessage();
		response.setChatId(chatIdStr);
		//response.setText("Ваш аккаунт был успешно удалён.");
		response.setText("Попутного ветра!");
		bot.sendAnswerMessage(response);
	}

	//"ПОДТВЕРЖДАЮ УДАЛЕНИЕ"
	private void confirmAccountDeletion(Message msg) {
		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId().toString());
		response.setText("Вы уверены, что хотите удалить свой аккаунт? Отправьте 'ПОДТВЕРЖДАЮ УДАЛЕНИЕ' для подтверждения.");
		bot.sendAnswerMessage(response);
	}
}
