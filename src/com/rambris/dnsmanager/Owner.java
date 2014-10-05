/* Owner.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.rambris.Database;
import com.rambris.Util;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: Owner.java 51 2010-09-07 20:53:50Z boost $
 * 
 */
public class Owner
{
	private final Database db;
	private int id;
	private String name;
	private String email;

	public Owner(Database db)
	{
		this.db = db;
	}

	private Owner(Database db, ResultSet rs) throws SQLException
	{
		this.db = db;
		loadResultSet(rs);
	}

	private void loadResultSet(ResultSet rs) throws SQLException
	{
		id = rs.getInt("owner_id");
		name = rs.getString("owner_name");
		email = rs.getString("owner_email");
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
		this.name = Util.truncateString(name, 40).replace(' ', '_').replaceAll("[^a-zA-Z0-9_-]", "");
	}

	/**
	 * @return the email
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email)
	{
		this.email = Util.truncateString(email, 40).toLowerCase();
	}

	public static List<Owner> GetOwners(Database db) throws SQLException
	{
		List<Owner> owners = new LinkedList<Owner>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM `owner` ORDER BY owner_name");
			while (rs.next())
			{
				owners.add(new Owner(db, rs));
			}
			return owners;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public static Owner GetOwnerById(Database db, int id) throws SQLException, NotFoundException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `owner` WHERE owner_id=?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				return new Owner(db, rs);
			}
			throw new NotFoundException("No owner by id " + id);
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public static Owner GetOwnerByName(Database db, String name) throws SQLException, NotFoundException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `owner` WHERE owner_name=?");
			stmt.setString(1, name);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				return new Owner(db, rs);
			}
			throw new NotFoundException("No owner by name " + name);
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
			stmt = conn.prepareStatement("DELETE FROM `owner` WHERE owner_id=?");
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
				stmt = conn.prepareStatement("INSERT INTO `owner` (owner_name, owner_email) VALUES (?, ?)");
				stmt.setString(1, name);
				stmt.setString(2, email);
				stmt.executeUpdate();
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				if (rs.next())
				{
					id = rs.getInt(1);
				}
			}
			else
			{
				stmt = conn.prepareStatement("UPDATE `owner` SET owner_name=?, owner_email=? WHERE owner_id=?");
				stmt.setString(1, name);
				stmt.setString(2, email);
				stmt.setInt(3, id);
				stmt.executeUpdate();
			}
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	@Override
	public String toString()
	{
		return name;
	}
}
