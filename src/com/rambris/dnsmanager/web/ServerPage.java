/* ServerPage.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rambris.Util;
import com.rambris.dnsmanager.DNS;
import com.rambris.dnsmanager.Group;
import com.rambris.dnsmanager.InvalidHostnameException;
import com.rambris.dnsmanager.NotFoundException;
import com.rambris.dnsmanager.Server;
import com.rambris.dnsmanager.View;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: ServerPage.java 43 2010-08-29 17:00:03Z boost $
 */
public class ServerPage extends SuperAdminPage
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
	public ServerPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException, RedirectedException,
			SQLException, PermissionDeniedException
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
		if (path == null) path = "";
		String[] parts = path.split("/");
		String subAction = "edit", viewName = "", action = "edit", serverName = "";

		if (parts.length > 3) subAction = parts[3];
		if (parts.length > 2) viewName = parts[2];
		if (parts.length > 1) action = parts[1];
		if (parts.length > 0) serverName = parts[0];

		if (serverName.equals("new"))
		{
			Server server = new Server(db);
			editServer(server);
		}
		else if (!serverName.isEmpty())
		{
			boolean json = false;
			if (serverName.endsWith(".json"))
			{
				json = true;
				serverName = serverName.substring(0, serverName.lastIndexOf('.'));
				System.out.println(request.getParameterMap().toString());
			}
			Server server = Server.GetServerByHostname(db, serverName);
			if (json)
			{
				response.setContentType("application/json");
				PrintWriter out = response.getWriter();
				out.print(server);
				return;
			}
			if (action.equals("view"))
			{
				if (viewName.equals("new"))
				{
					View view = new View(db, server);
					editView(view);
				}
				else if (!viewName.isEmpty())
				{
					View view = server.getView(viewName);
					if (subAction.equals("delete"))
					{
						view.delete();
						redirect(MODULE + "/" + serverName);
						return;
					}
					else
					{
						editView(view);
					}
				}
			}
			else if (action.equals("delete"))
			{
				server.delete();
				redirect(MODULE);
				return;
			}
			else
			{
				editServer(server);
			}
		}
		else
		{
			listServers();
		}
	}

	/**
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void listServers() throws SQLException, ServletException, IOException
	{
		setAttribute("servers", Server.GetServers(db));
		sendDispatch("/listServers.jsp");

	}

	private void editServer(Server server) throws IOException, SQLException, ServletException, InvalidHostnameException
	{
		if (getParameter("cancel") != null)
		{
			redirect(MODULE);
			return;
		}
		if (request.getMethod().equalsIgnoreCase("POST"))
		{
			boolean create = server.getId() == 0;
			if (getParameter("hostname") != null) server.setHostname(getParameter("hostname"));
			if (getParameter("master_prefix") != null) server.setMasterPrefix(getParameter("master_prefix"));
			if (getParameter("slave_prefix") != null) server.setSlavePrefix(getParameter("slave_prefix"));
			if (getParameter("scp_address") != null) server.setScpAddress(getParameter("scp_address"));
			if (getParameter("zone_path") != null) server.setZonePath(getParameter("zone_path"));
			if (getParameter("config_path") != null) server.setConfigPath(getParameter("config_path"));
			if (getParameter("reload_command") != null) server.setReloadCommand(getParameter("reload_command"));
			server.save();
			if (create)
			{
				redirect(MODULE + "/" + server.getHostname());
			}
			else
			{
				redirect(MODULE);
			}
			return;
		}
		setAttribute("server", server);
		sendDispatch("/editServer.jsp");
	}

	private void editView(View view) throws IOException, SQLException, ServletException, NotFoundException
	{
		String returnTo = MODULE + "/" + view.getServer().getHostname();
		if (getParameter("cancel") != null)
		{
			redirect(returnTo);
			return;
		}
		if (request.getMethod().equalsIgnoreCase("POST"))
		{
			boolean create = view.getId() == 0;
			if (getParameter("view_name") != null) view.setName(getParameter("view_name"));
			if (getParameter("view_description") != null) view.setDescription(getParameter("view_description"));
			if (getParameter("view_address") != null) view.setAddress(getParameter("view_address"));
			if (getParameter("notify") != null) view.setNotify("1".equals(getParameter("notify")));
			if (getParameter("groups") != null)
			{
				for (String groupName : request.getParameterValues("groups"))
				{
					short priority = (short) Util.Long(getParameter("group_priorities[" + groupName + "]"));
					view.addGroup(Group.GetGroupByName(db, groupName), priority);
				}
			}
			view.save();
			if (create) redirect(returnTo + "/view/" + view.getName());
			else redirect(returnTo);
			return;

		}

		/* Get groups with view groups selected */
		HashMap<Integer, Group> groupsMap = new HashMap<Integer, Group>();
		for (Group group : Group.GetGroups(db))
			groupsMap.put(group.getId(), group);
		for (Group group : view.getGroups())
			groupsMap.put(group.getId(), group);
		LinkedList<Group> groups = new LinkedList<Group>();
		groups.addAll(groupsMap.values());
		Collections.sort(groups);
		setAttribute("groups", groups);

		setAttribute("server", view.getServer());
		setAttribute("view", view);
		sendDispatch("/editView.jsp");
	}
}