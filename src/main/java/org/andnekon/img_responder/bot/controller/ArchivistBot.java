package org.andnekon.img_responder.bot.controller;

import org.andnekon.img_responder.bot.service.action.ActionService;
import org.andnekon.img_responder.bot.service.chat.ChatService;
import org.andnekon.img_responder.bot.service.reply.ReplyService;
import org.andnekon.img_responder.bot.service.reply.ReplyType;
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
public class ArchivistBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final String botTokenEnv;

    private final String allowedChatsEnv;

    private static final Logger logger = LoggerFactory.getLogger(ArchivistBot.class);

    @Autowired
    private ActionService actionService;

    @Autowired
    private ReplyService replyService;

    private ChatService chatService;


    @Autowired
    public ArchivistBot(
            @Value("${tgAuthToken}") String botToken,
            @Value("${tgChatId}") String allowedChats,
            ChatService chatService) {
        this.allowedChatsEnv = allowedChats; // TODO: remove
        this.botTokenEnv = botToken;
        this.chatService = chatService;
        initialize();
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

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String messageText = message.getText();
            if (!chatService.isChatAllowed(chatId)) {
                log("Chat not allowed", chatId);
                return;
            }
            if (message.isCommand()) {
                log("Recieved command", message.getChatId());
                processCommand(chatId, messageText);
            } else {
                processReply(chatId, messageText);
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
        log("Replying with responce type %s", chatId, rt.toString());
        replyService.reply(chatId, rt);
    }

    /**
      * Controller for the defined commands
      * @param chatId Chat identifier
      * @param messageText Recieved text
      */
    private void processCommand(long chatId, String messageText) {
        String[] commandText = messageText.split(" ", 2);
        String command = commandText[0];
        String text = commandText.length > 1 ? commandText[1] : "";
        switch (command) {
            case "/create_action" ->
                actionService.createAction(chatId, text);
            case "/list_actions" ->
                actionService.listActions(chatId);
            case "/remove_action" ->
                actionService.removeAction(chatId, text);
            default ->
                replyService.replyError(chatId, "Not a valid command");
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
}

