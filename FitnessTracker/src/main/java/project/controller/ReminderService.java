package project.controller;

// TODO:
// Система оповещений
// - Оповещения раз в день? [x]
// - Оповещения раз в неделю [ ]
// - Возможность отключения оповещений(новая команда)
// -   Возможно это будет сообщение с кнопочками типо оставить или нет
// можно просто чтобы было можно отключить оповещения
// или включить
// например как вариант две команды для переключения между двумя режимами
// - Жалобные оповещения
// - Счастливые оповещения

// Note:
// А как вообще слать оповещения? [x]
// Можно сделать это по расписанию в spring [x]? А можно самонастраиваемые?
// Как отключить это? [ ]
// Тупой вариант: Пропускать содержимое scheduled метода по флагу (что кстати и предлагают https://devmark.ru/article/spring-boot-scheduler)
//   \_ не тупой
// Флаг хранится в базе [ ]

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReminderService {
    @Autowired
    private FitnessBot bot;

    private static final Logger log = org.apache.log4j.Logger.getLogger(FitnessBot.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void registerBot(FitnessBot bot) {
        this.bot = bot;
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void remindEveryday()
    {
        List<Long> chatIDs = new ArrayList<>();
        List<Boolean> notifyFlag = new ArrayList<>();
        chatIDs = jdbcTemplate.queryForList("select chat_id from users;", Long.class);
        notifyFlag = jdbcTemplate.queryForList("select notify from users;", Boolean.class);
        log.debug(notifyFlag);
        for (int i = 0; i < chatIDs.size(); ++i)
        {
            if (!notifyFlag.get(i)) continue;
            bot.sendMsg(chatIDs.get(i).toString(), "Не забудьте сегодня потренироваться.");
        }
    }
}
