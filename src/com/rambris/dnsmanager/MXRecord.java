/* MXRecord.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import com.rambris.Database;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: MXRecord.java 46 2010-09-01 18:34:45Z boost $
 * 
 */
public class MXRecord extends Record
{

	/**
	 * @param type
	 * @param db
	 */
	protected MXRecord(Database db, Domain domain)
	{
		super(Type.MX, db, domain);
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Override
	public String toString(int size)
	{
		return String.format("%-" + size + "s %-5s IN %-5s %s %s", name, getTtl(), type, priority, data);
	}

}
