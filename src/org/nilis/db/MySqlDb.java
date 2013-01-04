package org.nilis.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class MySqlDb {
	
	protected Connection conn;
	protected Statement stat;
	protected ResultSet resultSet;
	
	public MySqlDb(String host, int port, String user, String pass, String db) {
		try {
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			String url = "jdbc:mysql://"+host+":"+port+"/"+db;
            conn = DriverManager.getConnection(url, user, pass);
            stat = conn.createStatement();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
	}
	
	public ResultSet query(String query) {
		try {
			return stat.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void exec(String query) {
		try {
			stat.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
            if (resultSet != null) {
            	resultSet.close();
            }
            if (stat != null) {
            	stat.close();
            }
            if (conn != null) {
            	conn.close();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
	}
}
