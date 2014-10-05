/* DomainView.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 * 
 */
public class DomainView
{
	private View master;
	private View slave;
	private Domain domain;

	public DomainView(Domain domain, View master)
	{
		this(domain, master, (View) null);
	}

	public DomainView(Domain domain, View master, View slave)
	{
		this.domain = domain;
		this.master = master;
		this.slave = slave;
	}

	/**
	 * @return the master
	 */
	public View getMaster()
	{
		return master;
	}

	/**
	 * @param master
	 *            the master to set
	 */
	public void setMaster(View master)
	{
		this.master = master;
	}

	/**
	 * @return the view
	 */
	public View getSlave()
	{
		return slave;
	}

	/**
	 * @param view
	 *            the view to set
	 */
	public void setSlave(View slave)
	{
		this.slave = slave;
	}

	/**
	 * @return the domain
	 */
	public Domain getDomain()
	{
		return domain;
	}

	/**
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(Domain domain)
	{
		this.domain = domain;
	}

	public boolean hasSlave()
	{
		return slave != null;
	}
}
