package Service;

import DAO.AccountDAO;
import Model.Account;

/**
 * Service class for handling business logic related to Account operations.
 */
public class AccountService {

    private AccountDAO accountDAO = new AccountDAO();

    /**
     * Validates whether an account object contains a non-blank username
     * and a password with at least 4 characters.
     *
     * @param account the Account to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidAccount(Account account) {
        return account.getUsername() != null && !account.getUsername().isBlank()
                && account.getPassword() != null && account.getPassword().length() >= 4;
    }

    /**
     * Checks if a given username already exists in the database.
     *
     * @param username the username to check
     * @return true if username is taken, false otherwise
     */
    public boolean isUsernameTaken(String username) {
        return accountDAO.usernameExists(username);
    }

    /**
     * Registers a new account by inserting it into the database.
     *
     * @param account the Account to register
     * @return the registered Account with generated ID, or null if registration fails
     */
    public Account registerAccount(Account account) {
        return accountDAO.insertAccount(account);
    }

    /**
     * Attempts to log in using the provided username and password.
     *
     * @param username the username
     * @param password the password
     * @return the matching Account if credentials are valid, or null if invalid
     */
    public Account login(String username, String password) {
        return accountDAO.getAccountByCredentials(username, password);
    }
}
