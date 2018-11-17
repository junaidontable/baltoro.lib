package io.baltoro.client;

public class ColumnMetadata
{

	private String colName;
	private int maxLen;
	private long maxValue;
	private int minValue;
	private int colIndex;
	
	
	ColumnMetadata(String colName)
	{
		this.colName = colName;
	}
	
	
	
	public String getColName()
	{
		return colName;
	}



	public int getMaxLen()
	{
		return maxLen;
	}
	void setMaxLen(int maxLen)
	{
		if(maxLen > this.maxLen)
		{
			this.maxLen = maxLen;
		}
	}
	
	public long getMaxValue()
	{
		return maxValue;
	}
	void setMaxValue(int maxValue)
	{
		if(maxValue > this.maxValue)
		{
			this.maxValue = maxValue;
		}
	}
	
	public int getMinValue()
	{
		return minValue;
	}
	void setMinValue(int minValue)
	{
		this.minValue = minValue;
	}
	
	public int getColIndex()
	{
		return colIndex;
	}
	void setColIndex(int colIndex)
	{
		this.colIndex = colIndex;
	}
	
	
}
