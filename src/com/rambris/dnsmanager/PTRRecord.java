/* PTRRecord.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rambris.Database;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: PTRRecord.java 44 2010-08-31 19:10:03Z boost $
 */
public class PTRRecord extends Record
{
	private Record reverse = null;

	/**
	 * @param type
	 * @param db
	 * @param domain
	 * @throws InvalidDataException
	 */
	public PTRRecord(Database db, Domain domain) throws InvalidDataException
	{
		super(Type.PTR, db, domain);
		if (!domain.isReverse() && !domain.getName().startsWith(".")) throw new InvalidDataException("PTR records may only exist in in-addr.arpa domains");
	}

	public String getIpAddress()
	{
		if (name == null) return null;

		StringBuilder str = new StringBuilder();
		String[] parts = name.split("\\.");
		for (int i = 0; i < parts.length; i++)
		{
			str.insert(0, parts[i] + (i > 0 ? "." : ""));
		}
		return domain.getReverseName() + "." + str.toString();

	}

	public Record getReverse() throws SQLException, NotFoundException, InvalidDataException
	{
		if (name == null) return null;
		if (reverse != null) return reverse;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT record.* FROM `record` JOIN `domain` ON `record`.`domain_id` = `domain`.`domain_id` WHERE `type`='A' AND CONCAT(name,'.',domain_name,'.')=? AND `data`=?;");
			stmt.setString(1, data);
			stmt.setString(2, getIpAddress());
			rs = stmt.executeQuery();
			if (rs.next()) return reverse = CreateRecord(db, rs);
			return null;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	@Override
	public void setData(String data) throws InvalidDataException
	{
		if (!DNS.validHostname(data)) throw new InvalidDataException("Invalid name");
		super.setData(data);
	}

}
