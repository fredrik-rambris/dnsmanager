/* GroupPage.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rambris.dnsmanager.DNS;
import com.rambris.dnsmanager.Group;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: GroupPage.java 43 2010-08-29 17:00:03Z boost $
 * 
 */
public class GroupPage extends SuperAdminPage
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
	public GroupPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException, RedirectedException,
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
		String groupName = "", action = "edit";
		if (parts.length > 1) action = parts[1];
		if (parts.length > 0) groupName = parts[0];
		if (groupName.isEmpty()) listGroups();
		else if (groupName.equals("new"))
		{
			Group group = new Group(db);
			editGroup(group);
		}
		else
		{
			Group group = Group.GetGroupByName(db, groupName);
			if (action.equals("delete"))
			{
				group.delete();
				redirect(MODULE);
				return;
			}
			else
			{
				editGroup(group);
			}
		}
	}

	/**
	 * @param group
	 * @throws IOException
	 * @throws ServletException
	 * @throws SQLException
	 */
	private void editGroup(Group group) throws ServletException, IOException, SQLException
	{
		if (getParameter("cancel") != null)
		{
			redirect(MODULE);
			return;
		}

		if (request.getMethod().equalsIgnoreCase("POST"))
		{
			if (getParameter("group_name") != null) group.setName(getParameter("group_name"));
			group.save();
			redirect(MODULE);
		}
		else
		{
			setAttribute("group", group);
			sendDispatch("/editGroup.jsp");
		}
	}

	/**
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 * 
	 */
	private void listGroups() throws SQLException, ServletException, IOException
	{
		setAttribute("groups", Group.GetGroups(db));
		sendDispatch("/listGroups.jsp");
	}

}
