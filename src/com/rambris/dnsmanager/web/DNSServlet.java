package com.rambris.dnsmanager.web;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.log4j.Logger;

import com.rambris.dnsmanager.DNS;
import com.rambris.dnsmanager.NotFoundException;

/**
 * Servlet implementation class Servlet
 */
public class DNSServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private DNS dns;
	private HttpServletRequest request;
	private HttpServletResponse response;
	public String localURI;
	private Logger log;

	/**
	 * @see DNSServlet#init(ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		File configfile = new File(config.getInitParameter("configfile"));
		log = Logger.getLogger(DNSServlet.class);
		try
		{
			dns = new DNS(configfile);
		}
		catch (Exception e)
		{
			throw new ServletException(e.getMessage(), e);
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{

		this.request = request;
		this.response = response;

		request.setCharacterEncoding("UTF-8");
		localURI = request.getRequestURI().substring(request.getContextPath().length());
		String[] parts = localURI.split("/", 3);
		if (parts.length > 0)
		{
			if (parts[0] == null) parts = Arrays.copyOfRange(parts, 1, parts.length);
			else if (parts[0].isEmpty()) parts = Arrays.copyOfRange(parts, 1, parts.length);
		}
		if (parts.length == 0)
		{
			parts = new String[] { "index" };
		}
		if (parts[0].endsWith(".do")) parts[0] = parts[0].substring(0, parts[0].length() - 3);
		invoke(parts);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}

	public DNS getDNS()
	{
		return dns;
	}

	private void invoke(String[] parts) throws IOException
	{
		Page page = null;
		if (parts[0].isEmpty()) parts[0] = "Index";
		else if ("favicon.ico".equals(parts[0])) return;
		else
		{
			parts[0] = cleanName(parts[0]);
			parts[0] = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1).toLowerCase();
		}
		try
		{
			String args = null;
			if (parts.length > 1)
			{
				args = parts[1];
			}
			String className = getClass().getPackage().getName() + "." + parts[0] + "Page";
			Class pageClass = Class.forName(className);

			try
			{
				page = (Page) pageClass.getConstructor(DNSServlet.class, HttpServletRequest.class, HttpServletResponse.class, DNS.class).newInstance(
						this, request, response, dns);
				page.run(args);
			}
			catch (InvocationTargetException e)
			{
				throw e.getTargetException();
			}
		}
		catch (NoSuchMethodException e)
		{
			log.error(e.getMessage() + " method not found");
			response.sendError(404, parts[0] + " not found");
		}
		catch (ClassNotFoundException e)
		{
			log.error(e.getMessage() + " not found");
			response.sendError(404, parts[0] + " not found");
		}
		catch (ClientAbortException e)
		{
			log.warn(e.getMessage());
		}
		catch (NotFoundException e)
		{
			log.error(e.getMessage());
			response.sendError(404, e.getMessage());
		}
		catch (RedirectedException e)
		{

		}
		catch (PermissionDeniedException e)
		{
			response.sendError(response.SC_UNAUTHORIZED, e.getMessage());
		}
		catch (Throwable e)
		{
			if (e.getCause() != null) log.error(e.getCause().getMessage(), e.getCause());
			log.error(e.getMessage(), e);
			response.sendError(500, e.getMessage());

		}
		finally
		{
			if (page != null) page.removeTemporaryAttributes();
		}
	}

	public static String cleanName(String name)
	{
		return name.replaceAll("[^a-zA-Z_]", "");
	}

}
