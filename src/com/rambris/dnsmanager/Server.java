/* Server.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import com.rambris.Database;
import com.rambris.Util;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: Server.java 51 2010-09-07 20:53:50Z boost $
 */
public class Server implements Comparable<Server>
{
	public static Server GetServerByHostname(Database db, String hostname) throws SQLException, NotFoundException
	{
		Server server = new Server(db);
		if (server.load(hostname)) return server;
		else throw new NotFoundException("No server by name " + hostname);
	}

	public static Server GetServerById(Database db, int id) throws SQLException, NotFoundException
	{
		Server server = new Server(db);
		if (server.load(id)) return server;
		else throw new NotFoundException("No server by ID " + id);
	}

	public static List<Server> GetServers(Database db) throws SQLException
	{
		List<Server> servers = new LinkedList<Server>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM server ORDER BY hostname");
			while (rs.next())
			{
				servers.add(new Server(db, rs));
			}
			return servers;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}

	}

	private final Database db;
	private String hostname = null;
	private int id = 0;

	private String masterPrefix = null;
	private String slavePrefix = null;

	private String scpAddress = null;
	private String zonePath = null;
	private String configPath = null;
	private String reloadCommand = "/usr/sbin/rndc reload";

	public Server(Database db)
	{
		this.db = db;
	}

	private Server(Database db, ResultSet rs) throws SQLException
	{
		this.db = db;
		loadResultSet(rs);
	}

	/**
	 * @return the hostname
	 */
	public String getHostname()
	{
		return hostname;
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return the masterPrefix
	 */
	public String getMasterPrefix()
	{
		return masterPrefix;
	}

	/**
	 * @return the slavePrefix
	 */
	public String getSlavePrefix()
	{
		return slavePrefix;
	}

	private boolean load(int id) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM server WHERE server_id=?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				loadResultSet(rs);
				return true;
			}
			else return false;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	private boolean load(String hostname) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM server WHERE hostname=?");
			stmt.setString(1, hostname);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				loadResultSet(rs);
				return true;
			}
			else return false;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}

	}

	public void save() throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			if (id == 0)
			{
				stmt = conn.prepareStatement("INSERT INTO server (hostname, master_prefix, slave_prefix, scp_address, zone_path, config_path, reload_command) VALUES (?, ?, ?, ?, ?, ?, ?)");
				stmt.setString(1, hostname);
				stmt.setString(2, masterPrefix);
				stmt.setString(3, slavePrefix);
				if (scpAddress != null) stmt.setString(4, scpAddress);
				else stmt.setNull(4, Types.VARCHAR);
				stmt.setString(5, zonePath);
				stmt.setString(6, configPath);
				if (reloadCommand != null) stmt.setString(7, reloadCommand);
				else stmt.setNull(7, Types.VARCHAR);
				stmt.executeUpdate();
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				if (rs.next())
				{
					id = rs.getInt(1);
				}
			}
			else
			{
				stmt = conn.prepareStatement("UPDATE server SET hostname=?, master_prefix=?, slave_prefix=?, scp_address=?, zone_path=?, config_path=?, reload_command=? WHERE server_id=?");
				stmt.setString(1, hostname);
				stmt.setString(2, masterPrefix);
				stmt.setString(3, slavePrefix);
				if (scpAddress != null) stmt.setString(4, scpAddress);
				else stmt.setNull(4, Types.VARCHAR);
				stmt.setString(5, zonePath);
				stmt.setString(6, configPath);
				if (reloadCommand != null) stmt.setString(7, reloadCommand);
				else stmt.setNull(7, Types.VARCHAR);
				stmt.setInt(8, id);
				stmt.executeUpdate();
			}
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public void delete() throws SQLException
	{
		if (id == 0) return;
		Connection conn = null;
		PreparedStatement stmt = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("DELETE FROM server WHERE server_id=?");
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
		finally
		{
			db.closeAll(conn, stmt, null);
		}
	}

	/**
	 * @param rs
	 * @throws SQLException
	 */
	private void loadResultSet(ResultSet rs) throws SQLException
	{
		id = rs.getInt("server_id");
		hostname = rs.getString("hostname");
		masterPrefix = rs.getString("master_prefix");
		slavePrefix = rs.getString("slave_prefix");
		scpAddress = rs.getString("scp_address");
		zonePath = rs.getString("zone_path");
		configPath = rs.getString("config_path");
		setReloadCommand(rs.getString("reload_command"));
	}

	/**
	 * @param hostname
	 *            the hostname to set
	 * @throws InvalidHostnameException
	 */
	public void setHostname(String hostname) throws InvalidHostnameException
	{
		if (hostname != null)
		{
			if (!DNS.validHostname(hostname)) throw new InvalidHostnameException(hostname + " is not a valid DNS hostname");
			this.hostname = hostname.toLowerCase();
		}
		else
		{
			this.hostname = hostname;
		}
	}

	/**
	 * @param masterPrefix
	 *            the masterPrefix to set
	 */
	public void setMasterPrefix(String masterPrefix)
	{
		this.masterPrefix = Util.truncateString(masterPrefix, 45);
	}

	/**
	 * @param slavePrefix
	 *            the slavePrefix to set
	 */
	public void setSlavePrefix(String slavePrefix)
	{
		this.slavePrefix = Util.truncateString(slavePrefix, 45);
	}

	public List<View> getViews() throws SQLException, NotFoundException
	{
		if (id == 0) throw new NotFoundException("id not set");

		return View.GetServerViews(db, this);
	}

	public View getView(String viewName) throws SQLException, NotFoundException
	{
		return View.GetServerViewByName(db, this, viewName);
	}

	public List<Domain> getDomains() throws SQLException
	{
		return Domain.GetServerDomains(db, this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return hostname;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Server server)
	{
		return ((Integer) id).compareTo(server.id);
	}

	/**
	 * @param scpAddress
	 *            the scpHost to set
	 */
	public void setScpAddress(String scpAddress)
	{
		if (scpAddress != null && scpAddress.trim().isEmpty()) this.scpAddress = null;
		else if (scpAddress != null) this.scpAddress = scpAddress.trim();
		else this.scpAddress = scpAddress;
	}

	/**
	 * @return the scpHost
	 */
	public String getScpAddress()
	{
		return scpAddress;
	}

	/**
	 * @param zonePath
	 *            the zonePath to set
	 */
	public void setZonePath(String zonePath)
	{
		this.zonePath = zonePath;
	}

	/**
	 * @return the zonePath
	 */
	public String getZonePath()
	{
		return zonePath;
	}

	/**
	 * @param configPath
	 *            the configPath to set
	 */
	public void setConfigPath(String configPath)
	{
		this.configPath = configPath;
	}

	/**
	 * @return the configPath
	 */
	public String getConfigPath()
	{
		return configPath;
	}

	public String getMasterZonePath()
	{
		return zonePath + (!zonePath.endsWith("/") && !masterPrefix.startsWith("/") ? "/" : "") + masterPrefix;
	}

	public String getScpZonePath()
	{
		return (scpAddress != null ? scpAddress + ":" : "") + getMasterZonePath();
	}

	public String getScpConfigPath()
	{
		return (scpAddress != null ? scpAddress + ":" : "") + configPath;
	}

	public void setReloadCommand(String reloadCommand)
	{
		if (reloadCommand != null && reloadCommand.trim().isEmpty()) this.reloadCommand = null;
		else if (reloadCommand != null) this.reloadCommand = reloadCommand.trim();
		else this.reloadCommand = reloadCommand;
	}

	public String getReloadCommand()
	{
		return reloadCommand;
	}
}
