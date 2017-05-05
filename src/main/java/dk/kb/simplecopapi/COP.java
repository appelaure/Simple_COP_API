package dk.kb.simplecopapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

@Path("cop")
@Api
public class COP {

    private String baseURL = "http://cop.kb.dk/cop/editions";
    private String dsflURL = "http://www.kb.dk/cop/syndication/images/luftfo/2011/maj/luftfoto/subject203?format=kml&itemsPerPage=500";

    @GET
    @ApiOperation(value = "Get the list of editions ",
            notes = "Only published editions",
            response = COP.class,
            responseContainer = "List<Edition>")
    @Path("editions")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Edition> getEditions() {
        List<Edition> editions = new ArrayList<Edition>();

        try {
            URLReader reader = new URLReader();
            Document doc = reader.getDocument(baseURL + "/editions/any/2009/jul/editions/da");
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("item");

            for (int i = 0; i < nodeList.getLength(); i++) {

                Node nNode = nodeList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Edition edition = new Edition();
                    Element eElement = (Element) nNode;
                    edition.setTitle(eElement.getElementsByTagName("title").item(0).getTextContent());
                    edition.setLink(eElement.getElementsByTagName("link").item(0).getTextContent());
                    edition.setDescription(eElement.getElementsByTagName("description").item(0).getTextContent());

                    //Maybe not the best way to parse?
                    NodeList mods = eElement.getElementsByTagName("identifier");
                    edition.setIdentifier(mods.item(0).getTextContent());
                    edition.setImageURI(mods.item(1).getTextContent());
                    edition.setThumbnailURI(mods.item(2).getTextContent());

                    editions.add(edition);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return editions;
    }

    @GET
    @Path("dsfl")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Pictures getPhotosInsideBBO(@QueryParam("bbo") String bbo) {

        Pictures pictures = new Pictures();
        pictures.setType("FeatureCollection");

        List<Picture> pictureList = new ArrayList<Picture>();
        URLReader reader = new URLReader();


        try {
            Document xmlDocument = reader.getDocument(dsflURL + "&bbo=" + bbo);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();

            NodeList placemarkList = (NodeList) xPath.compile("//Placemark").evaluate(xmlDocument, XPathConstants.NODESET);
            NodeList nameList = (NodeList) xPath.compile("//Placemark/name").evaluate(xmlDocument, XPathConstants.NODESET);
            NodeList coordList = (NodeList) xPath.compile("//Placemark/Point/coordinates").evaluate(xmlDocument, XPathConstants.NODESET);
            NodeList thumbnailList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectThumbnailSrc']").evaluate(xmlDocument, XPathConstants.NODESET);

            for (int i = 0; i < placemarkList.getLength(); i++) {

                Picture picture = new Picture();

                Geometry geometry = new Geometry();
                geometry.setType("Point");
                List<Float> list = new ArrayList<Float>();
                String[] s = coordList.item(i).getTextContent().split(",");
                list.add(Float.parseFloat(s[0]));
                list.add(Float.parseFloat(s[1]));
                geometry.setCoordinates(list);

                picture.setGeometry(geometry);

                Properties properties = new Properties();
                properties.setName(nameList.item(i).getTextContent());
                properties.setThumbnail(thumbnailList.item(i).getTextContent());
                picture.setProperties(properties);

                picture.setType("Feature");

                pictureList.add(picture);

            }

            pictures.setFeatures(pictureList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pictures;
    }


}