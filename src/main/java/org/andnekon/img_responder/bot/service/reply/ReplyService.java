package org.andnekon.img_responder.bot.service.reply;

import java.io.File;

import org.andnekon.img_responder.bot.service.resource.ResourceService;
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
    ResourceService resourceService;

    Logger logger = LoggerFactory.getLogger(ReplyService.class);

    public ReplyService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
      * Determines response type based on the text provided.
      * @param text Recieved text
      * @return response type
      */
    public ReplyType getReplyType(String text) {
        if (text.contains("img")) {
            return ReplyType.IMAGE;
        } else if (text.length() < 3) {
            return ReplyType.TEXT;
        }
        return ReplyType.NONE;
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
      * @param rt ReplyType
      */
    public void reply(long chatId, ReplyType rt) {
        try {
            switch (rt) {
                case IMAGE -> replyImage(chatId);
                case TEXT -> replyText(chatId);
                default -> {}
            };
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void replyText(long chatId) throws TelegramApiException {
        String replyText = "Hello, world!";
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(replyText)
            .build();
        telegramClient.execute(sendMessage);
    }

    private void replyImage(long chatId) throws TelegramApiException {
        File randomImage = resourceService.getRandomImage(chatId, "data/");
        InputFile photoReply = new InputFile(randomImage);
        String caption = "yay!";
        SendPhoto sendPhoto = SendPhoto.builder()
            .chatId(chatId)
            .photo(photoReply)
            .caption(caption)
            .build();
        telegramClient.execute(sendPhoto);
    }
}

