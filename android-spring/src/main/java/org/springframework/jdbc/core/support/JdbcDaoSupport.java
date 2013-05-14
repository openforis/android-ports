package org.springframework.jdbc.core.support;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

//move to new project
public class JdbcDaoSupport {

	private static Connection connection;
	private static DataSource dataSource;
	
	public static void init(DataSource dataSource) {
		JdbcDaoSupport.dataSource = dataSource;
	}
	
	public Connection getConnection() {
		try {
			if ( connection == null || connection.isClosed() ) {
				connection = dataSource.getConnection();
			}
		} catch (Exception e) {
			connection = null;
		} 
		return connection;
	}
	
	public static void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isOpen(){
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			return false;
		} catch (NullPointerException e){
			return false;
		}
	}
	
	public static boolean isClosed(){
		if ( connection == null ) {
			return false;
		} else {
			try {
				return connection.isClosed();
			} catch (SQLException e) {
				return true;
			}
		}
	}
}