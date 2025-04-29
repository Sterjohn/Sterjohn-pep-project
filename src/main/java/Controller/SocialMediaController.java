package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import Model.Account;
import Model.Message;
import Util.ConnectionUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SocialMediaController {

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

    private void registerHandler(Context ctx) {
        Account newAccount = ctx.bodyAsClass(Account.class);

        if (newAccount.getUsername() == null || newAccount.getUsername().isBlank()
                || newAccount.getPassword() == null || newAccount.getPassword().length() < 4) {
            ctx.status(400);
            return;
        }

        try (Connection conn = ConnectionUtil.getConnection()) {
            String checkSql = "SELECT * FROM account WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, newAccount.getUsername());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                ctx.status(400);
                return;
            }

            String insertSql = "INSERT INTO account (username, password) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, newAccount.getUsername());
            insertStmt.setString(2, newAccount.getPassword());
            insertStmt.executeUpdate();

            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                Account createdAccount = new Account(id, newAccount.getUsername(), newAccount.getPassword());
                ctx.json(createdAccount);
            } else {
                ctx.status(500);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    private void loginHandler(Context ctx) {
        Account loginAttempt = ctx.bodyAsClass(Account.class);

        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM account WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loginAttempt.getUsername());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword.equals(loginAttempt.getPassword())) {
                    int id = rs.getInt("account_id");
                    Account authenticated = new Account(id, loginAttempt.getUsername(), storedPassword);
                    ctx.json(authenticated);
                    return;
                }
            }
            ctx.status(401);
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    private void createMessageHandler(Context ctx) {
        Message newMessage = ctx.bodyAsClass(Message.class);

        if (newMessage.getMessage_text() == null || newMessage.getMessage_text().isBlank()
                || newMessage.getMessage_text().length() > 255) {
            ctx.status(400);
            return;
        }

        try (Connection conn = ConnectionUtil.getConnection()) {
            String userCheckSql = "SELECT * FROM account WHERE account_id = ?";
            PreparedStatement userCheckStmt = conn.prepareStatement(userCheckSql);
            userCheckStmt.setInt(1, newMessage.getPosted_by());
            ResultSet userRs = userCheckStmt.executeQuery();
            if (!userRs.next()) {
                ctx.status(400);
                return;
            }

            String insertSql = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, newMessage.getPosted_by());
            stmt.setString(2, newMessage.getMessage_text());
            stmt.setLong(3, newMessage.getTime_posted_epoch());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int messageId = keys.getInt(1);
                Message created = new Message(messageId, newMessage.getPosted_by(), newMessage.getMessage_text(), newMessage.getTime_posted_epoch());
                ctx.json(created);
            } else {
                ctx.status(500);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    private void getAllMessagesHandler(Context ctx) {
        List<Message> messages = new ArrayList<>();
        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM message";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message m = new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                );
                messages.add(m);
            }
            ctx.json(messages);
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    private void getMessagesByUserHandler(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("user_id"));
        List<Message> messages = new ArrayList<>();
        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM message WHERE posted_by = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message m = new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                );
                messages.add(m);
            }
            ctx.json(messages);
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    private void getMessageByIdHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM message WHERE message_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, messageId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Message m = new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                );
                ctx.json(m);
            } else {
                ctx.result("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    private void updateMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        Message updated = ctx.bodyAsClass(Message.class);
        String newText = updated.getMessage_text();

        if (newText == null || newText.isBlank() || newText.length() > 255) {
            ctx.status(400);
            return;
        }

        try (Connection conn = ConnectionUtil.getConnection()) {
            String checkSql = "SELECT * FROM message WHERE message_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, messageId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                ctx.status(400);
                return;
            }

            int postedBy = rs.getInt("posted_by");
            long timestamp = rs.getLong("time_posted_epoch");

            String updateSql = "UPDATE message SET message_text = ? WHERE message_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, newText);
            updateStmt.setInt(2, messageId);
            updateStmt.executeUpdate();

            Message response = new Message(messageId, postedBy, newText, timestamp);
            ctx.json(response);
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    private void deleteMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));

        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM message WHERE message_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, messageId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Message m = new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                );

                String deleteSql = "DELETE FROM message WHERE message_id = ?";
                PreparedStatement delStmt = conn.prepareStatement(deleteSql);
                delStmt.setInt(1, messageId);
                delStmt.executeUpdate();

                ctx.json(m);
            } else {
                ctx.result("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }
}  

/*Woohoo! That was no joke. Please let me know what changes I may need to make. Thanks!*/

/*Welp. RevPro isn't recognizing any of my commits. This really sucks. */

/*"You cannot submit the project without committing the progress done in the project. Please commit the recent changes in your code editor platform." I'm legit gonna cry if I can't submit this ugh. */