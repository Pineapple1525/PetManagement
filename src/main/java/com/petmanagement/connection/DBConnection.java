package com.petmanagement.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection conn;

    public DBConnection() { }

    public static Connection getConn() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petshop", "root", "");
                System.out.println("✅ Kết nối thành công!");
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("❌ Lỗi kết nối đến MySQL", e);
        }
        return conn;
    }
    // ✅ Hàm test kết nối CSDL
    public static void main(String[] args) {
        Connection conn = DBConnection.getConn();
        if (conn != null) {
            System.out.println("✅ Đã kết nối đến CSDL thành công!");
        } else {
            System.out.println("❌ Kết nối CSDL thất bại!");
        }

//        DBConnection.close();
    }
}
