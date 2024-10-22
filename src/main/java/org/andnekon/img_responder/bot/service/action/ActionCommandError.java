package org.andnekon.img_responder.bot.service.action;

public class ActionCommandError extends Exception {

    private String message;

    public ActionCommandError(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
