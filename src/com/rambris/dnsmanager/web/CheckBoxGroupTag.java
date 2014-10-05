/* SelectTag.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 */
public class CheckBoxGroupTag extends SimpleTagSupport
{
	private Object values;
	private Set<Object> selectedValues = null;
	private String name;
	private String id = null;
	private String className = null;

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
	 */
	@Override
	public void doTag() throws JspException, IOException
	{
		if (selectedValues == null) selectedValues = new HashSet<Object>();
		if (id == null) id = name;
		JspWriter out = getJspContext().getOut();

		if (values instanceof Map)
		{
			Map<Object, Object> map = (Map<Object, Object>) values;
			for (Map.Entry<Object, Object> e : map.entrySet())
			{
				String value = e.getKey().toString();
				String text = e.getValue().toString();
				String thisId = id + "_" + value;
				boolean selected = selectedValues.contains(value);

				out.print("<label for=\"" + thisId + "\"><input type=\"checkbox\" name=\"" + name + "\" value=\"" + value + "\"");
				if (selected) out.print(" checked=\"checked\"");
				if (id != null) out.print(" id=\"" + thisId + "\"");
				if (className != null) out.print(" class=\"" + className + "\"");
				out.print(" /> " + text + "</label><br />");
			}
		}
		else if (values instanceof Iterable)
		{
			Iterable i = (Iterable) values;
			for (Object o : i)
			{
				String value = o.toString();
				String thisId = id + "_" + value;
				boolean selected = selectedValues.contains(value);

				out.print("<label for=\"" + thisId + "\"><input type=\"checkbox\" name=\"" + name + "\" value=\"" + value + "\"");
				if (selected) out.print(" checked=\"checked\"");
				if (id != null) out.print(" id=\"" + thisId + "\"");
				if (className != null) out.print(" class=\"" + className + "\"");
				out.print(" /> " + value + "</label><br />");
			}
		}
	}

	public void setValues(Object values)
	{
		this.values = values;
	}

	public void setSelectedValues(Set selectedValues)
	{
		if (selectedValues == null) selectedValues = new HashSet<Object>();
		this.selectedValues = selectedValues;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}
}
