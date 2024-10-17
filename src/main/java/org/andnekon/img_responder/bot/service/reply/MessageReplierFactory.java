package org.andnekon.img_responder.bot.service.reply;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MessageReplierFactory {

    public static MessageReplier getReplier(TelegramClient tc, long chatId, ReplyType rt) {
        switch (rt) {
            case IMAGE:
                return new ImageReplier(tc, chatId, rt);
            case TEXT:
                return new TextReplier(tc, chatId, rt);
            case NONE:
            case ERROR:
            default:
                return new IgnoreReplier(tc, chatId, rt);
        }
    }

}

