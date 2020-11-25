package dev.tigr.ares.core.feature;

import com.mojang.authlib.exceptions.AuthenticationException;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.util.AbstractAccount;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static dev.tigr.ares.core.Ares.UTILS;

/**
 * @author Tigermouthbear 7/7/20
 */
public class AccountManager {
    private static final File ACCOUNTS_FILE = new File("Ares/accounts.json");
    private static final List<AbstractAccount> ACCOUNTS = read();

    private static List<AbstractAccount> read() {
        try {
            return read(ACCOUNTS_FILE);
        } catch(IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static List<AbstractAccount> read(File file) throws IOException {
        // create file if it doesnt exist
        if(!file.exists()) {
            file.createNewFile();
            return new ArrayList<>();
        }

        // read json object from file
        JSONArray accountsArray = new JSONArray(new JSONTokener(new FileInputStream(file)));

        // create list of accounts from json object
        List<AbstractAccount> list = new ArrayList<>();

        // create account for every json object in list
        for(int i = 0; i < accountsArray.length(); i++) {
            JSONObject accountObject = accountsArray.getJSONObject(i);
            if(accountObject.has("email") && accountObject.has("password")) {
                String email = accountObject.getString("email");
                String password = accountObject.getString("password");

                if(accountObject.has("uuid")) {
                    // create and add to list from email + password + uuid
                    list.add(UTILS.createAccount(email, password, accountObject.getString("uuid")));
                } else {
                    // create from email + password
                    AbstractAccount account = null;
                    try {
                        account = UTILS.createAccount(email, password);
                    } catch(AuthenticationException ignored) {
                    }
                    if(account != null) {
                        list.add(account);
                        Ares.LOGGER.info(account.getName() + ":" + account.getUuid());
                    }
                }
            }
        }
        return list;
    }

    public static void save() {
        try {
            save(ACCOUNTS_FILE);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void save(File file) throws IOException {
        // write everything to save object
        JSONArray accountsArray = new JSONArray(); // create array of accounts
        for(AbstractAccount account: ACCOUNTS) {
            // create account object and save to array
            JSONObject accountObject = new JSONObject();

            accountObject.put("email", account.getEmail());
            accountObject.put("password", account.getPassword());
            accountObject.put("uuid", account.getUuid());

            accountsArray.put(accountObject);
        }

        // write save object to file
        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.print(accountsArray.toString(4));
        printWriter.close();
    }

    public static List<AbstractAccount> getAccounts() {
        return ACCOUNTS;
    }
}
