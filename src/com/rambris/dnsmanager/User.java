/* User.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.rambris.Crypto;
import com.rambris.Database;
import com.rambris.Util;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 * 
 */
public class User implements Comparable<User>
{
	protected final Database db;
	protected int id;
	protected String name;
	protected String encryptedPassword;
	protected String salt;
	protected boolean superAdmin;
	private static Crypto crypto = new Crypto();
	protected List<Domain> domains = null;

	public User(Database db)
	{
		this.db = db;
	}

	protected User(Database db, ResultSet rs) throws SQLException
	{
		this.db = db;
		loadResultSet(rs);
	}

	private void loadResultSet(ResultSet rs) throws SQLException
	{
		id = rs.getInt("user_id");
		name = rs.getString("user_name");
		encryptedPassword = rs.getString("password");
		salt = rs.getString("salt");
		superAdmin = rs.getBoolean("superadmin");
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
		if (name != null) this.name = Util.truncateString(name.toLowerCase().replaceAll("[^a-z]", ""), 64);
		else this.name = name;
	}

	/**
	 * @return the superAdmin
	 */
	public boolean isSuperAdmin()
	{
		return superAdmin;
	}

	/**
	 * @param superAdmin
	 *            the superAdmin to set
	 */
	public void setSuperAdmin(boolean superAdmin)
	{
		this.superAdmin = superAdmin;
	}

	public void setPassword(String password)
	{
		if (salt == null || (salt != null && salt.isEmpty())) salt = crypto.hash(crypto.GetGUID());
		encryptedPassword = encryptPassword(password);
	}

	public String encryptPassword(String password)
	{
		return crypto.hash("The password " + password + " for user " + name + " is salted with the salt " + salt);
	}

	public boolean isValidPassword(String password)
	{
		return encryptPassword(password).equals(encryptedPassword);
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(User user)
	{
		return toString().compareTo(user.toString());
	}

	@Override
	public boolean equals(Object object)
	{
		if (object == this) return true;
		if (object instanceof User)
		{
			User user = (User) object;
			return compareTo(user) == 0;
		}
		return super.equals(object);
	}

	protected boolean load(int id) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `user` WHERE `user_id`=?");
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
			if (id == 0)
			{
				stmt = conn.prepareStatement("INSERT INTO `user` (user_name, password, salt, superadmin) VALUES (?, ?, ?, ?)");
				stmt.setString(1, name);
				stmt.setString(2, encryptedPassword);
				stmt.setString(3, salt);
				stmt.setBoolean(4, superAdmin);
				stmt.executeUpdate();
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				if (rs.next())
				{
					id = rs.getInt(1);
				}
				db.closeResultSet(rs);
				rs = null;
				db.closeStatement(stmt);
				stmt = null;
			}
			else
			{
				stmt = conn.prepareStatement("UPDATE `user` SET `user_name`=?, `password`=?, `salt`=?, `superadmin`=? WHERE user_id=?");
				stmt.setString(1, name);
				stmt.setString(2, encryptedPassword);
				stmt.setString(3, salt);
				stmt.setBoolean(4, superAdmin);
				stmt.setInt(5, id);
				stmt.executeUpdate();
				db.closeStatement(stmt);
				stmt = null;
			}
			if (domains != null)
			{
				stmt = conn.prepareStatement("INSERT INTO `domain_admin` (`domain_id`, `user_id`) VALUES (?, ?)");
				stmt.executeUpdate("DELETE FROM `domain_admin` WHERE `user_id`=" + id);
				stmt.setInt(2, id);
				for (Domain domain : domains)
				{
					stmt.setInt(1, domain.getId());
					stmt.executeUpdate();
				}
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
			stmt = conn.prepareStatement("DELETE FROM `user` WHERE user_id=?");
			stmt.setInt(1, id);
			stmt.executeUpdate();
		}
		finally
		{
			db.closeAll(conn, stmt, null);
		}
	}

	public static User GetUserById(Database db, int id) throws SQLException, NotFoundException
	{
		User user = new User(db);
		if (user.load(id)) return user;
		else throw new NotFoundException("No user by id " + id);
	}

	public static User GetUserByName(Database db, String name) throws SQLException, NotFoundException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `user` WHERE user_name=?");
			stmt.setString(1, name);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				return new User(db, rs);
			}
			throw new NotFoundException("No user by name " + name);
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public static User GetUserByHash(Database db, String hash) throws SQLException, NotFoundException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `user` WHERE SHA1(CONCAT(user_id,user_name,password,salt))=?");
			stmt.setString(1, hash);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				return new User(db, rs);
			}
			throw new NotFoundException();
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public static User AuthenticateUser(Database db, String userName, String password) throws SQLException
	{
		try
		{
			User user = GetUserByName(db, userName);
			if (user.isValidPassword(password)) return user;
			else return null;

		}
		catch (NotFoundException e)
		{
			return null;
		}
	}

	public static User AuthenticateUser(Database db, String hash) throws SQLException
	{
		try
		{
			User user = GetUserByHash(db, hash);
			return user;
		}
		catch (NotFoundException e)
		{
			return null;
		}
	}

	public static List<User> GetUsers(Database db) throws SQLException
	{
		List<User> users = new LinkedList<User>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM `user` ORDER BY user_name");
			while (rs.next())
			{
				users.add(new User(db, rs));
			}
			return users;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public String getAuthHash()
	{
		return crypto.hash(id + name + encryptedPassword + salt);
	}

	public List<Domain> getDomains() throws SQLException
	{
		if (domains != null) return domains;
		else
		{
			domains = Domain.GetUserDomains(db, this);
			return domains;
		}
	}

	public void clearDomains()
	{
		domains = new LinkedList<Domain>();
	}

	public void addDomain(Domain domain)
	{
		if (domains == null) clearDomains();
		domains.add(domain);
	}

	public boolean hasDomain(Domain domain)
	{
		if (domains == null) return false;
		for (Domain d : domains)
		{
			if (domain.getId() == d.getId()) return true;
		}
		return false;
	}

	public boolean hasDomain(String domain_name)
	{
		if (domains == null) return false;
		for (Domain d : domains)
		{
			if (d.getName().equals(domain_name)) return true;
		}
		return false;
	}

	public boolean isAuthorizedFor(Domain domain) throws SQLException
	{
		if (superAdmin) return true;
		getDomains();
		return hasDomain(domain);
	}

	public boolean isAuthorizedFor(String domain_name) throws SQLException
	{
		if (superAdmin) return true;
		getDomains();
		return hasDomain(domain_name);
	}
}
