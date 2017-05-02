package jbignums.CalculatorPlugin.StringCalculator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Basic String expression Parser class.
 * Parses a NumberFormat-type calculation expression into an XML format.
 * - For example. We have an expression:
 *      2 + (9*5) * log(2)
 * - We get this result:
 *
 * <number>2</number>
 * <oper>plus</oper>
 * <block>
 *     <number>9</number>
 *     <oper>mul</oper>
 *     <number>5</number>
 * </block>
 * <oper>mul</oper>
 * <func code="log">
 *     <arg>2</arg>
 * </func>
 *
 * ----------------------------------------
 * - We get this by traversing the string while searching for numbers and operations.
 */
public class StringExpressionParserXML {
    /*private final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private final XMLEventFactory  eventFactory  = XMLEventFactory.newInstance();

    private final StringWriter outputWriter = new StringWriter();
    private String xmlResult;
    private XMLEventWriter xmlWrit;*/

    private final DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder documentBuilder = null;

    private Document calcDoc = null;

    private boolean isWorking = false;

    {
        try {
            documentBuilder = documentFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Document parseStringToXML(String expression) throws XMLStreamException {
        synchronized (this){
            if(isWorking || documentBuilder==null){
                System.out.println(isWorking ? "Work is already in progress!" : "DocumentBuilder is NULL!");
                return null;
            }
            isWorking = true;
        }

        calcDoc = documentBuilder.newDocument();
        calcDoc.appendChild( calcDoc.createElement("Nyaa") );

        System.out.print(calcDoc.getElementById("1"));


        synchronized (this){
            //xmlResult = outputWriter.toString();
            isWorking = false;
        }
        return calcDoc;
    }

    public synchronized boolean isWorkingNow(){
        return isWorking;
    }

    public synchronized Document getDOMDocument(){
        return calcDoc;
    }

    /*public synchronized String getStringResult(){
        if(isWorking) return null;
        return calcDoc.;
    }

    public synchronized XMLEventWriter getOriginalXMLWriter(){
        if(isWorking) return null;
        return xmlWrit;
    }

    public synchronized XMLEventReader getXMLEventReader(){
        if(isWorking || xmlResult==null) // Can only read from the String source of the finished parser.
            return null;

        // Create an EventReader reading from the Result string.
        XMLEventReader xmlRead;
        try {
            xmlRead = XMLInputFactory.newInstance().createXMLEventReader(new StringReader(xmlResult));
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return null;
        }
        return xmlRead;
    }*/
}
