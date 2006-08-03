package freemail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.IOException;

/** A postman is any class that delivers mail to an inbox. Simple,
 *  if not politically correct.
 */
public class Postman {
	private static final int BOUNDARY_LENGTH = 32;

	protected void storeMessage(BufferedReader brdr, MessageBank mb) throws IOException {
		MailMessage newmsg = mb.createMessage();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		
		// add our own headers first
		// recieved and date
		newmsg.addHeader("Received", "(Freemail); "+sdf.format(new Date()));
		
		newmsg.readHeaders(brdr);
		
		PrintStream ps = newmsg.writeHeadersAndGetStream();
		
		String line;
		while ( (line = brdr.readLine()) != null) {
			ps.println(line);
		}
		
		newmsg.commit();
		brdr.close();
	}
	
	public static boolean bounceMessage(File origmsg, MessageBank mb, String errmsg) {
		return bounceMessage(origmsg, mb, errmsg, false);
	}
	
	public static boolean bounceMessage(File origmsg, MessageBank mb, String errmsg, boolean isFreemailFormat) {
		MailMessage bmsg = null;
		try {
			bmsg = mb.createMessage();
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
			
			bmsg.addHeader("From", "Freemail Postmaster <postmaster@freemail>");
			bmsg.addHeader("Subject", "Undeliverable Freemail");
			String origFrom = extractFromAddress(origmsg, isFreemailFormat);
			if (origFrom != null)
				bmsg.addHeader("To", origFrom);
			bmsg.addHeader("Date", sdf.format(new Date()));
			bmsg.addHeader("MIME-Version", "1.0");
			String boundary="boundary-";
			Random rnd = new Random();
			int i;
			for (i = 0; i < BOUNDARY_LENGTH; i++) {
				boundary += (char)(rnd.nextInt(25) + (int)'a');
			}
			bmsg.addHeader("Content-Type", "Multipart/Mixed; boundary=\""+boundary+"\"");
			
			PrintStream ps = bmsg.writeHeadersAndGetStream();
			
			ps.println("--"+boundary);
			ps.println("Content-Type: text/plain");
			ps.println("Content-Disposition: inline");
			ps.println("");
			ps.println("Freemail was unable to deliver your message. The problem was:");
			ps.println("");
			ps.println(errmsg);
			ps.println("");
			ps.println("The original message is included below.");
			ps.println("");
			ps.println("--"+boundary);
			ps.println("Content-Type: message/rfc822;");
			ps.println("Content-Disposition: inline");
			ps.println("");
			
			BufferedReader br = new BufferedReader(new FileReader(origmsg));
			
			String line;
			if (isFreemailFormat) {
				while ( (line = br.readLine()) != null) {
					if (line.length() == 0) break;
				}
			}
			
			while ( (line = br.readLine()) != null) {
				if (line.indexOf(boundary) > 0) {
					// The random boundary string appears in the
					// message! What are the odds!?
					// try again
					br.close();
					bmsg.cancel();
					bounceMessage(origmsg, mb, errmsg);
				}
				ps.println(line);
			}
			
			br.close();
			ps.println("--"+boundary);
			bmsg.commit();
		} catch (IOException ioe) {
			if (bmsg != null) bmsg.cancel();
			return false;
		}
		return true;
	}
	
	private static String extractFromAddress(File msg, boolean isFreemailFormat) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(msg));
			
			String line;
			if (isFreemailFormat) {
				while ( (line = br.readLine()) != null) {
					if (line.length() == 0) break;
				}
			}
			
			while ( (line = br.readLine()) != null) {
				if (line.length() == 0) return null;
				String[] parts = line.split(": ", 2);
				if (parts.length < 2) continue;
				if (parts[0].equalsIgnoreCase("From")) {
					br.close();
					return parts[1];
				}
			}
			br.close();
		} catch (IOException ioe) {
		}
		return null;
	}
}