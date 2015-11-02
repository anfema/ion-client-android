package com.anfema.ampclient.service.response_gsons;

public class Meta
{
	private String previous;

	private int count;

	private String next;


	public String getPrevious()
	{
		return previous;
	}

	public void setPrevious( String previous )
	{
		this.previous = previous;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount( int count )
	{
		this.count = count;
	}

	public String getNext()
	{
		return next;
	}

	public void setNext( String next )
	{
		this.next = next;
	}

	@Override
	public String toString()
	{
		return "ClassPojo [previous = " + previous + ", count = " + count + ", next = " + next + "]";
	}
}
