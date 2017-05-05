package dk.kb.simplecopapi;

import org.w3c.dom.Document;


import java.net.URL;
import java.net.URLConnection;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * Created by laap on 03-05-2017.
 */
public class URLReader {

    public Document getDocument(String url) throws Exception {

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        f.setValidating(false);
        DocumentBuilder b = null;
        b = f.newDocumentBuilder();
        URLConnection urlConnection = new URL(url).openConnection();
        urlConnection.addRequestProperty("Accept", "application/xml");
        return b.parse(urlConnection.getInputStream());
    }
}




