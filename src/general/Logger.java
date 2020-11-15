package general;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {
	
	File log_file;
	
	public Logger(String log_file_name)
	{
	  try {
	      this.log_file = new File(log_file_name);
	      if (!this.log_file.createNewFile())
	    	  new PrintWriter(log_file_name).close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	}
	
	public Logger()
	{
		this("log.txt");
	}
	
	private Date getTimeFromLine(String line)
	{
		String date_string = line;
		date_string = date_string.substring(date_string.indexOf("[") + 1);
		date_string = date_string.substring(0, date_string.indexOf("]"));
		
		try {
			Date date = new SimpleDateFormat("HH:mm:ss").parse(date_string);
			return date;			
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    return null;
	}
	
	private long getTimeDifference(Date d1, Date d2, TimeUnit time_unit)
	{
		long diff_millis =  Math.abs(d1.getTime() - d2.getTime());
		return time_unit.convert(diff_millis, TimeUnit.MILLISECONDS);
	}
	
	
	private String getCurrentTimePrefix()
	{
		SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		return "[" + formatter.format(date) + "]";
	}
	
	synchronized public void log(String content)
	{
		content = content.substring(0, 1).toUpperCase() + content.substring(1); //capitalize first letter
		content = this.getCurrentTimePrefix() + " " + content + System.lineSeparator();
		
		try {
		    Files.write(Paths.get(this.log_file.getName()), content.getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	synchronized public void log(String content, boolean print)
	{
		this.log(content);
		if (print)
			System.out.println(content);
	}

}
