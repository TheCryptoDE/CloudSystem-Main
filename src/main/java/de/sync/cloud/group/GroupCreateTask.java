package de.sync.cloud.group;

import de.sync.cloud.group.Group;
import de.sync.cloud.group.GroupManager;
import de.sync.cloud.task.Task;

import java.io.IOException;
import java.sql.SQLException;

public class GroupCreateTask implements Task {

    private final GroupManager groupManager;

    public GroupCreateTask(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 6) {
            System.out.println("Usage: group create <groupname> <spigot|bungeecord> <static|nostatic> <minserver> <maxplayers>");
            return;
        }

        String action = args[1];
        if (!action.equalsIgnoreCase("create")) {
            System.out.println("Unbekannter group-command: " + action);
            return;
        }

        String groupName = args[2];
        String type = args[3].toLowerCase();
        String staticFlag = args[4].toLowerCase();

        int minServer;
        try {
            minServer = Integer.parseInt(args[5]);
        } catch (NumberFormatException e) {
            System.out.println("minserver must be a number.");
            return;
        }

        int maxPlayers;
        try {
            maxPlayers = Integer.parseInt(args[6]);
        } catch (NumberFormatException e) {
            System.out.println("maxplayers must be a number.");
            return;
        }

        if (!type.equals("spigot") && !type.equals("bungeecord")) {
            System.out.println("Type must be ‘spigot’ or ‘bungeecord’.");
            return;
        }
        if (!staticFlag.equals("static") && !staticFlag.equals("nostatic")) {
            System.out.println("staticFlag must be ‘static’ or ‘nostatic’.");
            return;
        }
        if (minServer < 0 || maxPlayers <= 0) {
            System.out.println("minserver must be >= 0 and maxplayers > 0.");
            return;
        }

        Group group = new Group(groupName, type, staticFlag, minServer, maxPlayers);

        try {
            groupManager.addGroup(group);
            System.out.println("Group ‘“ + groupName + ”’ successfully created.");
        } catch (SQLException e) {
            System.out.println("Error when creating the group: " + e.getMessage());
        }
    }
}
