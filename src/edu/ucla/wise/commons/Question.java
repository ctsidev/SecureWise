package edu.ucla.wise.commons;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is a subclass of PageItem and represents a question on the page.
 * 
 * @author Douglas Bell
 * @version 1.0
 */
public class Question extends PageItem {
    /** Instance Variables */
    public String stem;
    public String requiredField;
    public boolean oneLine;

    /**
     * Constructor: To fill out the stem of the question and parse the precondition.
     * 
     * @param n DOM node for the question.
     */
    public Question(Node n) {
		
    	/* get the attributes of page item */
		super(n);
		try {
		    
			/* if there is a translation node, display the translated stem */
		    if (this.translationId != null) {
		    	stem = questionTranslated.stem;
		    } else {
		    	
		    	/* otherwise, display the stem transformed through jaxp parser */
				NodeList nodelist = n.getChildNodes();
				for (int i = 0; i < nodelist.getLength(); i++) {
				    if (nodelist.item(i).getNodeName().equalsIgnoreCase("Stem")) {
					Node node = nodelist.item(i);
					Transformer transformer = TransformerFactory
							.newInstance().newTransformer();
					StringWriter sw = new StringWriter();
					transformer.transform(new DOMSource(node),
							new StreamResult(sw));
					stem = sw.toString();
				    }
				}
		    }
		    // //parse the precondition
		    // NodeList nodelist = n.getChildNodes();
		    // for (int i=0; i < nodelist.getLength();i++)
		    // {
		    // if
		    // (nodelist.item(i).getNodeName().equalsIgnoreCase("Precondition"))
		    // {
		    // //hasPrecondition = true;
		    // //create the condition object
		    // cond = new Condition(nodelist.item(i));
		    // }
		    // }
		    
		    /* assign other attributes */
		    NamedNodeMap nnm = n.getAttributes();
		    
		    /* if the question has the required field to fillup */
		    Node n1 = nnm.getNamedItem("requiredField");
		    if (n1 != null) {
		    	requiredField = n1.getNodeValue();
			} else {
		    	requiredField = "false";
			}
		    
		    /* if the question requests one-line presence/layout */
		    n1 = nnm.getNamedItem("oneLine");
		    if (n1 != null) {
		    	oneLine = new Boolean(n1.getNodeValue()).booleanValue();
		    } else {
		    	oneLine = false;
		    }
		} catch (TransformerException e) {
		    WISEApplication
			    	.logError("WISE - QUESTION: " + e.toString(), null);
		    return;
		} catch (TransformerFactoryConfigurationError e) {
		    WISEApplication
		    		.logError("WISE - QUESTION: " + e.toString(), null);
		    return;
		}
		catch (DOMException e) {
		    WISEApplication
		    		.logError("WISE - QUESTION: " + e.toString(), null);
		    return;
		}
    }

    /**
     * Default field count for question
     * 
     * @return int	returns one always which is considered as default.
     */
    public int countFields() {
    	return 1;
    }

    /**
     * Default field names for question
     * 
     * @return Array	return array with one element as "name" which is considered as default.
     */
    public String[] listFieldNames() {
		
    	/* default is to wrap item name in an array */
		String[] fieldNames = new String[1];
		fieldNames[0] = name;
		return fieldNames;
    }

   @Override
    public boolean isRequired() {
		if (requiredField.equalsIgnoreCase("true")) {
		    return true;
		} else {
		    return false;
		}
    }

    /**
     * Returns the stem for indication purpose if the required field is not
     * filled out.
     * 
     * @return String	Required field value.
     */
    public String getRequiredStem() {
		
    	/* assign value "A" to the unfilled required field to let the JavaScript distinguish */
		String s = "A";
		return s;
    }

    /**
     * Gets the average results of this question from survey data table
     * 
     * @param 	page		Page in the survey which contains this question.
     * @param 	whereclause	The invitee's whose responses are to be picked up for average calculation.
     * @return	float		Average of the response.
     */
    public float getAvg(Page page, String whereclause) {
		float avg = 0;
		try {
		    
			/* connect to the database */
		    Connection conn = page.survey.getDBConnection();
		    Statement stmt = conn.createStatement();
		    
		    /* get the average answer of the question from data table */
		    String sql = "select round(avg(" + name + "),1) from "
		    		+ page.survey.getId() + "_data as s where s.invitee in "
		    		+ "(select distinct(invitee) from page_submit where page='"
		    		+ page.id + "' and survey='" + page.survey.getId() + "')";
		    if (!whereclause.equalsIgnoreCase(""))
		    	sql += " and s." + whereclause;
		    stmt.execute(sql);
		    ResultSet rs = stmt.getResultSet();
		    if (rs.next())
		    	avg = rs.getFloat(1);
		    rs.close();
		    stmt.close();
		    conn.close();
		} catch (SQLException e) {
		    AdminApplication.logError("WISE - QUESTION GET AVG: " + e.toString(), e);
		}
		return avg;
    }

    /**
     * Returns table row for the question stem (partial, or complete if not oneLine).
     * 
     * @return	String HTML form of the question.
     */
    public String makeStemHtml() {
		String s = "<tr><td width=10>&nbsp;</td>";
		
		/* display the question start from a new line if it is not requested by one-line layout */
		if (requiredField.equalsIgnoreCase("true")) {
		    if (!oneLine) {
		    	s += "<td colspan='2'>" + stem + " <b>(required)</b></td></tr>";
			} else {
				s += "<td align=left>" + stem + " <b>(required)</b>";
		    }
		} else {
		    if (!oneLine) {
		    	s += "<td colspan='2'>" + stem + "</td></tr>";
		    } else {
		    	s += "<td align=left>" + stem;
		    }
		}

		return s;
    }

    /**
     * renders the question form by printing the stem - used for admin tool:
     * view survey
     */
    // public String render_form()
    // {
    //
    // String s =
    // "<table cellspacing='0' cellpadding='0' width=600' border='0'>";
    // s += "<tr>";
    // s += "<td width=10>&nbsp;</td>";
    // //display the question stem
    // //start from a new line if it is not requested by one-line layout
    // if (requiredField.equalsIgnoreCase("true"))
    // {
    // if(!oneLine)
    // s += "<td colspan='2'>"+stem+" <b>(required)</b>";
    // else
    // s += "<td align=left>"+stem+" <b>(required)</b>";
    // }
    // else
    // {
    // if(!oneLine)
    // s += "<td colspan='2'>"+stem;
    // else
    // s += "<td align=left>"+stem;
    // }
    // if(!oneLine)
    // s += "</td></tr>";
    // return s;
    // }

    /**
     * Prints out the information about a question.
     * 
     * @return	String Information to be printed.
     */
    public String toString() {
	String s = super.toString();
	s += "Stem: " + stem + "<br>";
	s += "Required: " + isRequired() + "<br>";
	if (cond != null)
	    s += cond.toString();
	return s;
    }
}
