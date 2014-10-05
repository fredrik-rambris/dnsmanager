/* IndexPage.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rambris.dnsmanager.DNS;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: OverviewPage.java 48 2010-09-02 18:54:42Z boost $
 */
public class OverviewPage extends RestrictedPage
{

	/**
	 * @param servlet
	 * @param request
	 * @param response
	 * @param dns
	 * @throws SQLException
	 * @throws RedirectedException
	 * @throws IOException
	 */
	public OverviewPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException,
			RedirectedException, SQLException
	{
		super(servlet, request, response, dns);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rambris.dnsmanager.web.Page#run(java.lang.String)
	 */
	@Override
	public void run(String path) throws Exception
	{
		if (getParameter("generateZones") != null)
		{
			dns.addGenerateZoneFilesTask();
			redirect(SELF);
			return;
		}
		if (getParameter("generateConfigs") != null)
		{
			dns.addGenerateConfigsTask();
			redirect(SELF);
			return;

		}
		if (getParameter("clearExported") != null)
		{
			dns.clearExported();
			redirect(SELF);
			return;
		}
		setAttribute("stats", getStats());
		setAttribute("tasks", dns.getTasks(false));
		sendDispatch("indexPage.jsp");
	}

	private Map<String, Integer> getStats() throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Map<String, Integer> stats = new LinkedHashMap<String, Integer>();
		try
		{
			conn = db.getConnection();
			stmt = conn.createStatement();
			for (String t : new String[] { "server", "view", "group", "domain", "domain_alias", "record", "owner" })
			{
				rs = stmt.executeQuery("SELECT COUNT(*) FROM `" + t + "`");
				if (rs.next())
				{
					stats.put(t + "s", rs.getInt(1));
				}
				db.closeResultSet(rs);
				rs = null;
			}
			return stats;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}
}
