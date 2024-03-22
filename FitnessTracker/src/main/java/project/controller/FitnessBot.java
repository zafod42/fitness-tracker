package project.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;


@Component
public class FitnessBot extends TelegramLongPollingBot{
	@Value("${bot.name}")
	private String name = "FitTrackDomovonokBot";
	@Value("${bot.token}")
	private String token = "7033585733:AAGXgDOBCO3R9lz2XX1HVVGOWR_hcfThNds";
	private UpdateController controller;
	
	private static final Logger log = org.apache.log4j.Logger.getLogger(FitnessBot.class);
	
	public FitnessBot(UpdateController controller) {
		this.controller = controller;
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
		Message receivedMessage = update.getMessage();
		log.debug(receivedMessage.getText());
		
		SendMessage response = new SendMessage();
		response.setChatId(receivedMessage.getChatId().toString());
		response.setText("Тестовое сообщение от бота");
		sendAnswerMessage(response);
	}
	
    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }
	
}
