/* ARecord.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.rambris.Database;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: ARecord.java 44 2010-08-31 19:10:03Z boost $
 */
public class ARecord extends Record
{

	private static final Logger log = Logger.getLogger(ARecord.class);

	/**
	 * @param type
	 * @param db
	 */
	public ARecord(Database db, Domain domain)
	{
		super(Type.A, db, domain);
		// TODO Auto-generated constructor stub
	}

	public final static boolean ValidIP4(String ipAddr)
	{
		if (ipAddr == null) return false;
		if (ipAddr.isEmpty()) return false;
		String[] parts = ipAddr.split("\\.");
		if (parts.length != 4) return false;
		for (String part : parts)
		{
			try
			{
				short octet = Short.parseShort(part);
				if (octet < 0 || octet > 255) return false;
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void setData(String data) throws InvalidDataException
	{
		if (!ValidIP4(data)) throw new InvalidDataException("Invalid IPv4 address");
		super.setData(data);
	}

	public Domain getReverseDomain() throws SQLException, NotFoundException
	{
		if (!ValidIP4(data)) return null;
		String network = data.substring(0, data.lastIndexOf('.'));
		log.debug("Network:" + network);

		return Domain.GetDomainByName(db, Domain.GenerateArpa(network));
	}

	public PTRRecord getReverse(boolean create) throws SQLException
	{
		if (data == null) return null;
		String network = data.substring(0, data.lastIndexOf('.'));
		String host = data.substring(data.lastIndexOf('.') + 1);
		try
		{
			Domain reverseDomain = getReverseDomain();
			if (reverseDomain == null) return null;
			Map<String, List<Record>> records = reverseDomain.getRecords();
			String groupName = "common";
			if (groupId != 0) groupName = getGroup().getName();
			if (records.containsKey(groupName))
			{
				for (Record record : records.get(groupName))
				{
					if (record.type == Type.PTR && record.name.equals(host) && record.data.equalsIgnoreCase(getFQDN() + ".")) return (PTRRecord) record;
				}
			}
			/* If we didn't find it in the same group as ourselves try in common */
			if (groupId != 0)
			{
				for (Record record : records.get("common"))
				{
					if (record.type == Type.PTR && record.name.equals(host) && record.data.equalsIgnoreCase(getFQDN() + ".")) return (PTRRecord) record;
				}
			}
			/* If not found and we want it to be created, do so here */
			if (create)
			{
				PTRRecord reverseRecord = new PTRRecord(db, reverseDomain);
				reverseRecord.name = host;
				reverseRecord.setGroup(getGroup());
				reverseRecord.data = getFQDN() + ".";
				reverseRecord.save();
				return reverseRecord;
			}
		}
		catch (NotFoundException e)
		{
			log.warn("Trying to get reverse record but reverse domain (" + Domain.GenerateArpa(network) + ") does not exist");
		}
		catch (InvalidDataException e)
		{
			log.warn(e.getMessage(), e);
		}
		return null;
	}
}
