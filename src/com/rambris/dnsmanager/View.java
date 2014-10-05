/* View.java (c) 2010-2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rambris.Database;
import com.rambris.Util;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: View.java 51 2010-09-07 20:53:50Z boost $
 */
public class View implements Comparable<View>
{
	protected final Database db;
	protected int id;
	protected int serverId;
	protected Server server = null;
	protected String name;
	protected String description;
	protected String address;
	protected boolean notify;
	protected List<Group> groups = null;

	public View(Database db)
	{
		this.db = db;
	}

	public View(Database db, Server server)
	{
		this.db = db;
		this.server = server;
		this.serverId = server.getId();
	}

	protected View(Database db, ResultSet rs) throws SQLException
	{
		this.db = db;
		loadResultSet(rs);
	}

	private void loadResultSet(ResultSet rs) throws SQLException
	{
		Map<String, Integer> cols = Database.getColumns(rs);
		id = rs.getInt("view_id");
		if (cols.containsKey("server_id")) serverId = rs.getInt("server_id");
		name = rs.getString("view_name");
		description = rs.getString("view_description");
		if (cols.containsKey("view_address")) address = rs.getString("view_address");
		if (cols.containsKey("notify")) notify = rs.getBoolean("notify");
	}

	protected boolean load(int id) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM view WHERE view_id=?");
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

	public void save() throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			conn.setAutoCommit(false);
			if (id == 0)
			{
				stmt = conn.prepareStatement("INSERT INTO `view` (server_id, view_name, view_description, view_address, notify) VALUES (?, ?, ?, ?, ?)");
				stmt.setInt(1, serverId);
				stmt.setString(2, name);
				stmt.setString(3, description);
				stmt.setString(4, address);
				stmt.setBoolean(5, notify);
				stmt.executeUpdate();
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				if (rs.next())
				{
					id = rs.getInt(1);
				}
			}
			else
			{
				stmt = conn.prepareStatement("UPDATE `view` SET server_id=?, view_name=?, view_description=?, view_address=?, notify=? WHERE view_id=?");
				stmt.setInt(1, serverId);
				stmt.setString(2, name);
				stmt.setString(3, description);
				stmt.setString(4, address);
				stmt.setBoolean(5, notify);
				stmt.setInt(6, id);
				stmt.executeUpdate();
			}
			db.closeStatement(stmt);
			stmt = null;

			if (groups != null)
			{
				stmt = conn.prepareStatement("INSERT INTO `group_view` (group_id, view_id, view_priority) VALUES (?, ?, ?)");
				stmt.executeUpdate("DELETE FROM `group_view` WHERE view_id=" + id);
				for (Group group : groups)
				{
					stmt.setInt(1, group.getId());
					stmt.setInt(2, id);
					stmt.setShort(3, group.getViewPriority());
					stmt.executeUpdate();
				}
			}
			conn.commit();
			conn.setAutoCommit(true);
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
			stmt = conn.prepareStatement("DELETE FROM view WHERE view_id=?");
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
		finally
		{
			db.closeAll(conn, stmt, null);
		}
	}

	public static View GetViewById(Database db, int id) throws NotFoundException, SQLException
	{
		View view = new View(db);
		if (view.load(id)) return view;
		else throw new NotFoundException("No view by id " + id);
	}

	public static List<View> GetServerViews(Database db, Server server) throws SQLException
	{
		List<View> views = GetServerViews(db, server.getId());
		for (View view : views)
			view.server = server;
		return views;
	}

	public static List<View> GetServerViews(Database db, int serverId) throws SQLException
	{
		List<View> views = new LinkedList<View>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM view WHERE server_id=? ORDER BY view_name");
			stmt.setInt(1, serverId);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				views.add(new View(db, rs));
			}
			return views;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}

	}

	public static View GetServerViewByName(Database db, Server server, String name) throws SQLException, NotFoundException
	{
		View view = GetServerViewByName(db, server.getId(), name);
		view.server = server;
		return view;
	}

	public static View GetServerViewByName(Database db, int serverId, String name) throws SQLException, NotFoundException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM view WHERE server_id=? AND view_name=?");
			stmt.setInt(1, serverId);
			stmt.setString(2, name);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				return new View(db, rs);
			}
			else throw new NotFoundException("No view by name " + name + " for server " + serverId);
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}

	}

	public List<Domain> getDomains() throws SQLException
	{
		return Domain.GetViewDomains(db, this);
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = Util.truncateString(name, 50).toLowerCase();
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return the address
	 */
	public String getAddress()
	{
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address)
	{
		this.address = Util.truncateString(address, 255);
	}

	/**
	 * @return the notify
	 */
	public boolean isNotify()
	{
		return notify;
	}

	/**
	 * @param notify
	 *            the notify to set
	 */
	public void setNotify(boolean notify)
	{
		this.notify = notify;
	}

	/**
	 * @return the serverId
	 */
	public int getServerId()
	{
		return serverId;
	}

	public Server getServer() throws NotFoundException, SQLException
	{
		if (serverId == 0) throw new NotFoundException("Server not set");
		else if (server != null) return server;
		else return server = Server.GetServerById(db, serverId);
	}

	public List<Group> getGroups() throws SQLException
	{
		if (groups != null) return groups;
		return groups = Group.GetViewGroups(db, this);
	}

	public void addGroup(Group group, short priority)
	{
		if (group.getId() == 0) return;
		group.setViewPriority(priority);
		if (groups == null) groups = new LinkedList<Group>();
		groups.add(group);
	}

	public static List<View> GetExtraServers(Database db, Domain domain) throws SQLException
	{
		List<View> extraServers = new LinkedList<View>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn
					.prepareStatement("SELECT view.view_id, hostname, view_name, view_description, extra_server.role FROM view NATURAL JOIN server LEFT JOIN extra_server ON extra_server.view_id=view.view_id AND domain_id=? WHERE server_id NOT IN (SELECT server_id FROM domain WHERE domain_id=?) ORDER BY hostname, view_name");
			stmt.setLong(1, domain.getId());
			stmt.setLong(2, domain.getId());
			rs = stmt.executeQuery();
			while (rs.next())
			{
				extraServers.add(new View(db, rs));
			}
			return extraServers;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	@Override
	public String toString()
	{
		try
		{
			return getServer().getHostname() + ": " + getName();
		}
		catch (NotFoundException e)
		{
			return getAddress();
		}
		catch (SQLException e)
		{
			return getAddress();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(View view)
	{
		return toString().compareTo(view.toString());
	}

	public boolean equals(Object object)
	{
		if (object == this) return true;
		if (object instanceof View)
		{
			View view = (View) object;
			return compareTo(view) == 0;
		}
		return super.equals(object);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	public boolean isMasterOf(Domain domain) throws SQLException
	{
		for (Master master : domain.getMasters())
		{
			if (master.equals(this)) return true;
		}
		return false;
	}

}
