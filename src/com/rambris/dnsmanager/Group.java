/* Group.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rambris.Database;
import com.rambris.Util;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: Group.java 50 2010-09-04 22:38:59Z boost $
 */
public class Group implements Comparable<Group>
{
	private final Database db;
	private int id;
	private String name;

	private int viewId = 0;
	private View view = null;
	private short viewPriority = 0;

	public Group(Database db)
	{
		this.db = db;
	}

	private Group(Database db, ResultSet rs) throws SQLException
	{
		this.db = db;
		loadResultSet(rs);
	}

	private void loadResultSet(ResultSet rs) throws SQLException
	{
		id = rs.getInt("group_id");
		name = rs.getString("group_name");
		Map<String, Integer> cols = Database.getColumns(rs);
		if (cols.containsKey("view_id")) viewId = rs.getInt("view_id");
		if (cols.containsKey("view_priority")) viewPriority = rs.getShort("view_priority");

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

	public View getView() throws NotFoundException, SQLException
	{
		if (view != null) return view;
		else if (viewId != 0) return view = View.GetViewById(db, viewId);
		else throw new NotFoundException("No view set");
	}

	public boolean isViewSet()
	{
		return viewId != 0;
	}

	/**
	 * @return the viewPriority
	 */
	public short getViewPriority()
	{
		return viewPriority;
	}

	/**
	 * @param viewPriority
	 *            the viewPriority to set
	 */
	public void setViewPriority(short viewPriority)
	{
		this.viewPriority = viewPriority;
	}

	public static List<Group> GetGroups(Database db) throws SQLException
	{
		List<Group> groups = new LinkedList<Group>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM `group` ORDER BY group_name");
			while (rs.next())
			{
				groups.add(new Group(db, rs));
			}
			return groups;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public static List<Group> GetViewGroups(Database db, View view) throws SQLException
	{
		List<Group> groups = GetViewGroups(db, view.getId());
		for (Group group : groups)
		{
			if (group.viewId != 0) group.view = view;
		}
		return groups;
	}

	public static List<Group> GetViewGroups(Database db, int viewId) throws SQLException
	{
		List<Group> groups = new LinkedList<Group>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `group` JOIN group_view ON group_view.group_id=group.group_id AND view_id=? ORDER BY view_priority, group_name");
			stmt.setInt(1, viewId);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				groups.add(new Group(db, rs));
			}
			return groups;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public static Group GetGroupById(Database db, int id) throws SQLException, NotFoundException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `group` WHERE group_id=?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) { return new Group(db, rs); }
			throw new NotFoundException("No group by id " + id);
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public static Group GetGroupByName(Database db, String name) throws SQLException, NotFoundException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `group` WHERE group_name=?");
			stmt.setString(1, name);
			rs = stmt.executeQuery();
			if (rs.next()) { return new Group(db, rs); }
			throw new NotFoundException("No group by name " + name);
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
			stmt = conn.prepareStatement("DELETE FROM `group` WHERE group_id=?");
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
		finally
		{
			db.closeAll(conn, stmt, null);
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
				stmt = conn.prepareStatement("INSERT INTO `group` (group_name) VALUES (?)");
				stmt.setString(1, name);
				stmt.executeUpdate();
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				if (rs.next())
				{
					id = rs.getInt(1);
				}
			}
			else
			{
				stmt = conn.prepareStatement("UPDATE `group` SET group_name=? WHERE group_id=?");
				stmt.setString(1, name);
				stmt.setInt(2, id);
				stmt.executeUpdate();
			}
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public String toString()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Group group)
	{
		return group.name.compareToIgnoreCase(name);
	}

	public boolean equals(Object object)
	{
		if (object == this) return true;
		if (object instanceof Group)
		{
			Group group = (Group) object;
			return group.id == id;
		}
		return super.equals(object);
	}
}
