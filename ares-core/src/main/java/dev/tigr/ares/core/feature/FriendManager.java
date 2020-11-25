package dev.tigr.ares.core.feature;

import dev.tigr.ares.core.Ares;

import java.io.*;
import java.util.ArrayList;

/**
 * @author Tigermouthbear
 */
public class FriendManager {
    private static final String FILENAME = "Ares/friends.txt";
    private static ArrayList<String> friends;

    public static boolean save() {
        if(friends == null) {
            friends = new ArrayList<String>() {
            };
        }

        try {
            // First empty file
            PrintWriter writer = new PrintWriter(FILENAME);
            writer.print("");
            writer.close();

            // Then write
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter(FILENAME));
            for(String friend: friends) {
                outputWriter.write(friend);
                outputWriter.newLine();
            }
            outputWriter.flush();
            outputWriter.close();
            return true;
        } catch(Exception e) {
            Ares.LOGGER.info("Could not write friends.txt: " + e.toString());
            e.printStackTrace();
            Ares.LOGGER.info(friends);
        }
        return false;
    }

    public static boolean read() {
        try {
            try {
                friends = new ArrayList<>();

                BufferedReader bufferedReader = new BufferedReader(new FileReader(FILENAME));

                String line;
                while((line = bufferedReader.readLine()) != null) {
                    friends.add(line);
                }
                bufferedReader.close();
                Ares.LOGGER.info("Successfully read friends: " + friends.toString());
                return true;
            } catch(FileNotFoundException e) {
                File file = new File(FILENAME);
                file.createNewFile();
                friends = new ArrayList<String>() {
                };
                return true;
            }
        } catch(Exception e) {
            Ares.LOGGER.info("Could not read friends: " + e.toString());
            e.printStackTrace();
        }
        return false;
    }

    public static ArrayList<String> getFriends() {
        return friends;
    }

    public static void addFriend(String friend) {
        if(friends == null) return;
        friends.add(friend);
        save();
    }

    public static void removeFriend(String friend) {
        if(friends == null) return;
        friends.remove(friend);
        save();
    }

    public static boolean isFriend(String player) {
        if(friends == null) return false;
        return friends.contains(player);
    }
}
