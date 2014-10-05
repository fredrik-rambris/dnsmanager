/* OwnerPage.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rambris.dnsmanager.DNS;
import com.rambris.dnsmanager.Owner;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: OwnerPage.java 43 2010-08-29 17:00:03Z boost $
 * 
 */
public class OwnerPage extends SuperAdminPage
{

	/**
	 * @param servlet
	 * @param request
	 * @param response
	 * @param dns
	 * @throws SQLException
	 * @throws RedirectedException
	 * @throws IOException
	 * @throws PermissionDeniedException
	 */
	public OwnerPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException, RedirectedException,
			SQLException, PermissionDeniedException
	{
		super(servlet, request, response, dns);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rambris.dnsmanager.web.Page#run(java.lang.String)
	 */
	@Override
	public void run(String path) throws Exception
	{
		if (path == null) path = "";
		String[] parts = path.split("/");
		String ownerName = "", action = "edit";
		if (parts.length > 1) action = parts[1];
		if (parts.length > 0) ownerName = parts[0];
		if (ownerName.isEmpty()) listOwners();
		else if (ownerName.equals("new"))
		{
			Owner owner = new Owner(db);
			editOwner(owner);
		}
		else
		{
			Owner owner = Owner.GetOwnerByName(db, ownerName);
			if (action.equals("delete"))
			{
				owner.delete();
				redirect(MODULE);
				return;
			}
			else
			{
				editOwner(owner);
			}
		}
	}

	/**
	 * @param owner
	 * @throws IOException
	 * @throws ServletException
	 * @throws SQLException
	 */
	private void editOwner(Owner owner) throws ServletException, IOException, SQLException
	{
		if (getParameter("cancel") != null)
		{
			redirect(MODULE);
			return;
		}

		if (request.getMethod().equalsIgnoreCase("POST"))
		{
			if (getParameter("owner_name") != null) owner.setName(getParameter("owner_name"));
			if (getParameter("owner_email") != null) owner.setEmail(getParameter("owner_email"));
			owner.save();
			redirect(MODULE);
		}
		else
		{
			setAttribute("owner", owner);
			sendDispatch("/editOwner.jsp");
		}
	}

	/**
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 * 
	 */
	private void listOwners() throws SQLException, ServletException, IOException
	{
		setAttribute("owners", Owner.GetOwners(db));
		sendDispatch("/listOwners.jsp");
	}

}
