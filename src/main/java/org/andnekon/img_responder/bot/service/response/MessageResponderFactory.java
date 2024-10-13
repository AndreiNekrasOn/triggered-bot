package org.andnekon.img_responder.bot.service.response;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MessageResponderFactory {

    public static MessageResponder getResponder(TelegramClient tc, long chatId, ResponseType rt) {
        switch (rt) {
            case IMAGE:
                return new ImageResponder(tc, chatId, rt);
            case TEXT:
                return new TextResponder(tc, chatId, rt);
            case NONE:
            case ERROR:
            default:
                return new IgnoreResponder(tc, chatId, rt);
        }
    }

}

