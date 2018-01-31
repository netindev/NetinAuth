package com.huskehhh.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import gq.netin.auth.util.Messages;
import gq.netin.auth.util.Util;

/**
 * Connects to and uses a MySQL database
 *
 * @author -_Husky_-
 * @author tips48
 */
public class MySQL extends Database {

	private final String user;
	private final String database;
	private final String password;
	private final String port;
	private final String hostname;

	public MySQL(String hostname, String port, String database, String username, String password) {
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.user = username;
		this.password = password;
	}

	@Override
	public Connection openConnection() throws SQLException, ClassNotFoundException {
		if (this.checkConnection()) {
			return this.connection;
		}

		String connectionURL = "jdbc:mysql://" + this.hostname + ":" + this.port;
		if (this.database != null) {
			connectionURL = connectionURL + "/" + this.database;
		}

		Class.forName("com.mysql.jdbc.Driver");
		this.connection = DriverManager.getConnection(connectionURL, this.user, this.password);

		Util.info(Messages.CONNECTED_WITH_MYSQL);

		return this.connection;
	}

}
