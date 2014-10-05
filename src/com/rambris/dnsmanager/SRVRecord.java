/* SRVRecord.java (c) 2012 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rambris.Database;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 */
public class SRVRecord extends Record
{
	private static Pattern namePattern = Pattern.compile("^_(.+)\\._([^.]+)(\\..*)*$");
	private final static Set<String> protocols = new LinkedHashSet<String>();
	static
	{
		protocols.add("tcp");
		protocols.add("udp");
		protocols.add("tls");
	}

	private String service;
	private String proto;
	private int weight;
	private int port;
	String target;

	/**
	 * @param type
	 * @param db
	 */
	public SRVRecord(Database db, Domain domain)
	{
		super(Type.SRV, db, domain);
	}

	public static Set<String> GetProtocols()
	{
		return protocols;
	}

	@Override
	public void save() throws SQLException, InvalidDataException
	{
		if (service != null && !service.isEmpty() && proto != null && !proto.isEmpty()) super.save();
		else throw new InvalidDataException("Service and protocol must be set");
	}

	public String getService()
	{
		return service;
	}

	public void setService(String service)
	{
		this.service = service;
	}

	public void setProtocol(String proto) throws InvalidDataException
	{
		if (protocols.contains(proto.toLowerCase())) this.proto = proto.toLowerCase();
		else throw new InvalidDataException("Only TCP and UDP allowed");
	}

	public String getProtocol()
	{
		return proto;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public int getWeight()
	{
		return weight;
	}

	public void setWeight(int weight)
	{
		this.weight = weight;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	@Override
	public String parseName(String name) throws InvalidDataException
	{
		Matcher nameMatcher = namePattern.matcher(name);
		if (nameMatcher.find())
		{
			setService(nameMatcher.group(1));
			setProtocol(nameMatcher.group(2));
			if (nameMatcher.group(3) != null && nameMatcher.group(3).length() > 1) return nameMatcher.group(3).substring(1);
			return "";
		}
		else throw new InvalidDataException("Name of record does not follow RFC 2782");
	}

	@Override
	public String getBuiltName()
	{
		return "_" + getService() + "._" + getProtocol() + ("@".equals(getName()) || getName() == null ? "" : ("." + getName()));
	}

	@Override
	public void setData(String data)
	{
		String[] parts = data.trim().split("\\s+");
		if (parts.length == 3)
		{
			setWeight(Integer.parseInt(parts[0]));
			setPort(Integer.parseInt(parts[1]));
			setTarget(parts[2]);
		}
	}

	@Override
	public String getData()
	{
		data = String.format(Locale.US, "%d %d %s", weight, port, target);
		return data;
	}

	@Override
	public String toString(int size)
	{
		return String.format("%-" + size + "s %-5s IN %-5s %s %s", getBuiltName(), getTtl(), type, getPriority(), getData());
	}
}
