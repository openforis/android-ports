package org.springframework.jdbc.core.support;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * 
 * @author S. Ricci
 * @author K. Waga
 *
 */
public abstract class JdbcDaoSupport {

	private DataSource dataSource;
	
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
}