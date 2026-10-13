
package com.scc;

import com.scc.dao.DatabaseConnection;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();

        com.scc.ui.AdminMenu menu = new com.scc.ui.AdminMenu();
        menu.start();
    }
}

