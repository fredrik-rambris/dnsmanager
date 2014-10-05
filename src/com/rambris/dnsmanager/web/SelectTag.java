/* SelectTag.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: SelectTag.java 51 2010-09-07 20:53:50Z boost $
 */
public class SelectTag extends SimpleTagSupport
{
	protected Object values;
	protected Object selectedValue = null;
	protected Object skipValue = null;
	protected String name;
	protected String id = null;
	protected String className = null;
	protected String nullOption = null;
	protected int size = 1;

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
	 */
	@Override
	public void doTag() throws JspException, IOException
	{
		if (selectedValue == null) selectedValue = "";
		JspWriter out = getJspContext().getOut();
		out.print("<select name=\"" + name + "\"" + (id != null ? " id=\"" + id + "\"" : "") + (className != null ? " class=\"" + className + "\"" : "") + (size > 1 ? " size=\"" + size + "\" multiple=\"multiple\"" : "") + ">");
		if (nullOption != null)
		{
			out.print("<option value=\"0\"" + (selectedValue.toString().isEmpty() || selectedValue.toString().equals("0") ? " selected=\"selected\"" : "") + ">" + nullOption + "</option>");
		}
		if (values instanceof Map)
		{
			Map<Object, Object> map = (Map<Object, Object>) values;
			for (Map.Entry<Object, Object> e : map.entrySet())
			{
				String value = e.getKey().toString();
				if (skipValue != null && value.equals(skipValue.toString())) continue;
				String text = e.getValue().toString();
				boolean selected = isSelected(value);
				out.print("<option value=\"" + value + "\"" + (selected ? " selected=\"selected\"" : "") + ">" + text + "</option>");
			}
		}
		else if (values instanceof Iterable)
		{
			Iterable i = (Iterable) values;
			for (Object o : i)
			{
				String value = o.toString();
				if (skipValue != null && value.equals(skipValue.toString())) continue;
				boolean selected = isSelected(value);
				out.print("<option value=\"" + value + "\"" + (selected ? " selected=\"selected\"" : "") + ">" + value + "</option>");
			}
		}
		out.print("</select>");
	}

	protected boolean isSelected(String value)
	{
		if (size > 1)
		{
			if (selectedValue instanceof Iterable)
			{
				Iterable selectedValues = (Iterable) selectedValue;
				for (Object o : selectedValues)
				{
					if (o.equals(value)) return true;
				}
			}
			return false;
		}
		else
		{
			return value.equals(selectedValue.toString());
		}
	}

	public void setValues(Object values)
	{
		this.values = values;
	}

	public void setSelected(Object selectedValue)
	{
		this.selectedValue = selectedValue;
	}

	public void setSkip(Object skip)
	{
		this.skipValue = skip;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setClasses(String className)
	{
		this.className = className;
	}

	public void setNullOption(String value)
	{
		this.nullOption = value;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public int getSize()
	{
		return size;
	}

}
