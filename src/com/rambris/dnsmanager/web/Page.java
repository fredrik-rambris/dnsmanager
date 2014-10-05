/* Page.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.rambris.Config;
import com.rambris.Database;
import com.rambris.EnumerationIterator;
import com.rambris.dnsmanager.DNS;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: Page.java 44 2010-08-31 19:10:03Z boost $
 */
public abstract class Page
{
	protected DNSServlet servlet;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	private final HashSet<String> temporaryAttributes = new HashSet<String>();
	protected DNS dns;
	protected Database db;
	protected Config config;
	protected String return_to = null;
	protected Map<String, String> cookies = null;

	private Logger log = null;

	/** ROOT is the root of application (/DNSManager) */
	protected String ROOT;
	/** SELF is request URI (/DNSManager/record/123) */
	protected String SELF;
	/** MODULE is BASE + name (/DNSManager/record) */
	protected String MODULE;

	public void setAttribute(String name, Object value)
	{
		setAttribute(name, value, false);
	}

	public void setAttribute(String name, Object value, boolean permanent)
	{
		if (!permanent) temporaryAttributes.add(name);
		request.getSession().setAttribute(name, value);
	}

	public Object getAttribute(String name)
	{
		return request.getSession().getAttribute(name);
	}

	public void setGlobalAttribute(String name, Object value)
	{
		servlet.getServletContext().setAttribute(name, value);
	}

	public void removeAttribute(String name)
	{
		request.getSession().removeAttribute(name);
	}

	public void removeTemporaryAttributes()
	{
		for (Iterator<String> attrIter = temporaryAttributes.iterator(); attrIter.hasNext();)
		{
			String name = attrIter.next();
			removeAttribute(name);
			attrIter.remove();
		}
	}

	public String getParameter(String name)
	{
		return request.getParameter(name);
	}

	public int getIntegerParameter(String name)
	{
		try
		{
			return Integer.parseInt(getParameter(name));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public long getLongParameter(String name)
	{
		try
		{
			return Long.parseLong(getParameter(name));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public short getShortParameter(String name)
	{
		try
		{
			return Short.parseShort(getParameter(name));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public float getFloatParameter(String name)
	{
		try
		{
			String param = getParameter(name);
			if (param != null) param = param.replace(',', '.');
			else return -1;
			return Float.parseFloat(param);
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public double getDoubleParameter(String name)
	{
		try
		{
			String param = getParameter(name);
			if (param != null) param = param.replace(',', '.');
			else return -1;
			return Double.parseDouble(param);
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public char getCharParameter(String name)
	{
		String par = getParameter(name);
		if (par != null && par.length() > 0) return par.charAt(0);
		else return '\0';
	}

	public boolean getBooleanParameter(String name)
	{
		String parameter = getParameter(name);
		if (parameter == null) return false;
		if ("1".equals(parameter) || "true".equalsIgnoreCase(parameter)) return true;
		return false;
	}

	public int[] getIntegerArrayParameter(String name)
	{
		String[] values = request.getParameterValues(name + "[]");
		if (values == null) return null;
		int[] ret = new int[values.length];
		int p = 0;
		for (String value : values)
		{
			try
			{
				ret[p] = Integer.parseInt(value);
				p++;
			}
			catch (NumberFormatException e)
			{}
		}
		return ret;
	}

	protected void sendDispatch(String path) throws ServletException, IOException
	{
		if (path.startsWith("/")) servlet.getServletContext().getRequestDispatcher(path).forward(request, response);
		else servlet.getServletContext().getRequestDispatcher("/" + path).forward(request, response);
	}

	protected void redirect(String path) throws IOException
	{
		response.sendRedirect(path);
		// log.debug("sendRedirect(" + path + ")");
		response.getWriter().println("<a href=\"" + path + "\">" + path + "</a>");
	}

	public Page(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns)
	{
		this.servlet = servlet;
		this.request = request;
		this.response = response;
		this.dns = dns;
		this.db = dns.getDatabase();
		this.config = dns.getConfig();

		log = Logger.getLogger(Page.class);
		ROOT = config.getString("web.root", request.getContextPath());
		String className = this.getClass().getSimpleName();
		className = className.substring(0, className.length() - 4).toLowerCase();
		MODULE = ROOT + "/" + className;
		SELF = ROOT + servlet.localURI;

		return_to = getParameter("return_to");

		cookies = getCookies();

		setAttribute("root", ROOT);
		setAttribute("self", SELF);
		setAttribute("module", MODULE);
		setAttribute("pageName", className);
		setAttribute("version", DNS.version);
	}

	private Map<String, String> getCookies()
	{
		Map<String, String> mycookies = new HashMap<String, String>();
		Cookie[] cookieArray = request.getCookies();
		if (cookieArray != null && cookieArray.length > 0)
		{
			for (Cookie cookie : request.getCookies())
			{
				if (!cookie.getValue().isEmpty()) mycookies.put(cookie.getName(), cookie.getValue());
			}
		}
		return mycookies;
	}

	protected void removeCookie(String name)
	{
		for (Cookie cookie : request.getCookies())
		{
			if (cookie.getName().equals(name))
			{
				cookie.setMaxAge(0);
				cookie.setValue("");
				response.addCookie(cookie);
				return;
			}
		}
	}

	protected void notFound(String message) throws IOException
	{
		response.sendError(404, message);
	}

	protected void notFound() throws IOException
	{
		notFound("Not found");
	}

	public abstract void run(String path) throws Exception;

	protected HashMap<String, Object> getParams()
	{
		HashMap<String, Object> params = new HashMap<String, Object>();
		Pattern namePattern = Pattern.compile("(.+)\\[(.+)\\]");
		for (String name : new EnumerationIterator<String>(request.getParameterNames()))
		{
			Matcher m = namePattern.matcher(name);
			if (m.find())
			{
				String baseName = m.group(1);
				String key = m.group(2);
				HashMap<String, Object> map = null;
				if (params.containsKey(baseName) && params.get(baseName) instanceof Map)
				{
					map = (HashMap<String, Object>) params.get(baseName);
				}
				if (map == null)
				{
					map = new HashMap<String, Object>();
					params.put(baseName, map);
				}

				String[] vals = request.getParameterValues(name);
				if (vals.length == 1) map.put(key, vals[0]);
				else if (vals.length > 1) map.put(key, vals);
			}
			else
			{
				String[] vals = request.getParameterValues(name);
				if (name.endsWith("[]"))
				{
					name = name.substring(0, name.length() - 2);
					params.put(name, Arrays.asList(vals));
				}
				else if (vals.length == 1) params.put(name, vals[0]);
				else if (vals.length > 1) params.put(name, Arrays.asList(vals));
			}
		}

		return params;
	}

	protected Map<String, String> getGrid(String baseName)
	{
		Pattern namePattern = Pattern.compile(baseName + "\\[(.+)\\]");
		Map<String, String> gridValues = new HashMap<String, String>();
		Enumeration<String> e = request.getParameterNames();
		while (e.hasMoreElements())
		{
			String parameterName = e.nextElement();
			Matcher nameMatcher = namePattern.matcher(parameterName);
			if (nameMatcher.find())
			{
				String n = nameMatcher.group(1);
				gridValues.put(n, getParameter(parameterName));
			}
		}
		String[] keys = request.getParameterValues(baseName + "_keys[]");
		String[] values = request.getParameterValues(baseName + "_values[]");
		if (keys != null && values != null && keys.length == values.length)
		{
			for (int c = 0; c < keys.length; c++)
			{
				if (!keys[c].isEmpty()) gridValues.put(keys[c], values[c]);
			}
		}
		return gridValues;
	}

	protected String getGridHTML(Map<String, String> data, String baseName)
	{
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> e : data.entrySet())
		{
			builder.append("<label for=" + baseName + "_" + e.getKey() + "><span>" + e.getKey() + "</span> <input type=\"text\" name=\"" + baseName
					+ "[" + e.getKey() + "]\" id=\"" + baseName + "_" + e.getKey() + "\" value=\"" + e.getValue() + "\" /></label>\n");
		}
		for (int c = 1; c <= 5; c++)
		{
			builder.append("<label><span><input type=\"text\" name=\"" + baseName + "_keys[]\" /></span> <input type=\"text\" name=\"" + baseName
					+ "_values[]\" /></label>\n");
		}
		return builder.toString();
	}

	protected boolean returnTo() throws IOException
	{
		return returnTo(false);
	}

	protected boolean returnTo(boolean returnToBase) throws IOException
	{

		if (getAttribute("return_to") != null)
		{
			redirect((String) getAttribute("return_to"));
			removeAttribute("return_to");
			return true;
		}
		else if (return_to != null)
		{
			redirect(return_to);
			return true;
		}
		else if (returnToBase)
		{
			redirect(ROOT + "/");
			return true;
		}
		else return false;
	}

	protected void showMessage(String message) throws ServletException, IOException
	{
		setAttribute("message", message);
		sendDispatch("message.jsp");
	}

	protected void flashMessage(String message)
	{
		setAttribute("flash", message, true);
	}

	protected void flashError(String message)
	{
		flashMessage(message);
		setAttribute("flash_type", "error", true);
	}

}
