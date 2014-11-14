/* Domain.java (c) 2010-2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.rambris.Database;
import com.rambris.Util;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: Domain.java 50 2010-09-04 22:38:59Z boost $
 */
public class Domain implements Comparable<Domain>, Cloneable
{
	private final Logger log = Logger.getLogger(getClass());
	private final Database db;
	private int id = 0;
	private String name;
	private int serverId = 0;
	private Server server = null;
	private int ownerId = 0;
	private Owner owner = null;
	private int ttl;
	private long serial;
	private int refresh;
	private int retry;
	private int expire;
	private int minimum;
	protected boolean active = true;
	private String comment = null;
	private Set<Master> masters = null;
	private Date created = null;
	private Date updated = null;
	private Date exported = null;
	private Date newestRecord = null;
	private HashSet<String> aliases = null;
	private int aliasCount = 0;
	private SOARecord soaRecord = null;
	/** Used in UserPage */
	private boolean selected;
	/** Used in ReportPage */
	private Domain parentDomain;

	public Domain(Database db)
	{
		this.db = db;
		loadDefaults();
	}

	private Domain(Database db, ResultSet rs) throws SQLException
	{
		this(db);
		loadResultSet(rs);
	}

	public void loadDefaults()
	{
		setTtl(21600);
		setSerial(1900000000l);
		bumpSerial();
		setRefresh(18000);
		setRetry(3600);
		setExpire(604800);
		setMinimum(21600);
	}

	private void loadResultSet(ResultSet rs) throws SQLException
	{
		id = rs.getInt("domain_id");
		name = rs.getString("domain_name");
		serverId = rs.getInt("server_id");
		ownerId = rs.getInt("owner_id");
		ttl = rs.getInt("ttl");
		serial = rs.getLong("serial");
		refresh = rs.getInt("refresh");
		retry = rs.getInt("retry");
		expire = rs.getInt("expire");
		minimum = rs.getInt("minimum");
		active = rs.getBoolean("active");
		created = Database.TimestampToDate(rs.getTimestamp("created"));
		updated = Database.TimestampToDate(rs.getTimestamp("updated"));
		exported = Database.TimestampToDate(rs.getTimestamp("exported"));
		comment = rs.getString("comment");
		if (Database.getColumns(rs).containsKey("alias_count"))
		{
			aliasCount = rs.getInt("alias_count");
		}
	}

	public static Domain GetDomainById(Database db, int id) throws SQLException, NotFoundException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `domain` WHERE domain_id=?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				return new Domain(db, rs);
			}
			throw new NotFoundException("No domain by id " + id);
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public static Domain GetDomainByName(Database db, String name) throws SQLException, NotFoundException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `domain` WHERE domain_name=?");
			stmt.setString(1, name);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				return new Domain(db, rs);
			}
			throw new NotFoundException("No domain by name " + name);
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
			stmt = conn.prepareStatement("DELETE FROM `domain` WHERE `domain_id`=?");
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
			conn.setAutoCommit(false);
			if (id == 0)
			{
				stmt = conn
						.prepareStatement("INSERT INTO `domain` (`created`, `updated`, `domain_name`, `server_id`, `owner_id`, `ttl`, `serial`, `refresh`, `retry`, `expire`, `minimum`, `active`, `comment`) VALUES (NOW(), NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				stmt.setString(1, name);
				stmt.setInt(2, serverId);
				stmt.setInt(3, ownerId);
				stmt.setInt(4, ttl);
				stmt.setLong(5, serial);
				stmt.setInt(6, refresh);
				stmt.setInt(7, retry);
				stmt.setInt(8, expire);
				stmt.setInt(9, minimum);
				stmt.setBoolean(10, active);
				if (comment != null) stmt.setString(11, comment);
				else stmt.setNull(11, Types.VARCHAR);
				stmt.executeUpdate();
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				if (rs.next())
				{
					id = rs.getInt(1);
				}
			}
			else
			{
				stmt = conn
						.prepareStatement("UPDATE `domain` SET `domain_name`=?, `server_id`=?, `owner_id`=?, `ttl`=?, `serial`=?, `refresh`=?, `retry`=?, `expire`=?, `minimum`=?, `active`=?, `comment`=? WHERE `domain_id`=?");
				stmt.setString(1, name);
				stmt.setInt(2, serverId);
				stmt.setInt(3, ownerId);
				stmt.setInt(4, ttl);
				stmt.setLong(5, serial);
				stmt.setInt(6, refresh);
				stmt.setInt(7, retry);
				stmt.setInt(8, expire);
				stmt.setInt(9, minimum);
				stmt.setBoolean(10, active);
				if (comment != null) stmt.setString(11, comment);
				else stmt.setNull(11, Types.VARCHAR);
				stmt.setInt(12, id);
				stmt.executeUpdate();
			}
			if (masters != null)
			{
				db.closeStatement(stmt);
				stmt = null;

				stmt = conn.prepareStatement("DELETE FROM `domain_view` WHERE domain_id=?");
				stmt.setLong(1, id);
				stmt.executeUpdate();
				db.closeStatement(stmt);
				stmt = null;

				stmt = conn.prepareStatement("INSERT INTO `domain_view` (`domain_id`, `master_view_id`, `slave_view_id`) VALUES ( ?, ?, ? )");

				for (Master master : getMasters())
				{
					stmt.setInt(1, id);
					stmt.setInt(2, master.getId());

					if (master.hasSlaves())
					{
						for (View slave : master.getSlaves())
						{
							stmt.setInt(3, slave.getId());
							stmt.executeUpdate();
						}
					}
					else
					{
						stmt.setInt(3, 0);
						stmt.executeUpdate();
					}

				}
			}
			if (aliases != null)
			{
				db.closeResultSet(rs);
				rs = null;
				db.closeStatement(stmt);
				stmt = null;
				stmt = conn.prepareStatement("INSERT INTO `domain_alias` (`domain_id`, `domain_name`) VALUES (?, ?)");
				stmt.executeUpdate("DELETE FROM `domain_alias` WHERE `domain_id`=" + id);
				if (aliases.size() > 0)
				{
					stmt.setInt(1, id);
					PreparedStatement lookupStatement = null;
					try
					{
						lookupStatement = conn
								.prepareStatement("SELECT `domain_id` FROM domain WHERE domain.domain_name=? UNION SELECT `domain_id` FROM `domain_alias` WHERE domain_alias.domain_name=?");
						for (String alias : aliases)
						{
							if (domainNameExists(lookupStatement, alias)) throw new SQLException("Domain name not unique");
							stmt.setString(2, alias);
							stmt.addBatch();
						}
						stmt.executeBatch();
					}
					finally
					{
						db.closeStatement(lookupStatement);
					}
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

	private boolean domainNameExists(PreparedStatement lookupStatement, String domainName) throws SQLException
	{
		ResultSet rs = null;
		try
		{
			lookupStatement.setString(1, domainName);
			lookupStatement.setString(2, domainName);
			rs = lookupStatement.executeQuery();
			if (rs.next()) return true;
			return false;
		}
		finally
		{
			db.closeResultSet(rs);
		}

	}

	public void setExported() throws SQLException
	{
		exported = new Date();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			if (id != 0)
			{
				stmt = conn.prepareStatement("UPDATE `domain` SET `exported`=? WHERE `domain_id`=?");
				stmt.setTimestamp(1, Database.DateToTimestamp(exported));
				stmt.setInt(2, id);
				stmt.executeUpdate();
			}
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public void clearExported() throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		try
		{
			conn = db.getConnection();
			if (id != 0)
			{
				stmt = conn.prepareStatement("UPDATE `domain` SET `exported`=NULL, `updated`=`updated` WHERE `domain_id`=?");
				stmt.setInt(1, id);
				stmt.executeUpdate();
			}
		}
		finally
		{
			db.closeAll(conn, stmt, null);
		}
	}

	public Date getExported()
	{
		return exported;
	}

	public boolean shouldExport() throws SQLException, NotFoundException, InvalidDataException
	{
		if (!active) return false;
		if (exported == null) return true;
		long updated = Math.max(this.updated.getTime(), getNewestRecord().getTime());
		return updated > exported.getTime();
	}

	// Bean
	public boolean isShouldExport() throws SQLException, NotFoundException, InvalidDataException
	{
		return shouldExport();
	}

	public void touch() throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		try
		{
			if (id != 0)
			{
				conn = db.getConnection();
				stmt = conn.prepareStatement("UPDATE `domain` SET `updated`=NOW() WHERE `domain_id`=?");
				stmt.setInt(1, id);
				stmt.executeUpdate();
			}
		}
		finally
		{
			db.closeAll(conn, stmt, null);
		}
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	public void clear()
	{
		id = 0;
		name = "";
		setSerial(1900000000l);
		bumpSerial();
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
		if (name != null) this.name = name.trim().toLowerCase();
		else this.name = name;
	}

	/**
	 * @return the server
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public Server getServer() throws SQLException, NotFoundException
	{
		if (serverId == 0) throw new NotFoundException("No serverid");
		if (server != null) return server;
		else return server = Server.GetServerById(db, serverId);
	}

	/**
	 * @param server
	 *            the server to set
	 */
	public void setServer(Server server)
	{
		this.server = server;
		if (server != null) serverId = server.getId();
		else serverId = 0;
	}

	/**
	 * @return the owner
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public Owner getOwner() throws SQLException, NotFoundException
	{
		if (ownerId == 0) throw new NotFoundException("No ownerid");
		if (owner != null) return owner;
		else return owner = Owner.GetOwnerById(db, ownerId);
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(Owner owner)
	{
		this.owner = owner;
		if (owner != null) ownerId = owner.getId();
		else ownerId = 0;
	}

	/**
	 * @return the ttl as string
	 */
	public String getTtl()
	{
		return GenerateTime(ttl);
	}

	/**
	 * TTL in seconds
	 * 
	 * @return
	 */
	public int getTtlSeconds()
	{
		return ttl;
	}

	/**
	 * @param ttl
	 *            the ttl to set
	 */
	public void setTtl(int ttl)
	{
		this.ttl = ttl;
	}

	public void setTtl(String ttl)
	{
		this.ttl = ParseTime(ttl);
	}

	/**
	 * @return the serial
	 */
	public long getSerial()
	{
		return serial;
	}

	/**
	 * @param serial
	 *            the serial to set
	 */
	public void setSerial(long serial)
	{
		this.serial = serial;
	}

	/**
	 * @return the refresh
	 */
	public String getRefresh()
	{
		return GenerateTime(refresh);
	}

	/**
	 * @param refresh
	 *            the refresh to set
	 */
	public void setRefresh(int refresh)
	{
		this.refresh = refresh;
	}

	public void setRefresh(String refresh)
	{
		this.refresh = ParseTime(refresh);
	}

	/**
	 * @return the retry
	 */
	public String getRetry()
	{
		return GenerateTime(retry);
	}

	/**
	 * @param retry
	 *            the retry to set
	 */
	public void setRetry(int retry)
	{
		this.retry = retry;
	}

	public void setRetry(String retry)
	{
		this.retry = ParseTime(retry);
	}

	/**
	 * @return the expire
	 */
	public String getExpire()
	{
		return GenerateTime(expire);
	}

	/**
	 * @param expire
	 *            the expire to set
	 */
	public void setExpire(int expire)
	{
		this.expire = expire;
	}

	public void setExpire(String expire)
	{
		this.expire = ParseTime(expire);
	}

	/**
	 * @return the minimum
	 */
	public String getMinimum()
	{
		return GenerateTime(minimum);
	}

	/**
	 * @param minimum
	 *            the minimum to set
	 */
	public void setMinimum(int minimum)
	{
		this.minimum = minimum;
	}

	public void setMinimum(String minimum)
	{
		this.minimum = ParseTime(minimum);
	}

	/**
	 * @return the active
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}

	/**
	 * @return the serverId
	 */
	public int getServerId()
	{
		return serverId;
	}

	/**
	 * @return the ownerId
	 */
	public int getOwnerId()
	{
		return ownerId;
	}

	public Map<String, List<Record>> getRecords() throws SQLException, NotFoundException, InvalidDataException
	{
		Map<String, List<Record>> records = Record.GetDomainRecords(db, this);
		return records;
	}

	public Date getNewestRecord() throws SQLException, NotFoundException, InvalidDataException
	{
		if (newestRecord != null) return newestRecord;
		long latest = 0;
		for (List<Record> recordList : getRecords().values())
		{
			for (Record record : recordList)
			{
				if (record.getUpdated().getTime() > latest) latest = record.getUpdated().getTime();
			}
		}
		return newestRecord = new Date(latest);
	}

	public void bumpSerial()
	{
		if (serial < 1900000000l) serial++;
		else
		{
			String serialString = Long.toString(serial);
			String datePart = serialString.substring(0, 8);
			String serialPart = serialString.substring(8);
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			String currentDate = df.format(new Date());
			if (Long.parseLong(datePart) < Long.parseLong(currentDate))
			{
				datePart = currentDate;
				serialPart = "01";
				serial = Long.parseLong(datePart + serialPart);
			}
			else
			{
				serial++;
			}
		}
	}

	/**
	 * Converts seconds to DNS times if it is an even minute, hour, day or week
	 * 
	 * @param seconds
	 * @return
	 */
	public static String GenerateTime(int seconds)
	{
		if (seconds != 0 && (seconds % 604800 == 0)) return (seconds / 604800) + "w";
		else if (seconds != 0 && (seconds % 86400 == 0)) return (seconds / 86400) + "d";
		else if (seconds != 0 && (seconds % 3600 == 0)) return (seconds / 3600) + "h";
		else if (seconds != 0 && (seconds % 60 == 0)) return (seconds / 60) + "m";
		return seconds + "";
	}

	public static String GenerateHumanTime(int seconds)
	{
		if (seconds != 0 && (seconds % 604800 == 0))
		{
			int ret = seconds / 604800;
			return ret + " week" + (ret > 1 ? "s" : "");
		}
		else if (seconds != 0 && (seconds % 86400 == 0))
		{
			int ret = seconds / 86400;
			return ret + " day" + (ret > 1 ? "s" : "");
		}
		else if (seconds != 0 && (seconds % 3600 == 0))
		{
			int ret = seconds / 3600;
			return ret + " hour" + (ret > 1 ? "s" : "");
		}
		else if (seconds != 0 && (seconds % 60 == 0))
		{
			int ret = seconds / 60;
			return ret + " minute" + (ret > 1 ? "s" : "");
		}
		return seconds + "";
	}

	private static Pattern timePattern = Pattern.compile("(\\d+)\\s*([mhdw])");

	/**
	 * Converts DNS times like 1m and 3w to seconds
	 * 
	 * @param string
	 * @return
	 */
	public static int ParseTime(String string)
	{
		Matcher matcher = timePattern.matcher(string.trim());
		if (matcher.find())
		{
			char suffix = matcher.group(2).charAt(0);
			int value = Integer.parseInt(matcher.group(1));

			switch (suffix)
			{
				case 'w':
					return value * 604800;
				case 'd':
					return value * 86400;
				case 'h':
					return value * 3600;
				case 'm':
					return value * 60;
			}
			return 0;
		}
		else
		{
			try
			{
				int value = Integer.parseInt(string);
				return value;
			}
			catch (NumberFormatException e)
			{
				return 0;
			}
		}
	}

	public Record createRecord(String typeName) throws InvalidDataException
	{
		return Record.CreateRecord(db, this, typeName);
	}

	public Record getRecord(int recordId) throws SQLException, NotFoundException, InvalidDataException
	{
		Record record = Record.GetRecordById(db, recordId);
		if (record.getDomainId() != id) throw new NotFoundException("No record by that id in this domain");
		return record;
	}

	public String getDisplayName()
	{
		return isReverse() ? getReverseName() : getName();
	}

	public static List<Domain> GetDomains(Database db) throws SQLException
	{
		List<Domain> domains = new LinkedList<Domain>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT domain.*, (SELECT COUNT(*) FROM domain_alias WHERE domain_alias.domain_id=domain.domain_id) AS alias_count FROM `domain` ORDER BY domain_name");
			while (rs.next())
			{
				domains.add(new Domain(db, rs));
			}
			Collections.sort(domains);
			return domains;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}

	}

	public static List<Domain> GetServerDomains(Database db, Server server) throws SQLException
	{
		List<Domain> domains = new LinkedList<Domain>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try
		{
			conn = db.getConnection();
			stmt = conn
					.prepareStatement("SELECT domain.* FROM `view` INNER JOIN `domain_view` ON domain_view.master_view_id=view.view_id INNER JOIN `domain` ON domain_view.domain_id=domain.domain_id WHERE view.server_id=? GROUP BY domain_view.domain_id ORDER BY `domain_name`");
			stmt.setInt(1, server.getId());
			rs = stmt.executeQuery();
			while (rs.next())
			{
				Domain domain = new Domain(db, rs);
				domain.setServer(server);
				domains.add(domain);
			}
			Collections.sort(domains);

			return domains;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}

	}

	public static List<Domain> GetViewDomains(Database db, View view) throws SQLException
	{
		List<Domain> domains = new LinkedList<Domain>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try
		{
			conn = db.getConnection();
			stmt = conn
					.prepareStatement("SELECT domain.* FROM domain_view INNER JOIN domain ON domain_view.domain_id=domain.domain_id WHERE domain_view.master_view_id=? OR domain_view.slave_view_id=? GROUP BY domain_view.domain_id ORDER BY domain_name");
			stmt.setInt(1, view.getId());
			stmt.setInt(2, view.getId());
			rs = stmt.executeQuery();
			while (rs.next())
			{
				Domain domain = new Domain(db, rs);
				domains.add(domain);
			}
			Collections.sort(domains);

			return domains;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}

	}

	public Master getMasterById(int id) throws SQLException, NotFoundException
	{
		for (Master master : getMasters())
		{
			if (master.getId() == id) return master;
		}
		throw new NotFoundException("Domain " + getName() + " has no master " + id);
	}

	public Set<Master> getMasters() throws SQLException
	{
		if (masters != null) return masters;
		else
		{
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try
			{
				HashSet<Master> masters = new HashSet<Master>();
				Map<Integer, Master> masterCache = new HashMap<Integer, Master>();
				conn = db.getConnection();
				stmt = conn
						.prepareStatement("SELECT `master_view_id`, `slave_view_id` FROM `domain_view` WHERE `domain_id`=? ORDER BY `master_view_id`");
				stmt.setLong(1, id);
				rs = stmt.executeQuery();
				while (rs.next())
				{
					int master_view_id = (int) Util.Long(rs.getLong("master_view_id"));
					int slave_view_id = (int) Util.Long(rs.getLong("slave_view_id"));
					Master masterView = null;
					if (masterCache.containsKey(master_view_id)) masterView = masterCache.get(master_view_id);
					else
					{
						masterView = Master.GetMasterById(db, master_view_id);
						masterCache.put(master_view_id, masterView);
					}

					if (slave_view_id != 0)
					{
						masterView.addSlave(View.GetViewById(db, slave_view_id));
					}

					masters.add(masterView);
				}
				this.masters = masters;
			}
			catch (NotFoundException e)
			{
				e.printStackTrace();
			}
			finally
			{
				db.closeAll(conn, stmt, rs);
			}
			return masters;
		}

	}

	public void addMaster(Master masterView) throws SQLException
	{
		getMasters().add(masterView);
	}

	public void removeMaster(Master masterView) throws SQLException
	{
		getMasters().remove(masterView);
	}

	public Set<String> getAliases() throws SQLException
	{
		if (aliases != null) return aliases;
		else
		{
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try
			{
				LinkedHashSet<String> aliases = new LinkedHashSet<String>();
				conn = db.getConnection();
				stmt = conn.prepareStatement("SELECT `domain_name` FROM `domain_alias` WHERE `domain_id`=? ORDER BY `domain_name`");
				stmt.setInt(1, id);
				rs = stmt.executeQuery();
				while (rs.next())
				{
					aliases.add(rs.getString("domain_name"));
				}
				this.aliases = aliases;
			}
			finally
			{
				db.closeAll(conn, stmt, rs);
			}

			return aliases;
		}
	}

	public void addAlias(String alias) throws SQLException
	{
		if (alias != null && !alias.trim().isEmpty()) getAliases().add(alias.trim().toLowerCase());
	}

	public void removeAlias(String alias) throws SQLException
	{
		getAliases().remove(alias.trim().toLowerCase());
	}

	public void clearAliases()
	{
		aliases = new HashSet<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Domain [expire=" + expire + ", id=" + id + ", minimum=" + minimum + ", name=" + name + ", ownerId=" + ownerId + ", refresh="
				+ refresh + ", retry=" + retry + ", serial=" + serial + ", serverId=" + serverId + ", ttl=" + ttl + "]";
	}

	public String getSOA() throws SQLException, NotFoundException, InvalidDataException
	{
		StringWriter soa = new StringWriter();
		PrintWriter out = new PrintWriter(soa);
		out.println("$TTL " + getTtl() + "  ; " + GenerateHumanTime(ttl));
		String base = "@ IN SOA " + getServer().getHostname() + ". " + getOwner().getEmail().replace('@', '.') + ". (";
		out.printf("%s %-10s ; serial%n", base, getSerial());
		base = Util.repeat(' ', base.length());
		out.printf("%s %-10s ; refresh (%s)%n", base, getRefresh(), GenerateHumanTime(refresh));
		out.printf("%s %-10s ; retry (%s)%n", base, getRetry(), GenerateHumanTime(retry));
		out.printf("%s %-10s ; expire (%s)%n", base, getExpire(), GenerateHumanTime(expire));
		out.printf("%s %-10s ; minimum (%s)%n", base, getMinimum(), GenerateHumanTime(minimum));
		out.println(base + ")");

		return soa.toString();
	}

	public Date getCreated()
	{
		return created;
	}

	public Date getUpdated()
	{
		return updated;
	}

	public void copyRecords(Domain domain) throws SQLException
	{
		if (id == 0 || domain.getId() == 0) return;

		Connection conn = null;
		PreparedStatement stmt = null;
		try
		{
			if (id != 0)
			{
				conn = db.getConnection();
				stmt = conn
						.prepareStatement("INSERT INTO `record` (`record_id`, `domain_id`, `group_id`, `ttl`, `type`, `name`, `data`, `priority`, `active`, `comment`, `created`, `updated`) SELECT NULL AS record_id, ? AS domain_id, group_id, ttl, type, name, data, priority, active, comment, NOW() AS created, NOW() AS updated FROM `record` WHERE domain_id=?");
				stmt.setInt(1, id);
				stmt.setInt(2, domain.getId());
				stmt.executeUpdate();
			}
		}
		finally
		{
			db.closeAll(conn, stmt, null);
		}

	}

	public int getAliasCount()
	{
		return aliasCount;
	}

	public boolean isReverse()
	{
		return name.endsWith(".in-addr.arpa") || name.endsWith(".ip6.arpa");
	}

	public String getReverseName()
	{
		StringBuilder str = new StringBuilder();
		String[] parts = name.split("\\.");
		for (int i = 0; i < parts.length - 2; i++)
		{
			str.insert(0, parts[i] + (i > 0 ? "." : ""));
		}
		return str.toString();
	}

	public static String GenerateArpa(String ipAddress)
	{
		StringBuilder str = new StringBuilder();
		String[] parts = ipAddress.split("\\.");
		for (int i = 0; i < parts.length; i++)
		{
			str.insert(0, parts[i] + (i > 0 ? "." : ""));
		}
		return str.toString() + ".in-addr.arpa";

	}

	@Override
	public int compareTo(Domain arg0)
	{
		return getDisplayName().compareToIgnoreCase(arg0.getDisplayName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return getDisplayName().hashCode();
	}

	/**
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the soaRecord
	 */
	public SOARecord getSoaRecord()
	{
		return soaRecord;
	}

	public void setSoaRecord(SOARecord soaRecord)
	{
		this.soaRecord = soaRecord;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		// TODO Auto-generated method stub
		return super.clone();
	}

	/**
	 * @param db2
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	public static List<Domain> GetUserDomains(Database db, User user) throws SQLException
	{
		List<Domain> domains = new LinkedList<Domain>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try
		{
			conn = db.getConnection();
			stmt = conn
					.prepareStatement("SELECT domain.* FROM domain_admin JOIN domain ON domain_admin.domain_id=domain.domain_id WHERE user_id=? ORDER BY domain_name");
			stmt.setInt(1, user.getId());
			rs = stmt.executeQuery();
			while (rs.next())
			{
				Domain domain = new Domain(db, rs);
				domains.add(domain);
			}
			return domains;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected()
	{
		return selected;
	}

	/**
	 * @param selected
	 *            the selected to set
	 */
	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public Domain getParentDomain()
	{
		return parentDomain;
	}

	public void setParentDomain(Domain parentDomain)
	{
		this.parentDomain = parentDomain;
	}
	
	public String getDomainName()
	{
		if(parentDomain!=null) return parentDomain.getName();
		else return getName();
	}

}
