package org.andnekon.img_responder.bot.service.reply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public abstract class MessageReplier {

    private static final Logger logger = LoggerFactory.getLogger(MessageReplier.class);

    TelegramClient tc;
    long chatId;
    ReplyType rt;

    Object response;

    public MessageReplier(TelegramClient tc, long chatId, ReplyType rt) {
        this.tc = tc;
        this.chatId = chatId;
        this.rt = rt;
    }

    public void execute() throws TelegramApiException {
        prepare();
        if (response instanceof SendMessage) {
            logger.info("Sending message");
            tc.execute((SendMessage) response);
        } else if (response instanceof SendPhoto) {
            tc.execute((SendPhoto) response);
        } // else do nothing, later logging
    }

    abstract void prepare();
}

