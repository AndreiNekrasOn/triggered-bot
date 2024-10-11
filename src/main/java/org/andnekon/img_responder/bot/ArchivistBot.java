package org.andnekon.img_responder.bot;

import org.andnekon.img_responder.bot.response.MessageResponder;
import org.andnekon.img_responder.bot.response.MessageResponderFactory;
import org.andnekon.img_responder.bot.response.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Component
public class ArchivistBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    private static final String AUTH_TOKEN_ENV = "tgAuthToken";

    private static final Logger logger = LoggerFactory.getLogger(ArchivistBot.class);

    public ArchivistBot() {
        telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    @Override
    public String getBotToken() {
        return System.getenv(AUTH_TOKEN_ENV);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            ResponseType responseType = getResponseType(message.getText());
            if (!isChatAllowed(chatId)) {
                logger.info(String.format("Chat not allowed: %d", chatId));
                return;
            }
            logger.info(String.format("Chat allowed, responseType: %s", responseType.toString()));
            MessageResponder responder = MessageResponderFactory.getResponder(
                    telegramClient, message.getChatId(), responseType);
            try {
                responder.execute();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    private boolean isChatAllowed(long chatId) {
        return String.valueOf(chatId).equals(System.getenv("tgChatId"));
    }

    private ResponseType getResponseType(String text) {
        if (text.contains("img")) {
            return ResponseType.IMAGE;
        } else if (text.contains("corr")) {
            return ResponseType.CORRECTION;
        } else if (text.contains("wolf")) {
            return ResponseType.FUN_TEXT;
        } else if (text.length() < 3) {
            return ResponseType.TEXT;
        }
        return ResponseType.NONE;
    }
}
