package org.andnekon.img_responder.bot.service.reply;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class ReplyService {

    private final TelegramClient telegramClient;

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
        // TODO: move to ReplyService
        MessageReplier responder = MessageReplierFactory.getReplier(
                telegramClient, chatId, ReplyType.ERROR);
        try {
            responder.execute();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        // log(error, chatId);
    }

    public void reply(long chatId, ReplyType rt) {
        MessageReplier responder = MessageReplierFactory.getReplier(
                telegramClient, chatId, rt);
        try {
            responder.execute();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
