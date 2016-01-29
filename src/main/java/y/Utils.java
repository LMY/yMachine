package y;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	/**
	 * Salva un file di testo
	 * @param filename nome del file da salvare (con percorso assoluto)
	 * @param content contenuto del file di testo
	 * @throws FileNotFoundException se è stato impossibile salvare il file
	 */
	public static void saveText(String filename, String content) throws IOException
	{
		final PrintWriter out = new PrintWriter(filename);
		out.write(content);
		out.close();
	}
	
	/**
	 * Leggi l'intero contenuto di un file di testo
	 * @param filename nome del file da leggere
	 * @return la stringa contentente il testo del file
	 * @throws IOException
	 */
	public static String ReadWholeFile(String filename) throws IOException
	{
		final File file = new File(filename);
		final FileInputStream fis = new FileInputStream(file);
		final byte[] data = new byte[(int)file.length()];
		fis.read(data);
		fis.close();
		return new String(data, "UTF-8");
	}
	
	public static boolean WriteWholeFile(String filename, String content) {
		
		BufferedWriter fos = null;
		
		try {
			fos = new BufferedWriter(new FileWriter(filename));
			fos.write(content);
			return true;
		}
		catch (Exception e) { return false; }
		finally {
			if (fos != null)
			try { fos.close(); }
			catch (Exception xx) {}
		}
	}
	
	/**
	 * Leggi le prime nlines da un file di testo e tornale
	 * @param filename nome del file da cui leggere
	 * @param nlines numero di righe da leggere
	 * @return il testo risultante, "" se il file è vuoto o non esistente. Se si verifica un errore viene tornato tutto il testo che è stato possibile leggere
	 */
	public static String peekFile(String filename, int nlines)
	{
		String res = "";
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(filename));
			
			for (int l=0; l<nlines; l++) {
				final String tl = br.readLine();
				if (tl == null)
					break;
				if (!res.isEmpty())
					res += "\n";
				res += tl;
			}
		}
		catch (Exception e) {}
		finally {
			try { br.close(); }
			catch (Exception e) {}
		}

		return res;
	}
		
		
	public static String getFolderOfFile(String filename)
	{
		try { return new File(filename).getParent(); }
		catch (Exception e) { return ""; }
	}
	
	public static String formatDouble(double value, int numdec)
	{
		if (value < 0) return "-" + formatDouble(-value, numdec);
		
		if (numdec >= 0)
			return String.format("%1$."+numdec+"f", value).replace(',', '.');
		else
			return ""+value;
	}
	
	public static String formatDouble(double value, int intcip, int numdec)
	{
		if (value < 0) return "-" + formatDouble(-value, intcip, numdec);
		
		if (numdec > 0)
			return String.format("%0"+(intcip+numdec+1)+"."+numdec+"f", value).replace(',', '.');
		else if (numdec == 0)
			return String.format("%0"+intcip+".0f", value).replace(',', '.');
		else
			return ""+value;
	}
	
	public static String formatDoubleAsNeeded(double value)
	{
		if (value < 0) return "-" + formatDoubleAsNeeded(-value);
		
		return new DecimalFormat("#.##").format(value).replace(',', '.');
	}
	
	public static String formatDoubleAsNeeded(double value, int maxdec)
	{
		if (value < 0) return "-" + formatDoubleAsNeeded(-value, maxdec);
		
		return new DecimalFormat(maxdec == 0 ? "#" : "#." +stringFromRepChar('#', maxdec) ).format(value).replace(',', '.');
	}
	
	public static String stringFromRepChar(char c, int times)
	{
		 final char[] chars = new char[times];
		 Arrays.fill(chars, c);
		 return new String(chars);
	}
	

	
	public static int numberOfDecimals(double d)
	{
		if ((d == Math.floor(d)) && !Double.isInfinite(d)) 	// if it is an integer
			return 0;
		else {
			final String text = Double.toString(Math.abs(d));
			final int integerPlaces = text.indexOf('.');
			return integerPlaces < 0 ? 0 : text.length() - integerPlaces - 1;
		}
	}
	
	public static void clipboardCopy(String data)
	{
		final StringSelection selection = new StringSelection(data);
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}
	
	public static String clipboardPaste()
	{
		String result = "";
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		final Transferable contents = clipboard.getContents(null);
		final boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);

		if (hasTransferableText) {
			try {
				result = (String)contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (Exception e){
//				System.out.println(ex);
//				ex.printStackTrace();
			}
		}
		return result;
	}
	
	public static int[] getAllIntsInString(String s)
	{
		final Pattern pattern = Pattern.compile("[0-9]+"); 
		final Matcher matcher = pattern.matcher(s);
		
		final ArrayList<Integer> list = new ArrayList<Integer>();
		
		while (matcher.find())
			try {
				list.add(Integer.parseInt(matcher.group()));
			}
			catch (Exception e) {}
		
		final int[] ret = new int[list.size()];
		for (int i=0; i<ret.length; i++)
			ret[i] = list.get(i);
		return ret;
	}
	

	/**
	 * Calcola la permutazione che ordina i dati di ingresso
	 * @param input interi da ordinare
	 * @return array : output[i] è l'indice della posizione dove dovrebbe essere inserito input[i] per ordinare l'array
	 */
	public static <E> int[] ArrayGetSortPermutation(final Collection<E> input, final Comparator<Integer> comparator)
	{
		final int len = input.size();
		final List<Integer> indices = new ArrayList<Integer>(len);
		for (int i=0; i<len; i++)
			indices.add(i);

		Collections.sort(indices, comparator);

		return ArrayIntegerToInt(indices);
	}
	
	public static int[] ArrayIntegerToInt(List<Integer> array)
	{
		final int[] ret = new int[array.size()];

		int i=0;
		for (Iterator<Integer> iter=array.iterator(); iter.hasNext();)
			ret[i++] = iter.next();

		return ret;		
	}
	
	public static void touch(String filename) {
		final File file = new File(filename);
		if (file.exists()) {
			final long now = System.currentTimeMillis();
			final long then = file.lastModified();
			
			if (now > then)
				file.setLastModified(now);
		}
	}
	
	
	public static <T> int[] permutationSort(final List<T> list, final Comparator<T> comparator)
	{
		final int len = list.size();
		final Integer[] indices = new Integer[len];
		for (int i = 0; i < len; i++)
			indices[i] = i;
		
		Arrays.sort(indices, new Comparator<Integer>() {
			public int compare(Integer i, Integer j) {
				return comparator.compare(list.get(i), list.get(j));
			}
		});
		
		final int[] ret = new int[indices.length];
		for (int i=0; i<ret.length; i++)
			ret[i] = indices[i];
		
		return ret;
	}
	
	public static <T> int[] permutationSort(final T[] array, final Comparator<T> comparator)
	{
		final Integer[] indices = new Integer[array.length];
		for (int i = 0; i < array.length; i++)
			indices[i] = i;
		
		Arrays.sort(indices, new Comparator<Integer>() {
			public int compare(Integer i, Integer j) {
				return comparator.compare(array[i], array[j]);
			}
		});
		
		final int[] ret = new int[indices.length];
		for (int i=0; i<ret.length; i++)
			ret[i] = indices[i];
		
		return ret;
	}
	
	public static <T> void applyPermutation(final T[] input, final T[] output, int[] permutation)
	{
		for (int i=0; i<permutation.length; i++)
			output[i] = input[permutation[i]];
	}
	
	public static <T> void applyPermutation(final List<T> input, final List<T> output, int[] permutation)
	{
		for (int i=0; i<permutation.length; i++)
			output.set(i, input.get(permutation[i]));
	}

	/**
	 * Capture console output of a process
	 * @param p Process to capture
	 * @return captured lines
	 */
	public static String captureOutputOfProcess(Process p)
	{
		final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		final StringBuilder text = new StringBuilder();
		
		String line;
		try {
			while ((line = input.readLine()) != null)
				text.append(line + "\n");
		}
		catch (IOException e) {
			text.append("** EXCEPTION ("+e.getMessage()+") **");
		}
		
		try { p.waitFor(); }
		catch (InterruptedException e) {}
		
		return text.toString();
	}
	
	public final static String PathSeparator = File.separator;	// for those times when you already depend upon Utils, but not File
	
	public static boolean mkdirs(String filename) {
		try {
			new File(new File(filename).getParent()).mkdirs();
			return true;
		}
		catch (Exception e) { return false; }
	}
	
	
	public static String getFileExtension(String filename) {
		final int i = filename.lastIndexOf('.');
	    return i > 0 ? filename.substring(i+1) : "";
	}
	
	public static String firstNotEmpty(String... strings) {
		for (final String s : strings)
			if (s != null && !s.isEmpty()) // if (!String.isNullOrEmpty(s))
				return s;
		
		return "";
	}
	
	public static boolean isInteger(String s) {
	    return isInteger(s,10);
	}

	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	
	public static int fromByteArray(byte[] bytes) {
	     return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}
	public static byte[] toByteArray(int value) {
	    return new byte[] { (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value };
	}
	
	public static String[] splitAndTrim(String text, String expr) {
		String[] x = text.split(expr);
		
		for (int i=0; i<x.length; i++)
			x[i] = x[i].trim();
		
		return x;
	}
}
