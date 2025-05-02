package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;

import java.util.List;

public class SocialMediaController {

    private AccountService accountService = new AccountService();
    private MessageService messageService = new MessageService();

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::registerHandler);
        app.post("/login", this::loginHandler);
        app.post("/messages", this::createMessageHandler);
        app.get("/messages", this::getAllMessagesHandler);
        app.get("/accounts/{user_id}/messages", this::getMessagesByUserHandler);
        app.get("/messages/{message_id}", this::getMessageByIdHandler);
        app.patch("/messages/{message_id}", this::updateMessageHandler);
        app.delete("/messages/{message_id}", this::deleteMessageHandler);
        return app;
    }

    /**
     * Updated to handle blank username and short password
     */
    private void registerHandler(Context ctx) {
        Account newAccount = ctx.bodyAsClass(Account.class);
        if (!accountService.isValidAccount(newAccount) || accountService.isUsernameTaken(newAccount.getUsername())) {
            ctx.status(400);
            return;
        }
        Account registeredAccount = accountService.registerAccount(newAccount);
        if (registeredAccount != null) {
            ctx.status(200);
            ctx.json(registeredAccount);
        } else {
            ctx.status(500);
        }
    }

    /**
     * Updated to use AccountService for login verification
     */
    private void loginHandler(Context ctx) {
        Account loginAttempt = ctx.bodyAsClass(Account.class);
        Account account = accountService.login(loginAttempt.getUsername(), loginAttempt.getPassword());
        if (account != null) {
            ctx.status(200);
            ctx.json(account);
        } else {
            ctx.status(401);
        }
    }

    /**
     * Updated to return 400 with empty body on validation failure
     */
    private void createMessageHandler(Context ctx) {
        Message message = ctx.bodyAsClass(Message.class);
        Message createdMessage = messageService.createMessage(message);
        if (createdMessage != null) {
            ctx.status(200);
            ctx.json(createdMessage);
        } else {
            ctx.status(400);
            ctx.result("");
        }
    }

    /**
     * Returns all messages with status 200
     */
    private void getAllMessagesHandler(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.status(200);
        ctx.json(messages);
    }

    /**
     * Returns messages by user with status 200
     */
    private void getMessagesByUserHandler(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("user_id"));
        List<Message> messages = messageService.getMessagesByUser(userId);
        ctx.status(200);
        ctx.json(messages);
    }

    /**
     * Updated to return 200 and empty body if message not found
     */
    private void getMessageByIdHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        Message message = messageService.getMessageById(messageId);
        if (message != null) {
            ctx.status(200);
            ctx.json(message);
        } else {
            ctx.status(200);
            ctx.result("");
        }
    }

    /**
     * Updated to return 400 and empty body if update fails
     */
    private void updateMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        Message incoming = ctx.bodyAsClass(Message.class);
        Message updated = messageService.updateMessage(messageId, incoming.getMessage_text());
        if (updated != null) {
            ctx.status(200);
            ctx.json(updated);
        } else {
            ctx.status(400);
            ctx.result("");
        }
    }

    /**
     * Updated to return deleted message JSON on success, empty body otherwise
     */
    private void deleteMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        Message deleted = messageService.getMessageById(messageId);

        if (deleted != null && messageService.deleteMessage(messageId)) {
            ctx.status(200);
            ctx.json(deleted);
        } else {
            ctx.status(200);
            ctx.result("");
        }
    }
}
