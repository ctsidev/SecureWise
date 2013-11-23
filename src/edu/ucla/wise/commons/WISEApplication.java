package edu.ucla.wise.commons;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Hashtable;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

/**
 * Class to represent common elements for a given installation of 
 * the wise surveyor or admin java application. Never instantiated; 
 * Handles most interfaces to the file system
 * 
 * @author mrao
 * @author dbell
 * @author ssakdeo
 */
public class WISEApplication {

    static Logger log = Logger.getLogger(WISEApplication.class);
    public static String rootURL;
    
    /* Commenting these out and moving them to Surveyor Class */
    // public String servlet_url, shared_file_url,shared_image_url;
    public static String emailFrom;
    public static String alertEmail;
    public static String emailHost;
    public static String mailUsername;
    public static String mailPassword;
    public static String adminServer;
    public static String imagesPath;
    public static String stylesPath;
    public static String surveyApp;
    public static String sharedFilesLink;
    public static boolean sslEmail;

    public static Hashtable<String, SurveyorApplication> AppInstanceTbl 
    		= new Hashtable<String, SurveyorApplication>();

    private static final String localPropPath = "localwise";
    public static ResourceBundle localProps = null;

    public static ResourceBundle sharedProps = null;

    public static final String htmlExt = ".htm";
    public static final String mailUserNameExt = ".username";
    public static final String mailPasswdExt = ".password";
    
    // public static final String xml_ext = ".xml";
    private static final String WiseDefaultAcctPropID = "wise.email";

    public static String xmlLoc;
    public static Session mailSession; // Holds values for sending message;

    /** private class for JavaMail authentication */
    // private static class MyAuthenticator extends Authenticator
    // {
    // protected PasswordAuthentication getPasswordAuthentication()
    // {
    // String userName = WISE_Application.mail_username;
    // String password = WISE_Application.mail_password;
    // return new PasswordAuthentication(userName, password);
    // }
    // }

    // public static Surveyor_Application retrieveAppInstance(HttpSession s)
    // {
    // Surveyor_Application app =
    // (Surveyor_Application)s.getAttribute("SurveyorInst");
    // return app;
    // }

    private static class VarAuthenticator extends Authenticator {
		String userName = null;
		String password = null;
	
		@SuppressWarnings("unused")
		public VarAuthenticator() {
		    super();
		    userName = sharedProps.getString("mail.username");
		    password = sharedProps.getString("mail.password");
		    System.out.println(userName + "/" + password);
		}
	
		public VarAuthenticator(String uName, String pword) {
		    super();
		    userName = uName;
		    password = pword;
		}
	
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
		    return new PasswordAuthentication(userName, password);
		}
    }

    // Not called
    // public static void check_init(String appContext, PrintWriter out)
    // {
    // if (ApplicationName == null)
    // initialize(appContext);
    // }

    /**
     * Loads all the variables needed to run the application from the properties files
     * 
     * @returns String 	 Exception trace if there is one or null on successful initialization.
     * @throws 	IOException. 
     */
    public static String initialize() throws IOException {
    	
    	/* Load server's local properties */
		String sharedPropPath;
		localProps = ResourceBundle.getBundle(localPropPath,
				Locale.getDefault());
		try {
		    
			/* Loading Local Properties */
		    rootURL = localProps.getString("server.rootURL");
		    sharedPropPath = localProps.getString("shared.Properties.file");
		    if (CommonUtils.isEmpty(rootURL)
		    		|| CommonUtils.isEmpty(sharedPropPath)) {
		    	throw new Exception("Failed to read from local properties");
		    }
		    
		    /* loading shared properties file */
		    sharedProps = ResourceBundle.getBundle(sharedPropPath);
		} catch (NullPointerException  e) {
		    log.error("WISE Application initialization Error: " + e);
		    return e.toString();
		} catch (MissingResourceException  e) {
		    log.error("WISE Application initialization Error: " + e);
		    return e.toString();
		} catch (ClassCastException e) {
		    log.error("WISE Application initialization Error: " + e);
		    return e.toString();
		} catch (Exception e) {
		    log.error("WISE Application initialization Error: " + e);
		    return e.toString();
		}
	
		/* New Single JAR changes, putting everything like it was before, in WISEApplication */
		// servlet_url = rootURL + ApplicationName + "/";
		// servlet_url = rootURL + "/wise_survey" + "/";
		emailFrom = sharedProps.getString("wise.email.from");
		alertEmail = sharedProps.getString("alert.email");
		emailHost = sharedProps.getString("email.host");
		mailUsername = sharedProps.getString("SMTP_AUTH_USER");
		mailPassword = sharedProps.getString("SMTP_AUTH_PASSWORD");
		adminServer = sharedProps.getString("admin.server");
		xmlLoc = sharedProps.getString("xml_root.path");
		sslEmail="true".equalsIgnoreCase(sharedProps.getString("email.ssl"));
		stylesPath = sharedProps.getString("shared_style.path");
		imagesPath = sharedProps.getString("shared_image.path");
		
		// images_path = sharedProps.getProperty("wise.images_path");
		sharedFilesLink = localProps
				.getString("default.sharedFiles_linkName");
		logInfo("images_path read is: " + imagesPath + "");
		
		// WISE_Application.shared_file_url = rootURL +
		// WISE_Application.ApplicationName + "/" +
		// localProps.getProperty("default.sharedFiles_linkName") + "/";
		// WISE_Application.shared_file_url = rootURL + "/wise_survey/" +
		// localProps.getProperty("default.sharedFiles_linkName") + "/";
		
		if (CommonUtils.isEmpty(xmlLoc)) {
		    return "WISE Application initialization Error: Failed to read from Shared properties file "
			    + sharedPropPath + "\n";
		}
		
		/* set up Study_Space class -- pre-reads from sharedProps */	
		StudySpace.setupStudies(); 
	
		/* setup default email session for sending messages -- WISE needs this to send alerts */
		mailSession = getMailSession(WiseDefaultAcctPropID); 
		if (mailSession == null) {
		    return "WISE Application initialization Error: Failed to initialize mail session\n";
		}
		return null; // success!
    }

    /**
     * Loads all the variables needed to run the application from the properties files
     * 
     * @param 	errStr 		The error message that has to be printed.	
     * @returns PrintStream PrintStream that prints the input error message.

     */    
    static PrintStream initError(String errStr) {
		PrintStream ps = null;
		try {
		    FileOutputStream fos = new FileOutputStream("WISE_errors.txt", true);
		    ps = new PrintStream(fos, true);
		    ps.print(errStr);
		} catch (FileNotFoundException e) {
		    System.err.println(e.toString());
		    e.printStackTrace(System.err);
		}catch (SecurityException  e) {
		    System.err.println(e.toString());
		    e.printStackTrace(System.err);
		}catch (Exception e) {
		    System.err.println(e.toString());
		    e.printStackTrace(System.err);
		}
		return ps;
    }

    /**
     * Send an email alert to someone. Older version of this application used to
     * send email alert for every error. This would flood inbox trememdously.
     * Replaced this by logging errors, an industry standard of recording events
     * having in an application.
     * 
     * @param 	email_to 	Email Id to whom mail has to be sent.
     * @param 	subject		Subject of the Email.
     * @param	body		Body of the Email.	
     * 
     */
    @Deprecated
    public static void sendEmail(String email_to, String subject, String body) {
		try {
		    MimeMessage message = new MimeMessage(mailSession);
		    message.setFrom(new InternetAddress(emailFrom));
		    message.addRecipient(javax.mail.Message.RecipientType.TO,
		    		new InternetAddress(email_to));
		    message.setSubject(subject);
		    message.setText(body);
	
		    /* Send message */
		    Transport.send(message);
		} catch (AddressException e) {
		    logError("WISE_Application - SEND_EMAIL error: " + "\n" + body, e);
		} catch ( MessagingException e) {
		    logError("WISE_Application - SEND_EMAIL error: " + "\n" + body, e);
		} 
    }

    /**
     * Log error message to wise.log which is present in $CATALINA_HOME/bin The
     * only problem with this way of logging is every log will be named after
     * the {@link WISEApplication} class. But proper messagebody and exception
     * should help track where the error went wrong.
     * 
     * @param body	String that is sent to error() method.
     * @param e		Exception which has to be logged.
     */
    public static void logError(String body, Exception e) {
    	log.error(body, e);
    }

    /**
     * Log information message to wise.log which is present in $CATALINA_HOME/bin The
     * only problem with this way of logging is every log will be named after
     * the {@link WISEApplication} class. But proper messagebody and exception
     * should help track where the error went wrong.
     * 
     * @param body	The information that has to be logged.
     */
    public static void logInfo(String body) {
    	log.info(body);
    }

    /**
     * decoding - convert character-formatted ID to be the digit-formatted *
     * TOGGLE NAME OF THIS FUNCTION to move to production mode
     * 
     * @param  charId 	The String to decode.
     * @return String 	The decoded input.
     */
    public static String decode(String charId) {
		String result = new String();
		int sum = 0;
		for (int i = charId.length() - 1; i >= 0; i--) {
		    char c = charId.charAt(i);
		    int remainder = c - 65;
		    sum = sum * 26 + remainder;
		}
	
		sum = sum - 97654;
		int remain = sum % 31;
		if (remain == 0) {
		    sum = sum / 31;
		    result = Integer.toString(sum);
		} else {
		    result = "invalid";
		}
		return result;
    }

    //public static String decodeTest(String charId) {
	//return charId;
    //}

    /**
     * encoding - convert digit-formatted ID to be the character-formatted
     * TOGGLE NAME OF THIS FUNCTION to move to production mode
     * 
     * @param  charId 	The String to encode.
     * @return String 	The encoded input.
     */
    public static String encode(String userId) {
		int baseNumb = Integer.parseInt(userId) * 31 + 97654;
		String s1 = Integer.toString(baseNumb);
		String s2 = Integer.toString(26);
		BigInteger b1 = new BigInteger(s1);
		BigInteger b2 = new BigInteger(s2);
	
		int counter = 0;
		String charId = new String();
		while (counter < 5) {
		    BigInteger[] bs = b1.divideAndRemainder(b2);
		    b1 = bs[0];
		    int encodeValue = bs[1].intValue() + 65;
		    charId = charId + (new Character((char) encodeValue).toString());
		    counter++;
		}
		return charId;
    }

    //public static String encode_test(String user_id) {
	//return user_id;
    //}

    // TODO not used
    // public static String read_localProp(String prop_name) {
    // File prop_file = new File(localPropPath);
    // try {
    // FileInputStream in = new FileInputStream(prop_file);
    // localProps.load(in);
    // in.close();
    // } catch (Exception e) {
    // email_alert("WISE Application Error reading local property: ", e);
    // }
    // return localProps.getString(prop_name);
    // }

    /* return the default session if null */
    public static Session getMailSession(String fromAcct) {
		if (fromAcct == null) {
		    fromAcct = WiseDefaultAcctPropID;
		}
		String uname = sharedProps.getString(fromAcct + mailUserNameExt);
		String pwd = sharedProps.getString(fromAcct + mailPasswdExt);
	
		String smtpAuthUser = sharedProps.getString("SMTP_AUTH_USER");
		String smtpAuthPassword = sharedProps.getString("SMTP_AUTH_PASSWORD");
		String smtpAuthPort = sharedProps.getString("SMTP_AUTH_PORT");
		boolean tempsslEmail="true".equalsIgnoreCase(sharedProps.getString("email.ssl"));
		
		/*
		 * Pralav has commented old code Properties sys_props =
		 * System.getProperties(); // setup the mail server in system properties
		 * sys_props.put("mail.smtp.host", email_host);
		 * sys_props.put("mail.smtp.auth", CommonUtils.isEmpty(pwd) ? "false" :
		 * "true"); Authenticator auth = CommonUtils.isEmpty(pwd) ? null : new
		 * VarAuthenticator(uname, pwd);
		 */// Pralav's comment ends here
	
		/* Set the host smtp address */
		if (tempsslEmail) {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", emailHost);
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.port", smtpAuthPort);
			props.put("mail.smtp.user", smtpAuthUser);
			props.put("mail.smtp.password", smtpAuthPassword);
			String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
			props.setProperty("mail.smtp.socketFactory.port", smtpAuthPort);
			props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
			props.setProperty("mail.smtp.socketFactory.fallback", "false");
			props.setProperty("mail.smtp.connectiontimeout", "10000");
			props.setProperty("mail.smtp.timeout", "10000");		
			Authenticator auth = new VarAuthenticator(uname, pwd);
		
			/* create the message session */
			return Session.getInstance(props, auth);
		} else{
			Properties props = System.getProperties();
			props.put("mail.smtp.host", emailHost);
			props.setProperty("mail.smtp.connectiontimeout", "10000");
			props.setProperty("mail.smtp.timeout", "10000");
			return Session.getInstance(props);
		}			
	}
}

