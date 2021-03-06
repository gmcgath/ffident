package metadata.identification;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Static helper class that tries to identify the file format 
 * for a given file or byte array representing the first bytes of a file.
 * <h3>Usage</h3>
 * <pre>
 * FormatDescription desc = FormatIdentification.identify(new File("testfile.zip"));
 * if (desc != null) {
 *   System.out.println(desc.getShortName());
 * }
 * </pre>
 * @author Marco Schmidt
 */
public class FormatIdentification
{
	private static List descriptions;
	private static int minBufferSize;

	static
	{
		init();
	}

	/**
	 * Private empty constructor to avoid instantiations.
	 */
	private FormatIdentification()
	{
	}

	public static FormatDescription identify(byte[] data)
	{
		if (data == null || data.length < 1)
		{
			return null;
		}
		Iterator iter = descriptions.iterator();
		while (iter.hasNext())
		{
			FormatDescription desc = (FormatDescription)iter.next();
			if (desc.matches(data))
			{
				return desc;
			}
		}
		return null;
	}

	public static FormatDescription identify(File file)
	{
		if (!file.isFile())
		{
			return null;
		}
		long size = file.length();
		int numBytes;
		if (size > minBufferSize)
		{
			numBytes = minBufferSize;
		}
		else
		{
			numBytes = (int)size;
		}
		byte[] data = new byte[numBytes];
		RandomAccessFile in = null;
		try
		{
			in = new RandomAccessFile(file, "r");
			in.readFully(data);
			in.close();
		}
		catch (IOException ioe)
		{
			return null;
		}
		finally
		{
			try
			{
				if (in != null)
				{
					in.close();
				}
			}
			catch (IOException ioe)
			{
				// 
			}
		}
		return identify(data);
	}

	private static void init()
	{
		descriptions = new ArrayList();
		minBufferSize = 1;
		try
		{
			InputStream input = //descriptions.getClass().getResourceAsStream("/metadata/formats.txt");
			FormatIdentification.class.getClassLoader().getResourceAsStream("metadata/formats.txt");
			if (input == null)
			{
				return;
			}
			FormatDescriptionReader in = new FormatDescriptionReader(new InputStreamReader(input));
			FormatDescription desc;
			while ((desc = in.read()) != null)
			{
				byte[] magic = desc.getMagicBytes();
				Integer offset = desc.getOffset();
				if (magic != null && offset != null && offset.intValue() + magic.length > minBufferSize)
				{
					minBufferSize = offset.intValue() + magic.length;
				}
				descriptions.add(desc);
			}
			input.close();
		}
		catch (Exception e)
		{
		}
	}
}
