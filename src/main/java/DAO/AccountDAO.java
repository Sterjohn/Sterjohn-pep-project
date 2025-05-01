package DAO;

import Model.Account;
import Util.ConnectionUtil;

import java.sql.*;

/**
 * Handles all database operations related to Account.
 */
public class AccountDAO {

    /**
     * Checks if a username already exists in the account table.
     */
    public boolean usernameExists(String username) {
        Connection conn = null;
        try {
            conn = ConnectionUtil.getConnection();
            String sql = "SELECT * FROM account WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // assume taken if there's a DB error
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Inserts a new account into the database and returns it with generated ID.
     */
    public Account insertAccount(Account account) {
        Connection conn = null;
        try {
            conn = ConnectionUtil.getConnection();
            String sql = "INSERT INTO account (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                account.setAccount_id(rs.getInt(1));
                return account;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
        return null;
    }

    /**
     * Returns an Account if the username and password match a row in the table.
     */
    public Account getAccountByCredentials(String username, String password) {
        Connection conn = null;
        try {
            conn = ConnectionUtil.getConnection();
            String sql = "SELECT * FROM account WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Account(
                    rs.getInt("account_id"),
                    rs.getString("username"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
        return null;
    }
}
