package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class VerbosePrintStream extends PrintStream {

	public final boolean verbose = false;
	
	public VerbosePrintStream(OutputStream arg0) {
		super(arg0);
	}

	public VerbosePrintStream(String arg0) throws FileNotFoundException {
		super(arg0);
	}

	public VerbosePrintStream(File arg0) throws FileNotFoundException {
		super(arg0);
	}

	public VerbosePrintStream(OutputStream arg0, boolean arg1) {
		super(arg0, arg1);
	}

	public VerbosePrintStream(String arg0, String arg1)
			throws FileNotFoundException, UnsupportedEncodingException {
		super(arg0, arg1);
	}

	public VerbosePrintStream(File arg0, String arg1)
			throws FileNotFoundException, UnsupportedEncodingException {
		super(arg0, arg1);
	}

	public VerbosePrintStream(OutputStream arg0, boolean arg1, String arg2)
			throws UnsupportedEncodingException {
		super(arg0, arg1, arg2);
	}

	

	/* (non-Javadoc)
	 * @see java.io.PrintStream#print(boolean)
	 */
	@Override
	public void print(boolean arg0) {
		if (verbose)
			super.print(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#print(char)
	 */
	@Override
	public void print(char arg0) {
		if (verbose)
			super.print(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#print(char[])
	 */
	@Override
	public void print(char[] arg0) {
		if (verbose)
			super.print(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#print(double)
	 */
	@Override
	public void print(double arg0) {
		if (verbose)
			super.print(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#print(float)
	 */
	@Override
	public void print(float arg0) {
		if (verbose)
			super.print(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#print(int)
	 */
	@Override
	public void print(int arg0) {
		if (verbose)
			super.print(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#print(long)
	 */
	@Override
	public void print(long arg0) {
		if (verbose)
			super.print(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#print(java.lang.Object)
	 */
	@Override
	public void print(Object arg0) {
		if (verbose)
			super.print(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#print(java.lang.String)
	 */
	@Override
	public void print(String arg0) {
		if (verbose)
			super.print(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println()
	 */
	@Override
	public void println() {
		if (verbose)
			super.println();
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println(boolean)
	 */
	@Override
	public void println(boolean arg0) {
		if (verbose)
			super.println(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println(char)
	 */
	@Override
	public void println(char arg0) {
		if (verbose)
			super.println(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println(char[])
	 */
	@Override
	public void println(char[] arg0) {
		if (verbose)
			super.println(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println(double)
	 */
	@Override
	public void println(double arg0) {
		if (verbose)
			super.println(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println(float)
	 */
	@Override
	public void println(float arg0) {
		if (verbose)
			super.println(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println(int)
	 */
	@Override
	public void println(int arg0) {
		if (verbose)
			super.println(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println(long)
	 */
	@Override
	public void println(long arg0) {
		if (verbose)
			super.println(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println(java.lang.Object)
	 */
	@Override
	public void println(Object arg0) {
		if (verbose)
			super.println(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#println(java.lang.String)
	 */
	@Override
	public void println(String arg0) {
		if (verbose)
			super.println(arg0);
	}


	
}
