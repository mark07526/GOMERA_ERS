
package com.scc;

import com.scc.dao.initializeDatabase;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // 1. Initialize database before starting the system
        initializeDatabase.initialize();

        // 2. Create shared objects
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        try {

            // 3. Start authentication system
            Authentication authentication =
                    new Authentication(userDao, scanner);

            // 4. Start registration, login, and role-based menus
            authentication.start();

        } catch (Exception e) {

            System.out.println("An unexpected error occurred.");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();

        } finally {

            // 5. Close scanner when application exits
            scanner.close();

            System.out.println(
                    "Application closed successfully."
            );
        }
    }
}
