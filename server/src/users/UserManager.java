package users;

import data.PermissionType;
import data.SheetUserData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserManager {

    private final Set<String> usersSet;
    private Map<String, Set<SheetUserData>> userToSheetData;  // Maps userId -> Set of SheetUserData

    public UserManager() {
        usersSet = new HashSet<>();
        userToSheetData = new HashMap<>();
    }

    public synchronized void addUser(String username) {
        usersSet.add(username);
    }

    public synchronized void removeUser(String username) {
        usersSet.remove(username);
    }

    public boolean isUserExists(String username) {
        return usersSet.contains(username);
    }

    public synchronized Set<String> getUsers() {
        return new HashSet<>(usersSet);
    }

    public synchronized void addSheetToUser(String username, SheetUserData sheetData) {
        if (!userToSheetData.containsKey(username)) {
            userToSheetData.put(username, new HashSet<>());
        }
        userToSheetData.get(username).add(sheetData);

    }
    public boolean isSheetExists(String sheetName){
        for (String user : userToSheetData.keySet()) {
            for (SheetUserData sheetData : userToSheetData.get(user)) {
                if(sheetData.getSheetName().equals(sheetName)){
                    return true;
                }
            }
        }
        return false;
    }

    public Set<SheetUserData> getAllSheets() {
        Set<SheetUserData> allSheets = new HashSet<>();
        for (Set<SheetUserData> sheets : userToSheetData.values()) {
            allSheets.addAll(sheets);
        }
        return allSheets;
    }

    public SheetUserData getSheetByName(String sheetName) {
        // Logic to retrieve the SheetUserData by name
        for (Set<SheetUserData> sheets : userToSheetData.values()) {
            for (SheetUserData sheetData : sheets) {
                if (sheetData.getSheetName().equals(sheetName)) {
                    return sheetData;
                }
            }
        }
        return null;
    }



}
