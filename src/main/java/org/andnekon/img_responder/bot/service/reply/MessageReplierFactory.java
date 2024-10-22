package org.andnekon.img_responder.bot.service.reply;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MessageReplierFactory {

    public static MessageReplier getReplier(TelegramClient tc, long chatId, ReplyType rt) {
        return switch (rt) {
            case IMAGE -> new ImageReplier(tc, chatId, rt);
            case TEXT -> new TextReplier(tc, chatId, rt);
            case NONE, ERROR -> new IgnoreReplier(tc, chatId, rt);
            default -> new IgnoreReplier(tc, chatId, rt);
        };
    }

}

