package org.andnekon.img_responder.bot.service.reply;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class IgnoreReplier extends MessageReplier {

    public IgnoreReplier(TelegramClient tc, long chatId, ReplyType rt) {
        super(tc, chatId, rt);
    }

    @Override
    void prepare() {
        response = null;
    }

}

