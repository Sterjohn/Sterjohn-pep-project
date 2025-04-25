package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import Model.Account;
import Util.ConnectionUtil;
import java.sql.*;

public class SocialMediaController {

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.get("example-endpoint", this::exampleHandler);
        app.post("/register", this::registerHandler);
        return app;
    }

    private void exampleHandler(Context context) {
        context.json("sample text");
    }

    private void registerHandler(Context ctx) {
        Account newAccount = ctx.bodyAsClass(Account.class);

        // Input validation
        if (newAccount.getUsername() == null || newAccount.getUsername().isBlank()
                || newAccount.getPassword() == null || newAccount.getPassword().length() < 4) {
            ctx.status(400); // Bad Request
            return;
        }

        try (Connection conn = ConnectionUtil.getConnection()) {
            // Check if username already exists
            String checkSql = "SELECT * FROM account WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, newAccount.getUsername());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                ctx.status(400); // Username already exists
                return;
            }

            // Insert new account
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
                ctx.status(500); // Something went wrong with ID generation
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500); // Internal Server Error
        }
    }
}  
