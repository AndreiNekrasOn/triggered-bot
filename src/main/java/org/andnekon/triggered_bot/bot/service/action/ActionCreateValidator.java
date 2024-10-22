package org.andnekon.triggered_bot.bot.service.action;

import org.andnekon.triggered_bot.bot.model.Action;
import org.andnekon.triggered_bot.utils.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ActionCreateValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Action.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "chatId", "chatId.empty");
        ValidationUtils.rejectIfEmpty(errors, "type", "type.empty");
        Action a = (Action) target;
        if ("match".equals(a.getType()) || "time".equals(a.getType())) {
            if (StringUtils.isEmtpy(a.getPattern())) {
                errors.reject("image.trigger-empty");
            }
            if (StringUtils.isEmtpy(a.getReply()) &&
                    (StringUtils.isEmtpy(a.getResource()) || !a.getResource().endsWith("/"))) {
                errors.reject("text.reply-and-resource-empty");
            }
        } else {
            errors.rejectValue("type", "type.incorrect");
        }
    }
}

