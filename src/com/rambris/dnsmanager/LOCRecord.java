/* LOCRecord.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.util.Locale;

import com.rambris.Database;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: LOCRecord.java 44 2010-08-31 19:10:03Z boost $
 * 
 */
public class LOCRecord extends Record
{
	private short d1, d2, m1, m2;
	private float s1, s2, alt, siz = 0, hp = 0, vp = 0;
	private boolean cardinal1, cardinal2;

	/**
	 * @param type
	 * @param db
	 */
	public LOCRecord(Database db, Domain domain)
	{
		super(Type.LOC, db, domain);
	}

	public void setLatitude(short d, short m, float s, char cardinal)
	{
		d1 = d;
		m1 = m;
		s1 = s;
		cardinal1 = cardinal == 'N';
	}

	public void setLongitude(short d, short m, float s, char cardinal)
	{
		d2 = d;
		m2 = m;
		s2 = s;
		cardinal2 = cardinal == 'W';
	}

	public void setAltitude(float alt)
	{
		this.alt = alt;
	}

	public void setSize(float siz)
	{
		this.siz = siz;
	}

	public void setPrecision(float hp, float vp)
	{
		this.hp = hp;
		this.vp = vp;
	}

	@Override
	public void setData(String data)
	{
		String[] parts = data.replace('m', ' ').replace(',', '.').trim().split("\\s+");
		if (parts.length > 8)
		{
			setLatitude(Short.parseShort(parts[0]), Short.parseShort(parts[1]), Float.parseFloat(parts[2]), parts[3].charAt(0));
			setLongitude(Short.parseShort(parts[4]), Short.parseShort(parts[5]), Float.parseFloat(parts[6]), parts[7].charAt(0));
			setAltitude(Float.parseFloat(parts[8]));
		}
		if (parts.length > 9) setSize(Float.parseFloat(parts[9]));
		if (parts.length > 11) setPrecision(Float.parseFloat(parts[10]), Float.parseFloat(parts[11]));
	}

	@Override
	public String getData()
	{
		data = String.format(Locale.US, "%d %d %.3f %s %d %d %.3f %s %.2fm", d1, m1, s1, cardinal1 ? "N" : "S", d2, m2, s2, cardinal2 ? "W" : "E",
				alt);
		if (siz != 0.0)
		{
			data += String.format(Locale.US, " %.2fm", siz);
			if (hp != 0.0 || vp != 0.0)
			{
				data += String.format(Locale.US, " %.2fm %.2fm", hp, vp);
			}
		}
		return data;
	}

	/**
	 * @return the d1
	 */
	public short getLatD()
	{
		return d1;
	}

	/**
	 * @return the d2
	 */
	public short getLonD()
	{
		return d2;
	}

	/**
	 * @return the m1
	 */
	public short getLatM()
	{
		return m1;
	}

	/**
	 * @return the m2
	 */
	public short getLonM()
	{
		return m2;
	}

	/**
	 * @return the s1
	 */
	public float getLatS()
	{
		return s1;
	}

	/**
	 * @return the s2
	 */
	public float getLonS()
	{
		return s2;
	}

	/**
	 * @return the alt
	 */
	public float getAlt()
	{
		return alt;
	}

	/**
	 * @return the siz
	 */
	public float getSiz()
	{
		return siz;
	}

	/**
	 * @return the hp
	 */
	public float getHp()
	{
		return hp;
	}

	/**
	 * @return the vp
	 */
	public float getVp()
	{
		return vp;
	}

	/**
	 * @return the cardinal1
	 */
	public char getLatCardinal()
	{
		return cardinal1 ? 'N' : 'S';
	}

	/**
	 * @return the cardinal2
	 */
	public char getLonCardinal()
	{
		return cardinal2 ? 'W' : 'E';
	}
}
