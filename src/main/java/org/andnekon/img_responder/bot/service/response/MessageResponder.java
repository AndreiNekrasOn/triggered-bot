package org.andnekon.img_responder.bot.service.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public abstract class MessageResponder {

    TelegramClient tc;
    long chatId;
    ResponseType rt;
    Object response;

    Logger logger = LoggerFactory.getLogger(MessageResponder.class);

    public MessageResponder(TelegramClient tc, long chatId, ResponseType rt) {
        this.tc = tc;
        this.chatId = chatId;
        this.rt = rt;
    }

    abstract void prepare();

    public void execute() throws TelegramApiException {
        prepare();
        if (response instanceof SendMessage) {
            logger.info("Sending message");
            tc.execute((SendMessage) response);
        } else if (response instanceof SendPhoto) {
            tc.execute((SendPhoto) response);
        } // else do nothing, later logging
    }
}

