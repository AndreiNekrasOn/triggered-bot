package org.andnekon.img_responder.bot.service.reply;

import java.io.File;
import java.util.List;

import org.andnekon.img_responder.bot.dao.ActionRepository;
import org.andnekon.img_responder.bot.model.Action;
import org.andnekon.img_responder.bot.service.resource.ResourceService;
import org.andnekon.img_responder.utils.StringUtils;
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
        // dummy
        logger.info("[chatId {}] {}", chatId, error);
    }

    /**
      * Use {@code telegramClient} to reply, depending on {@code ReplyType}
      * @param chatId Chat identifier
      * @param messageText ReplyType
      */
    public void reply(long chatId, String messageText) {
        List<Action> actions = actionRepository.findAllByChatId(chatId);
        for (Action action : actions) {
            if (!"match".equals(action.getType()) || !messageText.matches(action.getPattern())) {
                continue;
            }
            try {
                if (action.hasImage()) {
                    replyImage(action);
                } else {
                    replyText(action);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void replyText(Action action) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(action.getChatId())
            .text(action.getReply())
            .build();
        telegramClient.execute(sendMessage);
    }

    private void replyImage(Action action) throws TelegramApiException {
        File randomImage = resourceService.getRandomImage(action.getChatId(), action.getResource());
        InputFile photoReply = new InputFile(randomImage);
        SendPhoto sendPhoto = SendPhoto.builder()
            .chatId(action.getChatId())
            .photo(photoReply)
            .caption(action.getReply())
            .build();
        telegramClient.execute(sendPhoto);
    }
}

