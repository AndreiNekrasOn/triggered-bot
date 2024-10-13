package org.andnekon.img_responder.bot.service.response;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TextResponder extends MessageResponder  {

    public TextResponder(TelegramClient tc, long chatId, ResponseType rt) {
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

