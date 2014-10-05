/* TXTRecord.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import com.rambris.Database;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: TXTRecord.java 46 2010-09-01 18:34:45Z boost $
 */
public class TXTRecord extends Record
{

	/**
	 * @param type
	 * @param db
	 */
	public TXTRecord(Database db, Domain domain)
	{
		super(Type.TXT, db, domain);
	}

	@Override
	public void setData(String data) throws InvalidDataException
	{
		if (data != null)
		{
			data = data.trim();
			if (data.startsWith("\"")) data = data.substring(1);
			if (data.endsWith("\"")) data = data.substring(0, data.length() - 1);
		}
		super.setData(data);
	}

	@Override
	public String toString(int size)
	{
		return String.format("%-" + size + "s %-5s IN %-5s \"%s\"", name, getTtl(), type, data);
	}
}
