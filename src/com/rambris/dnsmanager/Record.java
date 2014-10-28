/* Record.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
 * @version $Id: Record.java 46 2010-09-01 18:34:45Z boost $
 */
public abstract class Record
{
	// Used for parser
	private static Pattern recordPattern = Pattern.compile("(.*)IN\\s+(\\S+)\\s+(.*)");
	private static Pattern namePattern = Pattern.compile("^(.*)\\s+(\\d+)$");
	private static Pattern dataPattern = Pattern.compile("^(\\d+)\\s+(.*)$");

	private Logger log=Logger.getLogger(Record.class);
	protected Database db;
	protected int id;
	protected int domainId;
	protected Domain domain = null;
	protected int groupId;
	protected Group group = null;
	protected Type type;
	protected boolean active = true;
	protected String name;
	protected String data;
	protected int priority;
	protected int ttl = 0;
	protected String comment = null;
	private Date created = null;
	private Date updated = null;

	protected enum Type
	{
		NS, MX, TXT, A, CNAME, LOC, PTR, SRV
	}

	private static Map<String, Type> types = new LinkedHashMap<String, Type>();
	static
	{
		types.put("A", Type.A);
		types.put("CNAME", Type.CNAME);
		types.put("MX", Type.MX);
		types.put("NS", Type.NS);
		types.put("TXT", Type.TXT);
		types.put("LOC", Type.LOC);
		types.put("PTR", Type.PTR);
		types.put("SRV", Type.SRV);
	}

	protected Record(Type type, Database db, Domain domain)
	{
		this.type = type;
		this.db = db;
		this.domain = domain;
		this.domainId = domain.getId();
	}

	public static Map<String, Type> getTypes()
	{
		return types;
	}

	public static Set<String> getTypeNames()
	{
		return getTypes().keySet();
	}

	protected void loadResultSet(ResultSet rs) throws SQLException, InvalidDataException
	{
		id = rs.getInt("record_id");
		domainId = rs.getInt("domain_id");
		groupId = rs.getInt("group_id");
		setTtl(rs.getInt("ttl"));
		setType(rs.getString("type"));
		setName(parseName(rs.getString("name")));
		setData(rs.getString("data"));
		priority = rs.getInt("priority");
		active = rs.getBoolean("active");
		comment = rs.getString("comment");
		created = Database.TimestampToDate(rs.getTimestamp("created"));
		updated = Database.TimestampToDate(rs.getTimestamp("updated"));

	}

	public static Record GetRecordById(Database db, int id) throws SQLException, NotFoundException, InvalidDataException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM `record` WHERE record_id=?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) { return CreateRecord(db, rs); }
			throw new NotFoundException("No record by id " + id);
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	protected static Record CreateRecord(Database db, ResultSet rs) throws SQLException, NotFoundException, InvalidDataException
	{
		Domain domain = Domain.GetDomainById(db, rs.getInt("domain_id"));
		return CreateRecord(db, domain, rs);
	}

	protected static Record CreateRecord(Database db, Domain domain, ResultSet rs) throws SQLException, NotFoundException, InvalidDataException
	{
		String typeName = rs.getString("type");
		Record record = CreateRecord(db, domain, typeName);
		if (record != null)
		{
			record.loadResultSet(rs);
		}
		return record;
	}

	public static Record CreateRecord(Database db, Domain domain, String typeName) throws InvalidDataException
	{
		Record record = null;
		if (typeName == null) return record;
		else if ("A".equalsIgnoreCase(typeName)) return new ARecord(db, domain);
		else if ("CNAME".equalsIgnoreCase(typeName)) return new CNAMERecord(db, domain);
		else if ("NS".equalsIgnoreCase(typeName)) return new NSRecord(db, domain);
		else if ("MX".equalsIgnoreCase(typeName)) return new MXRecord(db, domain);
		else if ("TXT".equalsIgnoreCase(typeName)) return new TXTRecord(db, domain);
		else if ("LOC".equalsIgnoreCase(typeName)) return new LOCRecord(db, domain);
		else if ("PTR".equalsIgnoreCase(typeName)) return new PTRRecord(db, domain);
		else if ("SRV".equalsIgnoreCase(typeName)) return new SRVRecord(db, domain);
		else throw new InvalidDataException("Uknown type: " + typeName);
	}

	/**
	 * Tries to parse a record from zone file
	 * 
	 * @param recordLine
	 *            Text from zone file
	 * @return a new Record
	 * @throws InvalidDataException
	 */
	public static Record ParseRecord(Database db, Domain domain, String recordLine) throws InvalidDataException
	{
		recordLine = recordLine.trim();
		Matcher recordMatcher = recordPattern.matcher(recordLine);
		if (recordMatcher.find())
		{
			String name = recordMatcher.group(1).trim();
			String type = recordMatcher.group(2).trim();
			String data = recordMatcher.group(3).trim();
			String ttl = "";
			Matcher nameMatcher = namePattern.matcher(name);
			if (nameMatcher.find())
			{
				name = nameMatcher.group(1).trim();
				ttl = nameMatcher.group(2).trim();
			}
			else if ("PTR".equalsIgnoreCase(type))
			{
				ttl = "";
			}
			else if (Util.Long(name) > 0)
			{
				ttl = name.trim();
				name = "";
			}
			Record record = CreateRecord(db, domain, type);
			if (record == null) return null;
			record.setName(record.parseName(name));
			record.setTtl(ttl);
			if ("MX".equalsIgnoreCase(type) || "SRV".equalsIgnoreCase(type))
			{
				Matcher dataMatcher = dataPattern.matcher(data);
				if (dataMatcher.find())
				{
					if (record instanceof MXRecord)
					{
						((MXRecord) record).setPriority(Integer.parseInt(dataMatcher.group(1)));
					}
					else if (record instanceof SRVRecord)
					{
						((SRVRecord) record).setPriority(Integer.parseInt(dataMatcher.group(1)));
					}
					data = dataMatcher.group(2).trim();
				}
			}
			record.setData(data);
			return record;
		}
		return null;
	}

	public static Map<String, List<Record>> GetDomainRecords(Database db, Domain domain) throws SQLException, NotFoundException, InvalidDataException
	{
		Map<String, List<Record>> records = new LinkedHashMap<String, List<Record>>();
		Map<String, Integer> groups = new HashMap<String, Integer>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			List<Record> currentList;
			records.put("common", currentList = new LinkedList<Record>());
			for (Group group : Group.GetGroups(db))
			{
				records.put(group.getName(), new LinkedList<Record>());
				groups.put(group.getName(), group.getId());
			}

			conn = db.getConnection();
			/* Get the common records */
			stmt = conn.prepareStatement("SELECT * FROM record WHERE domain_id=? AND group_id IS NULL ORDER BY IF(`name`='@', 0, 1), `name`, `type`");
			stmt.setInt(1, domain.getId());
			rs = stmt.executeQuery();
			while (rs.next())
			{
				currentList.add(CreateRecord(db, domain, rs));
			}
			db.closeResultSet(rs);
			rs = null;
			db.closeStatement(stmt);
			stmt = null;

			/* If we have any groups defined get records for each */
			if (records.size() > 1)
			{
				stmt = conn.prepareStatement("SELECT * FROM record WHERE domain_id=? AND group_id=? ORDER BY IF(`name`='@', 0, 1), `name`, `type`");
				for (String groupName : records.keySet())
				{
					if (groupName.equals("common")) continue;
					currentList = records.get(groupName);
					int groupId = groups.get(groupName);
					stmt.setInt(1, domain.getId());
					stmt.setInt(2, groupId);
					rs = stmt.executeQuery();
					while (rs.next())
					{
						currentList.add(CreateRecord(db, domain, rs));
					}
				}
			}
		}
		finally
		{
			db.closeResultSet(rs);
			db.closeStatement(stmt);
			db.closeConnection(conn);
		}

		return records;
	}

	public void delete() throws SQLException
	{
		if (id == 0) return;
		Connection conn = null;
		PreparedStatement stmt = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("DELETE FROM `record` WHERE record_id=?");
			stmt.setInt(1, id);
			stmt.executeUpdate();
			domain.touch();
		}
		finally
		{
			db.closeAll(conn, stmt, null);
		}
	}

	public void save() throws SQLException, InvalidDataException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			if (id == 0)
			{
				stmt = conn.prepareStatement("INSERT INTO `record` (`created`, `updated`, `domain_id`, `group_id`, `ttl`, `type`, `name`, `data`, `priority`, `active`, `comment`) VALUES (NOW(), NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				stmt.setInt(1, domainId);
				if (groupId != 0) stmt.setInt(2, groupId);
				else stmt.setNull(2, Types.INTEGER);
				stmt.setInt(3, ttl);
				stmt.setString(4, type.toString());
				stmt.setString(5, getBuiltName());
				stmt.setString(6, getData());
				stmt.setInt(7, priority);
				stmt.setBoolean(8, active);
				stmt.setString(9, comment);
				stmt.executeUpdate();
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				if (rs.next())
				{
					id = rs.getInt(1);
				}
			}
			else
			{
				stmt = conn.prepareStatement("UPDATE `record` SET `domain_id`=?, `group_id`=?, `ttl`=?, `type`=?, `name`=?, `data`=?, `priority`=?, `active`=?, `comment`=? WHERE `record_id`=?");
				stmt.setInt(1, domainId);
				if (groupId != 0) stmt.setInt(2, groupId);
				else stmt.setNull(2, Types.INTEGER);
				stmt.setInt(3, ttl);
				stmt.setString(4, type.toString());
				stmt.setString(5, getBuiltName());
				stmt.setString(6, getData());
				stmt.setInt(7, priority);
				stmt.setBoolean(8, active);
				stmt.setString(9, comment);
				stmt.setInt(10, id);
				stmt.executeUpdate();
			}
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	/**
	 * @return the group
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	public Group getGroup() throws NotFoundException, SQLException
	{
		if (groupId == 0) return null;
		else if (group != null) return group;
		else return group = Group.GetGroupById(db, groupId);
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setGroup(Group group)
	{
		this.group = group;
		if (group != null) groupId = group.getId();
		else groupId = 0;
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
	 * @throws InvalidDataException
	 */
	public void setName(String name) throws InvalidDataException
	{
		if (name != null && name.isEmpty()) name = "@";
		if (type != Type.SRV && !"@".equals(name) && !DNS.validHostname(name)) throw new InvalidDataException("Invalid name:" + name);
		this.name = name;
	}

	/**
	 * Parses name from database. Used in SRV-records where service and protocol
	 * is parsed out of name
	 * 
	 * @param name
	 * @return What is left after parsing.
	 * @throws InvalidDataException
	 */
	public String parseName(String name) throws InvalidDataException
	{
		return name;
	}

	/**
	 * Builds name to database. Used in SRV-records where name =
	 * _service._protocol.name.
	 * 
	 * @return Built name
	 */
	public String getBuiltName()
	{
		return name;
	}

	/**
	 * @return the data
	 */
	public String getData()
	{
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 * @throws InvalidDataException
	 */
	public void setData(String data) throws InvalidDataException
	{
		this.data = data;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return the domainId
	 */
	public int getDomainId()
	{
		return domainId;
	}

	/**
	 * @return the domain
	 */
	public Domain getDomain()
	{
		return domain;
	}

	/**
	 * @return the groupId
	 */
	public int getGroupId()
	{
		return groupId;
	}

	/**
	 * @return the type
	 */
	public Type getType()
	{
		return type;
	}

	private void setType(String typeName)
	{
		type = types.get(typeName.toUpperCase());
	}

	/**
	 * @return the ttl
	 */
	public String getTtl()
	{
		if (ttl == 0) return "";
		else return Domain.GenerateTime(ttl);
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
		if (ttl.isEmpty()) this.ttl = 0;
		else this.ttl = Domain.ParseTime(ttl);
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

	public String toString(int size)
	{
		return String.format((active ? "" : "; ") + "%-" + size + "s %-5s IN %-5s %s", getBuiltName(), getTtl(), type, getData());

	}

	@Override
	public String toString()
	{
		return toString(30);
	}

	public Date getCreated()
	{
		return created;
	}

	public Date getUpdated()
	{
		return updated;
	}

	/**
	 * Get Fully Qualified Domain Name
	 * 
	 * @return
	 */
	public String getFQDN()
	{
		return getBuiltName() + "." + getDomain().getName();
	}

	public String getShortData()
	{
		if (this.getData().length() < 50) return this.getData();
		else return this.getData().substring(0, 47) + "...";
	}

	public void clear()
	{
		id = 0;
		name = "";
	}
}
