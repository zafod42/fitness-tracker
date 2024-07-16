package project.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.io.*;

//import jdk.javadoc.internal.doclets.formats.html.markup.Text;
import project.model.*;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.springframework.jdbc.core.RowMapper;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class UpdateController {

	private FitnessBot bot;
	private Map<Long, OrdinaryExercise> activeOrdinaryExercises = new HashMap<>();
	private Map<Long, TimeExercise> activeTimeExercises = new HashMap<>();
	private Map<Long, WeightExercise> activeWeightExercises = new HashMap<>();


	private CustomExercise newExercise;
	private StateMachine stateMachine = StateMachine.Listen;

	private static final Logger log = Logger.getLogger(FitnessBot.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

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
		}
		else if (update.hasCallbackQuery()) {
			String callbackData = update.getCallbackQuery().getData();

			if (callbackData.contains("YES_BUTTON")) {
				log.debug(callbackData);
				if (stateMachine.equals(StateMachine.Listen)) {
					addExerciseToTraining(update, callbackData);
				}
				else if (stateMachine.equals(StateMachine.DeleteExercise)) {
					//deleteExercise(update, callbackData);
				}
			}
			else if (callbackData.contains("NO_BUTTON")) {
				DeleteMessage deleteMessage = new DeleteMessage();
				deleteMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
				deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
				try {
					bot.execute(deleteMessage);
				} catch (TelegramApiException e) {
					log.error("Cannot delete message: " + update.getCallbackQuery().getData());
				}
			}
			else {
				log.error("Unsupported message type is received: " + update.getCallbackQuery().getData());
			}
		}
	}

	// Список команд, с помощью которых можно взаимодействовать с ботом
	private void defineCommand(Message msg) {
		String command = msg.getText();

		switch(stateMachine) {
			case Delete: {
                if (command.equals("ПОДТВЕРДИТЬ УДАЛЕНИЕ")) {
					deleteUser(msg);
					stateMachine = StateMachine.Listen;
				} else if (command.equalsIgnoreCase("отмена")) {
					stateMachine = StateMachine.Listen;
					SendMessage response = new SendMessage();
					response.setChatId(msg.getChatId().toString());
					response.setText("Удаление профиля отменено.");
					bot.sendAnswerMessage(response);
				} else {
                    SendMessage response = new SendMessage();
                    response.setChatId(msg.getChatId().toString());
                    response.setText("Бот ожидает подтверждения. Напишите \"Отмена\" для отмены удаления.");
                    bot.sendAnswerMessage(response);
                }
				break;
			}
			case DeleteExercise: {
				deleteExercise(msg); // sets stateMachine
				stateMachine = StateMachine.Listen;
				break;
			}
			case GetReps: {
				getRepsDialog(msg);
				stateMachine = StateMachine.Create; // just for fun
				createExercise(msg);
				stateMachine = StateMachine.Listen;
				break;
			}
			case GetSets: {
				getSetsDialog(msg);
				stateMachine = StateMachine.GetReps;
				break;
			}
			case GetName: {
				getNameDialog(msg);
				stateMachine = StateMachine.GetDescr;
				break;
			}
			case GetDescr: {
				getDescriptionDialog(msg);
				stateMachine = StateMachine.GetSets;
				break;
			}
			case UpdateExercise: {
				updateExercise(msg);
				stateMachine = StateMachine.Listen;
				break;
			}
			case Listen:
			case Command: {
				if (command.startsWith("/start ")) {
					command = command.replaceAll("/start ", "");
					describeExercise(command, msg);
				} else if (command.startsWith("/start_exercise")) {
					startExercise(command, msg);
				} else {
					switch (command) {
						case "/delete_exercise": {
							deleteExerciseDialog(msg); // sets StateMachine
							break;
						}
						case "/update_exercise": {
							updateExerciseDialog(msg); // sets StateMachine
							break;
						}
						case "/create_exercise":
							newExercise = new CustomExercise();
							stateMachine = StateMachine.GetName;
							getNameResponse(msg);
							break;
						case "/start":
							sendWelcomeMessage(msg);
							break;
						case "/register":
							registerUser(msg);
							break;
						case "/tren":
							viewExercises(msg);
							break;
						case "/delete_user":
							confirmAccountDeletion(msg);
							stateMachine = StateMachine.Delete;
							break;
						case "/stop_exercise":
							stopExercise(msg);
							break;
						case "/finish_set":
							finishSet(msg);
							break;
						case "/toggle_notification":
							toggleNotification(msg);
							break;

						default:
							SendMessage response = new SendMessage();
							response.setChatId(msg.getChatId().toString());
							response.setText("Неизвестная команда. Попробуйте снова.");
							bot.sendAnswerMessage(response);
							break;
					}
				}
				break;
			}
		}
	}

	private void updateExercise(Message msg) {
		Long chatId = msg.getChatId();
		SendMessage response = new SendMessage();
		response.setChatId(chatId);
		System.out.println("Метод deleteExercise (/update id");
		String[] text = msg.getText().split(" ");
		Integer exerciseId;
		if(text.length > 1) {
			exerciseId = Integer.parseInt(msg.getText().split(" ")[1]);
		}
		else {
			return;
		}
		log.debug(msg.getText());

		String sql = "UPDATE customExercise SET description = ? WHERE id = ?";
		jdbcTemplate.update(sql, msg.getText(), exerciseId);

		bot.getExercises().update();

		response.setText("Упражнение удалено");
		bot.sendAnswerMessage(response);
	}

	private void updateExerciseDialog(Message msg) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(msg.getChatId());
		StringBuilder msgText = new StringBuilder();
		Boolean isEmpty = Boolean.TRUE;
		msgText.append("Выберите упражнение, которое вы хотите изменить.\n");
		for (HashMap.Entry<Integer, Exercise> entry : bot.getExercises().getExerciseMap().entrySet()) {
			if(entry.getKey() < 1000) {
				isEmpty = Boolean.FALSE;
				msgText.append("<a href='");
				msgText.append(bot.getUrl());
				msgText.append("?start=");
				msgText.append(entry.getKey().toString());
				msgText.append("'>");
				msgText.append(entry.getValue().getName());
				msgText.append("</a>\n");
			}
		}
		//botUrl + "?start=" +
		if (isEmpty){
			String text = "Нет упражнений, которые можно изменить.";
			sendMessage.setText(text);
			bot.sendAnswerMessage(sendMessage);
			stateMachine = StateMachine.Listen;
		}
		else {
			sendMessage.setText(String.valueOf(msgText));
			sendMessage.enableHtml(true);
			bot.sendAnswerMessage(sendMessage);
			log.debug(msg.getText());
			log.debug(String.valueOf(msgText));
			stateMachine = StateMachine.UpdateExercise;
		}
	}

	private void deleteExerciseDialog(Message msg) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(msg.getChatId());
		StringBuilder msgText = new StringBuilder();
		Boolean isEmpty = Boolean.TRUE;
		msgText.append("Выберите упражнение, которое вы хотите удалить.\n");
		for (HashMap.Entry<Integer, Exercise> entry : bot.getExercises().getExerciseMap().entrySet()) {
			if(entry.getKey() < 1000) {
				isEmpty = Boolean.FALSE;
				msgText.append("<a href='");
				msgText.append(bot.getUrl());
				msgText.append("?start=");
				msgText.append(entry.getKey().toString());
				msgText.append("'>");
				msgText.append(entry.getValue().getName());
				msgText.append("</a>\n");
			}
		}
		//botUrl + "?start=" +
		if (isEmpty){
			String text = "Нет упражнений, которые можно удалить.";
			sendMessage.setText(text);
			bot.sendAnswerMessage(sendMessage);
			stateMachine = StateMachine.Listen;
		}
		else {
			sendMessage.setText(String.valueOf(msgText));
			sendMessage.enableHtml(true);
			bot.sendAnswerMessage(sendMessage);
			log.debug(msg.getText());
			log.debug(String.valueOf(msgText));
			stateMachine = StateMachine.DeleteExercise;
		}
	}

	private void createExercise(Message msg) {

		// sql запрос INSERT (C из CRUD)

		String sql = "INSERT INTO customexercise (name, description, sets, repetitions, type) VALUES (?, ?, ?, ?, 'custom')";
		jdbcTemplate.update(sql, newExercise.getName(), newExercise.getDescription(), newExercise.getSets(), newExercise.getRepetitions());

		// Update exercises table
		bot.getExercises().update();

		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId());
		response.setText("Ваше упражнение создано!");
		bot.sendAnswerMessage(response);
	}

	private void getRepsDialog(Message msg) {
		String reps = msg.getText();
		newExercise.setRepetitions(Integer.parseInt(reps));
	}

	private void getSetsDialog(Message msg) {
        String sets = msg.getText();
		newExercise.setSets(Integer.parseInt(sets));

		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId());
		response.setText("Сколько повторений у вашего упражнения? Введите число.");
		bot.sendAnswerMessage(response);
	}

	private void getDescriptionDialog(Message msg) {

		String description = msg.getText();
		newExercise.setDescription(description);

		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId());
		response.setText("Сколько подходов у вашего упражнения? Введите число.");
		bot.sendAnswerMessage(response);
	}

	private void getNameDialog(Message msg) {
		String exerciseName = msg.getText();
		newExercise.setName(exerciseName);
		// id autogenerated
		//String sql = "INSERT INTO customexercise (name, type) VALUES (array[?], array['custom'])";
		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId());
		response.setText("Введите описание своего упражнения. В него могут входить подробности того как следует его выполнять" +
				" или необходимый инвентарь.");
		bot.sendAnswerMessage(response);
	}

	private void getNameResponse(Message msg) {
		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId());
		response.setText("Введите название упражнения, которое вы хотите добавить.");
		bot.sendAnswerMessage(response);
	}

	private void sendWelcomeMessage(Message msg) {
		Long chatId = msg.getChatId();

		SendMessage response = new SendMessage();
		response.setChatId(chatId);

		List<Long> userIds = jdbcTemplate.queryForList("SELECT id FROM users WHERE chat_id =?", new Object[]{chatId}, Long.class);

		if (userIds.isEmpty()) {
			response.setText("Привет! Я бот для контроля за спортивными результатами в тренировках. " +
					"Отправьте команду /tren, чтобы посмотреть доступные упражнения.");
			bot.sendAnswerMessage(response);
		}
		else {
			String text;
			text = "Хотите начать тренировку?\nВаша тренировка состоит из:\n";
			//Integer[] exerciseIds = jdbcTemplate.queryForObject("SELECT array_agg(exercises) FROM users WHERE chat_id =?", Integer[].class, chatId);
			List<Integer> exerciseIds = new ArrayList<>();
			String sql = "SELECT exercises FROM users WHERE chat_id = ?";
			try {
				exerciseIds = jdbcTemplate.queryForObject(sql, new Object[]{chatId}, new RowMapper<List<Integer>>() {
					@Override
					public List<Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
						Integer[] exercises = (Integer[]) rs.getArray("exercises").getArray();
						return Arrays.asList(exercises);
					}
				});
			} catch (NullPointerException e)
			{
				log.error(e);
				response.setText("Ваша тренировка пуста. Отправьте команду /tren, чтобы посмотреть доступные упражнения.");
				bot.sendAnswerMessage(response);
				return;
			}
			//Integer[] exerciseIds = exerciseIdsList.toArray(new Integer[0]);
            assert exerciseIds != null;
            for (Integer Id: exerciseIds) {
				text += bot.getExercises().getExerciseMap().get(Id).getName() + "\n";
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

	private void viewExercises(Message msg) {
		SendMessage sendMessage = new SendMessage();
		StringBuilder helpStr = new StringBuilder();
		sendMessage.setChatId(msg.getChatId());
		String botUrl = bot.getUrl();
		for (HashMap.Entry<Integer, Exercise> entry : bot.getExercises().getExerciseMap().entrySet()) {
			helpStr.append("<a href='");
			helpStr.append(botUrl);
			helpStr.append("?start=");
			helpStr.append(entry.getKey().toString());
			helpStr.append("'>");
			helpStr.append(entry.getValue().getName());
			helpStr.append("</a>\n");
		}
	//botUrl + "?start=" +
		sendMessage.setText(String.valueOf(helpStr));
		sendMessage.enableHtml(true);
		bot.sendAnswerMessage(sendMessage);
		log.debug(msg.getText());
		log.debug(String.valueOf(helpStr));
	}
	private void describeExercise(String command, Message msg) {
		SendMessage response = new SendMessage();
		String text;
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
		}
		catch (TelegramApiException e) {
			log.error("Cannot delete message: " + msg);
		}

		bot.sendAnswerMessage(response);
	}


	// Регистрация пользователя - заносит человека в базу данных
	public void registerUser(Message msg) {
		System.out.println("Метод registerUser (/register) вызван");
		log.debug(msg.getText());

		String chatIdStr = msg.getChatId().toString();
		Long chatId = Long.parseLong(chatIdStr);

		List<Long> userIds = jdbcTemplate.queryForList("SELECT id FROM users WHERE chat_id =?",
				new Object[]{chatId}, Long.class);

		if (userIds.isEmpty()) {
			jdbcTemplate.update("INSERT INTO users (chat_id) VALUES (?)", chatId);
			SendMessage response = new SendMessage();
			response.setChatId(chatIdStr);
			response.setText("Регистрация прошла успешно!");
			bot.sendAnswerMessage(response);
		}
		else {
			SendMessage response = new SendMessage();
			response.setChatId(chatIdStr);
			response.setText("Вы уже зарегистрированы!");
			bot.sendAnswerMessage(response);
		}
	}

	// Потверждение удаления (удаляет аккаунт только после ввода кодовой фразы)
	private void confirmAccountDeletion(Message msg) {
		System.out.println("Метод confirmAccountDeletion вызван");
		log.debug(msg.getText());

		SendMessage response = new SendMessage();
		response.setChatId(msg.getChatId().toString());
		response.setText("Вы уверены, что хотите удалить свой аккаунт? " +
				"Отправьте 'ПОДТВЕРДИТЬ УДАЛЕНИЕ' для подтверждения.");
		bot.sendAnswerMessage(response);
	}

	private void deleteUser(Message msg) {
		System.out.println("Метод deleteUser (/delete) вызван");
		log.debug(msg.getText());

		String chatIdStr = msg.getChatId().toString();
		Long chatId = Long.parseLong(chatIdStr);
		jdbcTemplate.update("DELETE FROM users WHERE chat_id =?", chatId);
		SendMessage response = new SendMessage();
		response.setChatId(chatIdStr);
		response.setText("Ваш аккаунт был успешно удалён.");
		bot.sendAnswerMessage(response);
	}

	// Начать упражнение из заготовленного списка
	private void startExercise(String command, Message msg) {
		System.out.println("Метод startExercise (/startExercise [id упражнения]) вызван");
		log.debug(msg.getText());
		try {
			String[] parts = command.split(" ");
			if (parts.length!= 2) {
				throw new NumberFormatException("Invalid command format");
			}
			int exerciseId = Integer.parseInt(parts[1]);
			Exercise startedExercise = bot.getExercises().getExerciseMap().get(exerciseId);
			if (startedExercise!= null) {
				if (startedExercise instanceof OrdinaryExercise) {
					activeOrdinaryExercises.put(msg.getChatId(), (OrdinaryExercise) startedExercise);
					System.out.println("Обычное упражнение добавлено в activeExercises для чата: " + msg.getChatId());
					((OrdinaryExercise) startedExercise).startExercise(msg.getChatId(), bot);
				}
				else if (startedExercise instanceof TimeExercise) {
					activeTimeExercises.put(msg.getChatId(), (TimeExercise) startedExercise);
					System.out.println("Упражнение на время добавлено в activeTimeExercises для чата: " + msg.getChatId());
					((TimeExercise) startedExercise).startExercise(msg.getChatId(), bot);
				}
				else if (startedExercise instanceof WeightExercise) {
					activeWeightExercises.put(msg.getChatId(), (WeightExercise) startedExercise);
					System.out.println("Упражнение с весом добавлено в activeWeightExercises для чата: " + msg.getChatId());
					((WeightExercise) startedExercise).startExercise(msg.getChatId(), bot);
				}
			}
			else {
				throw new IllegalArgumentException("Exercise not found");
			}
		}
		catch (NumberFormatException e) {
			log.error("Invalid exercise ID format: " + e.getMessage());
			SendMessage response = new SendMessage();
			response.setChatId(msg.getChatId().toString());
			response.setText("Неверный формат команды. Используйте: /startExercise <ID упражнения>");
			bot.sendAnswerMessage(response);
		}
		catch (IllegalArgumentException e) {
			log.error("Error starting exercise: " + e.getMessage());
			SendMessage response = new SendMessage();
			response.setChatId(msg.getChatId().toString());
			response.setText("Упражнение не найдено. Проверьте ID и попробуйте снова.");
			bot.sendAnswerMessage(response);
		}
	}

	// Завершить текущий подход выполняемого упражнения
	private void finishSet(Message msg) {
		System.out.println("Метод finishSet (/finishSet) вызван");
		log.debug(msg.getText());

		long chatId = msg.getChatId();
		OrdinaryExercise ordinaryExercise = activeOrdinaryExercises.get(chatId);
		WeightExercise weightExercise = activeWeightExercises.get(chatId);
		SendMessage response = new SendMessage();

		if (ordinaryExercise!= null) {
			System.out.println("Текущий подход для обычного упражнения из чата " + chatId + " завершено");
			ordinaryExercise.finishSet(chatId, bot);
		}
		else if (weightExercise!= null) {
			System.out.println("Текущий подход для упражнения с весом из чата " + chatId + " завершено");
			weightExercise.finishSet(chatId, bot);
		}
		else {
			response.setText("Упражнение не запущено.");
			System.out.println("Активное упражнение из чата " + chatId + " не найдено");
			bot.sendAnswerMessage(response);
		}
	}

	// Досрочно завершить упражнение
	private void stopExercise(Message msg) {
		System.out.println("Метод stopExercise (/stop) вызван");
		log.debug(msg.getText());

		long chatId = msg.getChatId();
		OrdinaryExercise ordinaryExercise = activeOrdinaryExercises.remove(chatId);
		WeightExercise weightExercise = activeWeightExercises.remove(chatId);
		TimeExercise timeExercise = activeTimeExercises.remove(chatId);

		if (ordinaryExercise != null) {
			ordinaryExercise.stopExercise(chatId, bot);
			System.out.println("Обычное упражнение остановлено и удалено из activeOrdinaryExercises для чата: " + chatId);
		}
		else if (weightExercise != null) {
			weightExercise.stopExercise(chatId, bot);
			System.out.println("Упражнение с весом остановлено и удалено из activeWeightExercises для чата: " + chatId);
		}
		else if (timeExercise != null) {
			timeExercise.stopExercise(chatId, bot);
			System.out.println("Упражнение с на время остановлено и удалено из activeWeightExercises для чата: " + chatId);
		}
		else {
			System.out.println("Активное упражнение из чата: " + chatId + " не найдено");
		}
	}

	// export to CSV
	private void exportToCSV(Long chatId, List<Map<String, Object>> results) {
		String fileName = "Статистика_" + chatId + ".csv";
		try (FileWriter writer = new FileWriter(fileName)) {
			writer.write("Упражнение ID,Информация\n");
			for (Map<String, Object> row : results) {
				Integer[] exercises = (Integer[]) row.get("exercises");
				String info = (String) row.get("info");

				for (Integer exerciseId : exercises) {
					writer.write(exerciseId + "," + info + "\n");
				}
			}
			System.out.println("Статистика экспортирован в CSV успешно!");
		} catch (IOException e) {
			log.error("Произошла ошибка при экспорте статистики в CSV: " + e.getMessage());
		}
	}

	// export to XLS
	private void exportToXLS(Long chatId, List<Map<String, Object>> results) throws IOException {
		String fileName = "Статистика_" + chatId + ".xlsx";
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Статистика");

		Row headerRow = sheet.createRow(0);
		Cell headerCell = headerRow.createCell(0);
		headerCell.setCellValue("Упражнение ID");
		headerCell = headerRow.createCell(1);
		headerCell.setCellValue("Информация");

		int rowNum = 1;
		for (Map<String, Object> row : results) {
			Integer[] exercises = (Integer[]) row.get("exercises");
			String info = (String) row.get("info");

			Row dataRow = sheet.createRow(rowNum++);
			for (int i = 0; i < exercises.length; i++) {
				Cell cell = dataRow.createCell(i);
				cell.setCellValue(exercises[i]);
			}
			dataRow.getCell(dataRow.getLastCellNum() - 1).setCellValue(info);
		}

		try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
			workbook.write(outputStream);
		} finally {
			workbook.close();
		}

		System.out.println("Статистика экспортирована в XLS успешно!");
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
			}
			catch (NullPointerException e) {
				exercise = null;
				isExerciseInTraining = false;
			}

			if (exercise == null) {
				log.error("Exercise " + exerciseId.toString() + " not found");
			}
			else if (isExerciseInTraining) {
				response.setText("Это упражнение уже присутствует в вашей тренировке.");
				bot.sendAnswerMessage(response);
			}
			else {
				jdbcTemplate.update("UPDATE users SET exercises = array_append(exercises, ?) WHERE chat_id = ?", exerciseId, chatId);
				response.setText("Теперь это упражнение в вашей тренировке.");
				bot.sendAnswerMessage(response);
			}
		}
		else {
			response.setText("Зарегестрируйтесь (/register), чтобы добавлять упражнения.");
			response.enableHtml(true);
			bot.sendAnswerMessage(response);
		}
	}

	// CRUD Region

	private void deleteExercise(Message msg)
	{
		Long chatId = msg.getChatId();
		SendMessage response = new SendMessage();
		response.setChatId(chatId);
		System.out.println("Метод deleteExercise (/delete id");
		String[] text = msg.getText().split(" ");
		Integer exerciseId;
		if(text.length > 1) {
			exerciseId = Integer.parseInt(msg.getText().split(" ")[1]);
		}
		else {
			return;
		}
		log.debug(msg.getText());

		String sql = "DELETE FROM customExercise WHERE id = ?";
		jdbcTemplate.update(sql, exerciseId);

		bot.getExercises().update();

		response.setText("Упражнение удалено");
		bot.sendAnswerMessage(response);

	}

	private void toggleNotification(Message msg)
	{
		String getFlagQuery = "select notify from users where chat_id = ?;";
		String setFlagQuery = "update users set notify = ?;";
		Long chatId = msg.getChatId();
		Boolean notifyFlag;

		String text = "Ваши напоминания были включены.";

        notifyFlag = jdbcTemplate.queryForObject(getFlagQuery, new Object[]{chatId}, Boolean.class);
		jdbcTemplate.update(setFlagQuery, new Object[]{Boolean.FALSE.equals(notifyFlag)});

		SendMessage response = new SendMessage();
		response.setChatId(chatId.toString());
		if(Boolean.TRUE.equals(notifyFlag))
		{
			text = "Ваши напоминания были отключены.";
		}
		response.setText(text);
		bot.sendAnswerMessage(response);
    }


}

