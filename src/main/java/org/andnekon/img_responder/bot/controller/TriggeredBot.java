package org.andnekon.img_responder.bot.controller;

import java.util.List;

import org.andnekon.img_responder.bot.model.Chat;
import org.andnekon.img_responder.bot.service.action.ActionService;
import org.andnekon.img_responder.bot.service.chat.ChatService;
import org.andnekon.img_responder.bot.service.reply.ReplyService;
import org.andnekon.img_responder.bot.service.reply.ReplyType;
import org.andnekon.img_responder.utils.TgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
public class TriggeredBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TriggeredBot.class);

    private final String botTokenEnv;

    private final String allowedChatsEnv;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ReplyService replyService;

    private ChatService chatService;

    @Autowired
    public TriggeredBot(
            @Value("${tgAuthToken}") String botToken,
            @Value("${tgChatId}") String allowedChats,
            ChatService chatService) {
        this.allowedChatsEnv = allowedChats; // TODO: remove
        this.botTokenEnv = botToken;
        this.chatService = chatService;
        initialize();
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            var mto = TgUtils.getMessageText(message);
            if (mto.isEmpty()) {
                log("Empty messageText", chatId);
                return;
            }
            String messageText = mto.get();
            if (!chatService.isChatAllowed(chatId)) {
                log("Chat not allowed", chatId);
                List<Chat> allowedChats = chatService.listAllowedChats();
                for (Chat chat : allowedChats) {
                    log(chat.toString(), chatId);
                }
                return;
            }
            log(messageText, chatId);
            if (messageText.startsWith("/")) {
                log("Recieved command", chatId);
                processCommand(chatId, message);
            } else {
                processReply(chatId, messageText);
            }
        } else {
            logger.info("Recieved a different kind of update: {}", update.toString());
            logger.info("{}, {}", update.hasChannelPost(), update.hasCallbackQuery());
        }
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public String getBotToken() {
        return this.botTokenEnv;
    }

    /**
      * Structured logging, prepends chatId.
      * @param format Format string
      * @param chatId Chat identifier
      * @param args Remaining arguments for {@code String.format}
      */
    void log(String format, long chatId, Object... args) {
        // TODO: move to utils
        format = "[chat %d] " + format;
        if (args.length == 0) {
            logger.info(String.format(format, chatId));
        } else {
            logger.info(String.format(format, chatId, args));
        }
    }

    /**
      * Registers chats set up in the .env variable
      */
    private void initialize() {
        String[] chatIds = allowedChatsEnv.split(";");
        for (String chatId: chatIds) {
            try {
                chatService.registerChat(Long.valueOf(chatId));
            } catch (NumberFormatException e) {
                e.printStackTrace(); // don't fail if .env broken, for now
            }
        }
    }

    /**
      * Replies to message based on the triggers specifed by created actions
      * or ignores message.
      * @param chatId Chat identifier
      * @param messageText Recieved text
      */
    private void processReply(long chatId, String messageText) {
        ReplyType rt = replyService.getReplyType(messageText);
        logger.info("[chatId {}] Replying with type {}", chatId, rt.toString());
        replyService.reply(chatId, rt);
    }

    /**
      * Controller for the defined commands
      * @param chatId Chat identifier
      * @param message Recieved text
      */
    private void processCommand(long chatId, Message message) {
        var mto = TgUtils.getMessageText(message);
        if (mto.isEmpty()) {
            logger.error("[chatId {}], processCommand recieved emtpy message somehow", chatId);
            return;
        }
        String[] commandText = mto.get().split("\\s", 2);
        String command = commandText[0];
        log(command, chatId);
        String text = commandText.length > 1 ? commandText[1] : "";
        switch (command) {
            case "/create_action" ->
                actionService.createAction(chatId, message);
            case "/list_actions" ->
                actionService.listActions(chatId);
            case "/remove_action" ->
                actionService.removeAction(chatId, text);
            default ->
                replyService.replyError(chatId, "Not a valid command");
        }
    }
}

