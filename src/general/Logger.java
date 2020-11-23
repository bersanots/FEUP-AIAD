package general;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Logger {

	File log_file;

	public Logger(String log_file_name) {
		this.log_file = this.createLogFile(log_file_name);
	}

	public Logger() {
		this("log");
	}

	private String getNormalizedFileName(String file_name) {
		String normalized_file_name = file_name;

		String[] parts = file_name.split("\\.", 2);
		if (parts.length < 2)
			normalized_file_name += ".txt";
		else if (!parts[1].equals("txt")) {
			normalized_file_name = normalized_file_name.substring(0, normalized_file_name.length() - parts[1].length());
			normalized_file_name += "txt";
		}

		return normalized_file_name;
	}

	public File createLogFile(String log_file_name) {
		log_file_name = this.getNormalizedFileName(log_file_name);
		File file = null;

		try {
			file = new File(log_file_name);
			if (!file.createNewFile())
				new PrintWriter(log_file_name).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	private Date getTimeFromLine(String line) {
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

	public long getTimeDifference(Date d1, Date d2, TimeUnit time_unit) {
		long diff_millis = Math.abs(d1.getTime() - d2.getTime());
		return time_unit.convert(diff_millis, TimeUnit.MILLISECONDS);
	}

	private String getLastNLines(String file_name, int n) {
		file_name = this.getNormalizedFileName(file_name);
		File file = new File(file_name);

		RandomAccessFile fileHandler = null;
		try {
			fileHandler = new RandomAccessFile(file, "r");
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();
			int line = 0;

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					if (filePointer < fileLength)
						line++;
				}
				else if (readByte == 0xD) {
					if (filePointer < fileLength - 1)
						line++;
				}
				if (line >= n)
					break;

				sb.append((char) readByte);
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (fileHandler != null)
				try {
					fileHandler.close();
				} catch (IOException e) {
				}
		}
	}

	private List<String> getLastNLinesList(String file_name, int n) {
		List<String> lines = new ArrayList<>();
		String lines_str = this.getLastNLines(file_name, n + 1);

		String[] parts = lines_str.split("\n", n);
		for (int i = 0; i < parts.length; i++)
			lines.add(parts[i]);

		return lines;
	}

	public long getRoundTripTime(String file_name, TimeUnit time_unit) {
		file_name = this.getNormalizedFileName(file_name);
		List<String> lines = this.getLastNLinesList(file_name, 2);
		Date leave_time = this.getTimeFromLine(lines.get(0));
		Date return_time = this.getTimeFromLine(lines.get(1));

		return this.getTimeDifference(leave_time, return_time, time_unit);
	}

	private String getCurrentTimePrefix() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		return "[" + formatter.format(date) + "]";
	}

	synchronized public void log(String file_name, String content) {
		file_name = this.getNormalizedFileName(file_name);
		content = content.substring(0, 1).toUpperCase() + content.substring(1); // capitalize first letter
		content = this.getCurrentTimePrefix() + " " + content + System.lineSeparator();

		try {
			Files.write(Paths.get(file_name), content.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	synchronized public void log(String content) {
		this.log(this.log_file.getName(), content);
	}

	synchronized public void log(String content, boolean print) {
		this.log(content);
		if (print)
			System.out.println(content);
	}

}
