package org.andnekon.img_responder.bot.service.reply;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TextReplier extends MessageReplier  {

    public TextReplier(TelegramClient tc, long chatId, ReplyType rt) {
        super(tc, chatId, rt);
        assert switch (rt) {
            case IMAGE -> true;
            default -> false;
        };
    }

    @Override
    void prepare() {
        String replyText = switch (rt) {
            case TEXT -> getReply();
            default -> throw new IllegalStateException(
                    String.format("Unreachable ResponseType: %s", rt.toString()));
        };
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(replyText)
            .build();
        response = sendMessage;
    }

    private String getReply() {
        return "Hello, World!";
    }
}

