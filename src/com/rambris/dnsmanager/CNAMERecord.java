/* CNAMERecord.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import com.rambris.Database;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: CNAMERecord.java 41 2010-08-25 21:12:29Z boost $
 * 
 */
public class CNAMERecord extends Record
{

	/**
	 * @param type
	 * @param db
	 */
	public CNAMERecord(Database db, Domain domain)
	{
		super(Type.CNAME, db, domain);
	}

}
