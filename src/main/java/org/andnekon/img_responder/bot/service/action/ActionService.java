package org.andnekon.img_responder.bot.service.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.andnekon.img_responder.bot.dao.ActionRepository;
import org.andnekon.img_responder.bot.model.Action;
import org.andnekon.img_responder.bot.service.resource.ChatMemoryExceededException;
import org.andnekon.img_responder.bot.service.resource.ResourceService;
import org.andnekon.img_responder.utils.StringUtils;
import org.andnekon.img_responder.utils.TgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class ActionService {

    private static final Logger logger = LoggerFactory.getLogger(ActionService.class);

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    ResourceService resourceService;

    @Autowired
    ActionCreateValidator actionCreateValidator;

    /**
      * Validates and registers action to the chatId
      * and stores the passed attachment in the file system if possible.
      * @param chatId Id of the chat
      * @param message Message passed with the command
     * @throws ActionCommandError
      */
    public void createAction(long chatId, Message message) throws ActionCommandError {
        String messageText = TgUtils.getMessageText(message).orElse("");
        Action action = parseCreationText(messageText);
        action.setChatId(chatId);
        var errors = actionCreateValidator.validateObject(action);
        if (errors.getErrorCount() != 0) {
            throw new ActionCommandError("/create_action command has invalid format");
        }
        if (action.hasImage() && !message.hasDocument()) {
            throw new ActionCommandError("/create_action Actions with resources must have an attachment");
        } else if (action.hasImage()) {
            processSaveFile(chatId, message, action);
        }
        actionRepository.save(action);
        logger.info("[chat {}] Action saved", chatId);
    }

    /**
      * @param chatId Id of the chat
      * @return List of actions registered for the chat
      */
    public List<Action> listActions(long chatId) {
        List<Action> actions = actionRepository.findAllByChatId(chatId);
        logger.info("[chat: {}] Actions size: {}", chatId, actions.size());
        for (var action: actions) {
            logger.info(action.toString());
        }
        return actions;
    }

    /**
      * Removes action specified by action id passed in {@param text}.
      * @param chatId Id of the chat
      * @param actionId Text, passed with the command
      */
    public void removeAction(long chatId, long actionId) {
        List<Action> actions = actionRepository.findByIdAndChatId(actionId, chatId);
        for (Action action : actions) {
            if (!StringUtils.isEmtpy(action.getResource())) {
                resourceService.removeResource(chatId, action.getResource());
            }
        }
        actionRepository.deleteAll(actions);
    }

    /**
      * Converts text passed with the /create_action command to {@code Action}.
      * Valid text format: <ul>
      * <li> TYPE: match/time</li>
      * <li> PATTERN: regex/cron:cron_datetime</li>
      * <li> RESOURCE: filename/dirname</li>
      * </ul>
      * @param text Text to convert to action
      */
    private Action parseCreationText(String text) {
        String[] lines = text.split("\n");
        Action action = new Action();
        Map<String, String> actionLayout = new HashMap<>();
        for (String line : lines) {
            String[] row = line.split(": ", 2);
            if (row.length < 2) {
                continue;
            }
            actionLayout.put(row[0], row[1]);
        }
        if (actionLayout.containsKey("TYPE")) {
            action.setType(actionLayout.get("TYPE"));
        }
        if (actionLayout.containsKey("PATTERN")) {
            action.setPattern(actionLayout.get("PATTERN"));
        }
        if (actionLayout.containsKey("RESOURCE")) {
            action.setResource(actionLayout.get("RESOURCE"));
        }
        if (actionLayout.containsKey("REPLY")) {
            action.setReply(actionLayout.get("REPLY"));
        }
        return action;
    }

    private void processSaveFile(long chatId, Message message, Action action) throws ActionCommandError {
        try {
            resourceService.saveFile(chatId, message.getDocument(), action.getResource());
        } catch (NoSuchElementException e) {
            logger.info("[chat {}] /create_action chat not found");
            throw new ActionCommandError("/create_action chat not found");
        } catch (IOException | TelegramApiException e) {
            logger.info("[chat {}] /create_action error downloading files");
            e.printStackTrace();
            throw new ActionCommandError("/create_action error downloading files");
        } catch (ChatMemoryExceededException e) {
            logger.info("[chat {}] /create_action memory exceeded (possibly after downloading)");
            e.printStackTrace();
            throw new ActionCommandError("/create_action memory exceeded");
        } catch (IllegalStateException e) {
            logger.info("[chat {}] /create_action {}", e.getMessage());
            e.printStackTrace();
            throw new ActionCommandError(e.getMessage());
        }
    }
}

