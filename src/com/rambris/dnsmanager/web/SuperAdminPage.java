/* SuperAdminPage.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rambris.dnsmanager.DNS;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 * 
 */
public abstract class SuperAdminPage extends RestrictedPage
{

	/**
	 * @param servlet
	 * @param request
	 * @param response
	 * @param dns
	 * @throws IOException
	 * @throws RedirectedException
	 * @throws SQLException
	 * @throws PermissionDeniedException
	 */
	public SuperAdminPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException,
			RedirectedException, SQLException, PermissionDeniedException
	{
		super(servlet, request, response, dns);
		if (!currentUser.isSuperAdmin()) throw new PermissionDeniedException("You do not have permission to access this page");
	}
}
