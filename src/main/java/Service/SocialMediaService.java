package Service;

import DAO.AccountDAO;
import DAO.MessageDAO;
import Model.Account;
import Model.Message;

import java.util.List;

/**
 * Uses DAO classes to handle business logic for accounts and messages.
 */
public class SocialMediaService {

    private AccountDAO accountDAO = new AccountDAO();
    private MessageDAO messageDAO = new MessageDAO();

    // ----------- Account Logic -----------

    /**
     * Validates that the account has a non-blank username and password with at least 4 characters.
     */
    public boolean isValidAccount(Account account) {
        return account.getUsername() != null && !account.getUsername().isBlank()
                && account.getPassword() != null && account.getPassword().length() >= 4;
    }

    /**
     * Checks if the username already exists in the system.
     */
    public boolean isUsernameTaken(String username) {
        return accountDAO.usernameExists(username);
    }

    /**
     * Inserts a new account into the database.
     */
    public Account registerAccount(Account account) {
        return accountDAO.insertAccount(account);
    }

    /**
     * Returns an account if the credentials match a database record.
     */
    public Account login(String username, String password) {
        return accountDAO.getAccountByCredentials(username, password);
    }

    // ----------- Message Logic -----------

    /**
     * Validates and inserts a new message.
     */
    public Message createMessage(Message message) {
        if (message.getMessage_text() == null || message.getMessage_text().isBlank()
                || message.getMessage_text().length() > 255 || message.getPosted_by() <= 0) {
            return null;
        }
        return messageDAO.insertMessage(message);
    }

    /**
     * Retrieves all messages from the database.
     */
    public List<Message> getAllMessages() {
        return messageDAO.getAllMessages();
    }

    /**
     * Gets messages posted by a specific user.
     */
    public List<Message> getMessagesByUser(int userId) {
        return messageDAO.getMessagesByUserId(userId);
    }

    /**
     * Retrieves a single message by its ID.
     */
    public Message getMessageById(int messageId) {
        return messageDAO.getMessageById(messageId);
    }

    /**
     * Updates message text if it's valid.
     */
    public Message updateMessageText(int messageId, String newText) {
        if (newText == null || newText.isBlank() || newText.length() > 255) {
            return null;
        }
        return messageDAO.updateMessageText(messageId, newText);
    }

    /**
     * Deletes a message by its ID.
     */
    public boolean deleteMessageById(int messageId) {
        return messageDAO.deleteMessageById(messageId);
    }
}
