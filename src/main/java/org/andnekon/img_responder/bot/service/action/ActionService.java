package org.andnekon.img_responder.bot.service.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.andnekon.img_responder.bot.dao.ActionRepository;
import org.andnekon.img_responder.bot.model.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionService {

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    ActionCreateValidator actionCreateValidator;

    private static final Logger logger = LoggerFactory.getLogger(ActionService.class);

    /**
      * Validates and registers action to the chatId
      * and stores the passed attachment in the file system if possible.
      * @param chatId Id of the chat
      * @param text Message passed with the command
      */
    public void createAction(long chatId, String text) {
        Action action = parseCreationText(text);
        action.setChatId(chatId);
        var errors = actionCreateValidator.validateObject(action);
        if (errors.getErrorCount() != 0) {
            logger.info(String.format("[chat %d] /create_action text has errors: %s",
                        chatId, errors.toString()));
            return;
        }
        actionRepository.save(action);
        logger.info(String.format("[chat %d] Actino saved", chatId));
    }

    /**
      * @param chatId Id of the chat
      * @return List of actions registered for the chat
      */
    public List<Action> listActions(long chatId) {
        List<Action> actions = actionRepository.findByChatId(chatId);
        logger.info(String.format("[chat: %d] Actions size: %d", chatId, actions.size()));
        for (var action: actions) {
            logger.info(action.toString());
        }
        return actions;
    }


    /**
      * Removes action specified by action id passed in {@param text}.
      * @param chatId Id of the chat
      * @param text Text, passed with the command
      */
    public void removeAction(long chatId, String text) {

    }

    /**
      * Converts text passed with the /create_action command to {@code Action}.
      * Valid text format: <ul>
      * <li> TYPE: image/text</li>
      * <li> PATTERN: re:regex/cron:cron_datet</li>
      * <li> RESOURCE: filename/dirname</li>
      * </ul>
      * @param text Text to convert to action
      */
    Action parseCreationText(String text) {
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
        return action;
    }
}
