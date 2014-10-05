package com.rambris.dnsmanager;

/**
 * Simple bean to hold SOA data
 * 
 * @author boost
 */
public class SOARecord
{
	private String host, admin;
	private int serial, refresh, retry, expire, minimum, ttl;

	public SOARecord(String host, String admin, int serial, int refresh, int retry, int expire, int minimum, int ttl)
	{
		this.host = host;
		this.admin = admin;
		this.serial = serial;
		this.refresh = refresh;
		this.retry = retry;
		this.expire = expire;
		this.minimum = minimum;
		this.setTtl(ttl);
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public String getAdmin()
	{
		return admin;
	}

	public void setAdmin(String admin)
	{
		this.admin = admin;
	}

	public int getSerial()
	{
		return serial;
	}

	public void setSerial(int serial)
	{
		this.serial = serial;
	}

	public String getRefresh()
	{
		return Domain.GenerateTime(refresh);
	}

	public void setRefresh(int refresh)
	{
		this.refresh = refresh;
	}

	public String getRetry()
	{
		return Domain.GenerateTime(retry);
	}

	public void setRetry(int retry)
	{
		this.retry = retry;
	}

	public String getExpire()
	{
		return Domain.GenerateTime(expire);
	}

	public void setExpire(int expire)
	{
		this.expire = expire;
	}

	public String getMinimum()
	{
		return Domain.GenerateTime(minimum);
	}

	public void setMinimum(int minimum)
	{
		this.minimum = minimum;
	}

	/**
	 * @param ttl
	 *            the ttl to set
	 */
	public void setTtl(int ttl)
	{
		this.ttl = ttl;
	}

	/**
	 * @return the ttl
	 */
	public String getTtl()
	{
		return Domain.GenerateTime(ttl);
	}

}
