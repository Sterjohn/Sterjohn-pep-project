package Service;

import DAO.MessageDAO;
import Model.Message;

import java.util.List;

/**
 * Service class for handling business logic related to Message operations.
 */
public class MessageService {

    private MessageDAO messageDAO = new MessageDAO();

    /**
     * Creates a new message after validating its content and posted_by ID.
     *
     * @param message the Message to create
     * @return the created Message if valid, otherwise null
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
     *
     * @return a list of all messages
     */
    public List<Message> getAllMessages() {
        return messageDAO.getAllMessages();
    }

    /**
     * Retrieves all messages posted by a specific user.
     *
     * @param userId the ID of the user
     * @return a list of messages from the user
     */
    public List<Message> getMessagesByUser(int userId) {
        return messageDAO.getMessagesByUser(userId);
    }

    /**
     * Retrieves a message by its unique ID.
     *
     * @param messageId the ID of the message
     * @return the Message if found, otherwise null
     */
    public Message getMessageById(int messageId) {
        return messageDAO.getMessageById(messageId);
    }

    /**
     * Updates the text of a message after validating the new content.
     *
     * @param messageId the ID of the message to update
     * @param newText the new message text
     * @return the updated Message if valid, otherwise null
     */
    public Message updateMessage(int messageId, String newText) {
        if (newText == null || newText.isBlank() || newText.length() > 255) {
            return null;
        }
        return messageDAO.updateMessage(messageId, newText);
    }

    /**
     * Deletes a message by its ID.
     *
     * @param messageId the ID of the message to delete
     * @return true if the message was successfully deleted, false otherwise
     */
    public boolean deleteMessage(int messageId) {
        return messageDAO.deleteMessage(messageId);
    }
}
