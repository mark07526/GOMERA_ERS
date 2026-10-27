package com.scc;

import com.scc.dao.initializeDatabase;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // 1. Initialize the database first (Tables and constraints setup)
        initializeDatabase.initialize();

        // 2. Create shared objects
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        try {
            // 3. Start authentication and role-based routing
            Authentication authentication =
                    new Authentication(userDao, scanner);

            authentication.start();

        } finally {
            // 4. Close the shared Scanner when the application ends
            scanner.close();
            System.out.println("Application closed successfully.");
        }
    }
}