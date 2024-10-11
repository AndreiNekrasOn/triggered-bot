package org.andnekon.img_responder.bot.response;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class IgnoreResponder extends MessageResponder {

    public IgnoreResponder(TelegramClient tc, long chatId, ResponseType rt) {
        super(tc, chatId, rt);
    }

    @Override
    void prepare() {
        response = null;
    }

}

