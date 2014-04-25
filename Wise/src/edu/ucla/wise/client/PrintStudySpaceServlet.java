package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.StudySpace;

/**
 * PrintStudySpaceServlet class used to print a study space, which should force
 * a load.
 * 
 * @author Douglas Bell
 * @version 1.0
 */
@WebServlet("/survey/print_study_space")
public class PrintStudySpaceServlet extends HttpServlet {
    static final long serialVersionUID = 1000;
    private static final Logger LOGGER = Logger
	    .getLogger(PrintStudySpaceServlet.class);

    /**
     * Prints the study space on to the screen.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
	    throws ServletException, IOException {

	/* prepare for writing */
	PrintWriter out;
	res.setContentType("text/html");
	out = res.getWriter();

	/* get requested study space ID */
	String spaceId = req.getParameter("ss");
	out.println("<p>Requested study space is: " + spaceId + "</p>\n");
	StudySpace theStudy = StudySpace.getSpace(spaceId);
	if (theStudy != null) {
	    out.println(theStudy.toString());
	} else {
	    out.println("Retrieve of study failed.\n");
	}
	out.println("<br>Done.");
	out.close();
    }
}