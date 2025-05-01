package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import Model.Account;
import Model.Message;
import Service.SocialMediaService;

import java.util.List;

/**
 * Handles routing for account and message features.
 * Business logic is handled in the service layer.
 */
public class SocialMediaController {

    private SocialMediaService service = new SocialMediaService();

    /**
     * Sets up and returns the Javalin app with all routes.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.get("example-endpoint", this::exampleHandler);
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

    private void exampleHandler(Context context) {
        context.json("sample text");
    }

    /**
     * Handles user signup. Rejects short passwords or blank usernames.
     */
    private void registerHandler(Context ctx) {
        Account newAccount = ctx.bodyAsClass(Account.class);

        if (!service.isValidAccount(newAccount) || service.isUsernameTaken(newAccount.getUsername())) {
            ctx.status(400);
            return;
        }

        Account registeredAccount = service.registerAccount(newAccount);
        if (registeredAccount != null) {
            ctx.status(200);
            ctx.json(registeredAccount);
        } else {
            ctx.status(500);
        }
    }

    /**
     * Logs in the user if credentials match a record.
     */
    private void loginHandler(Context ctx) {
        Account loginAttempt = ctx.bodyAsClass(Account.class);
        Account result = service.login(loginAttempt.getUsername(), loginAttempt.getPassword());

        if (result != null) {
            ctx.status(200);
            ctx.json(result);
        } else {
            ctx.status(401);
        }
    }

    /**
     * Creates a new message if it passes validation.
     */
    private void createMessageHandler(Context ctx) {
        Message newMessage = ctx.bodyAsClass(Message.class);
        Message created = service.createMessage(newMessage);

        if (created != null) {
            ctx.status(200);
            ctx.json(created);
        } else {
            ctx.status(400);
        }
    }

    /**
     * Returns all messages in the database.
     */
    private void getAllMessagesHandler(Context ctx) {
        List<Message> messages = service.getAllMessages();
        ctx.status(200);
        ctx.json(messages);
    }

    /**
     * Returns all messages posted by a given user.
     */
    private void getMessagesByUserHandler(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("user_id"));
        List<Message> messages = service.getMessagesByUser(userId);
        ctx.status(200);
        ctx.json(messages);
    }

    /**
     * Gets a message by its ID. Sends 200 even if not found (test requirement).
     */
    private void getMessageByIdHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        Message message = service.getMessageById(messageId);

        if (message != null) {
            ctx.status(200);
            ctx.json(message);
        } else {
            ctx.status(200); // Required by test: return 200 with no body
        }
    }

    /**
     * Updates message text if the new text is valid.
     */
    private void updateMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        Message incoming = ctx.bodyAsClass(Message.class);
        Message updated = service.updateMessageText(messageId, incoming.getMessage_text());

        if (updated != null) {
            ctx.status(200);
            ctx.json(updated);
        } else {
            ctx.status(400);
        }
    }

    /**
     * Deletes a message and returns the deleted object.
     * If it didn’t exist, still return 200 (per test spec).
     */
    private void deleteMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        Message toDelete = service.getMessageById(messageId);

        if (toDelete != null) {
            service.deleteMessageById(messageId);
            ctx.status(200);
            ctx.json(toDelete);
        } else {
            ctx.status(200); // Required by test: return 200 with no body
        }
    }
}

/*Going to re-commit my code. Hopefully my instructor can see everything. I made a ton of changes and added a ton of comments, but they said nothing was changed. Hopefully it works this time. */

/*Trying again. Praying this works. */