package org.andnekon.triggered_bot.bot.service.reply;

import java.io.File;
import java.util.List;

import org.andnekon.triggered_bot.bot.dao.ActionRepository;
import org.andnekon.triggered_bot.bot.model.Action;
import org.andnekon.triggered_bot.bot.service.resource.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class ReplyService {

    private final TelegramClient telegramClient;

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    ResourceService resourceService;

    Logger logger = LoggerFactory.getLogger(ReplyService.class);

    public ReplyService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
      * Use {@code telegramClient} to reply with error
      * @param chatId Chat identifier
      * @param error Error text
      */
    public void replyError(long chatId, String error) {
        logger.info("[chatId {}] {}", chatId, error);
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(String.format("error: %s", error))
            .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    /**
      * Use {@code telegramClient} to reply, depending on {@code ReplyType}
      * @param chatId Chat identifier
      * @param text Recieved text
      */
    public void reply(long chatId, String text) {
        List<Action> actions = actionRepository.findAllByChatId(chatId);
        for (Action action : actions) {
            if (!"match".equals(action.getType()) || !text.matches(action.getPattern())) {
                continue;
            }
            if (action.hasImage()) {
                replyImage(action);
            } else {
                replyText(action.getChatId(), action.getReply());
            }
        }
    }

    /**
      * Use {@code telegramClient} to reply with specifed text
      * @param chatId Chat identifier
      * @param text Text content of the reply
      */
    public void replyText(long chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void replyImage(Action action) {
        File randomImage = resourceService.getRandomImage(action.getChatId(), action.getResource());
        InputFile photoReply = new InputFile(randomImage);
        SendPhoto sendPhoto = SendPhoto.builder()
            .chatId(action.getChatId())
            .photo(photoReply)
            .caption(action.getReply())
            .build();
        try {
            telegramClient.execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

