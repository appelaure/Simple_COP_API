package dk.kb.simplecopapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

//http://localhost:8080/swagger/

@Path("api")
@Api
public class API {

    private String copURL = "http://cop.kb.dk/cop/editions";
    private String dsflURL = "http://www.kb.dk/cop/syndication/images/luftfo/2011/maj/luftfoto/subject203?format=kml";

    @GET
    @ApiOperation(value = "Get the list of editions ",
            notes = "Only published editions",
            response = API.class,
            responseContainer = "List<Edition>")
    @Path("editions")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Edition> getEditions() {
        List<Edition> editions = new ArrayList<Edition>();

        try {
            URLReader reader = new URLReader();
            Document doc = reader.getDocument(copURL + "/editions/any/2009/jul/editions/da");
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
    @ApiOperation(value = "Get a list of pictures (url, coordinates and descriptions) inside a bounding box",
            notes = "To know hoe many images are available inside this bounding box use the function ")
    @HeaderParam(value="Pagination-Count")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPhotosInsideBBO(@ApiParam(value = "The bounding box", name = "bbo", required = true) @QueryParam("bbo") String bbo,
                                       @ApiParam(value = "Pagination-Page", name = "page") @QueryParam("page") String page,
                                       @ApiParam(value = "Pagination-Limit", name = "limit") @QueryParam("limit") String limit,
                                       @ApiParam(value = "default is 1920-01-01, Do not return pictures before this date YYYY-MM-DD", name = "notBefore") @QueryParam("notBefore") String notBefore,
                                       @ApiParam(value = "default is 1970-12-31, Do not return pictures before this date YYYY-MM-DD", name = "notAfter") @QueryParam("notAfter") String notAfter) {


        Pictures pictures = new Pictures();
        pictures.setType("FeatureCollection");
        String totalResults = "unknown";
        String startIndex = "unknown";
        String itemsPerPage = "unknown";
        List<Picture> pictureList = new ArrayList<Picture>();
        URLReader reader = new URLReader();

        String url = dsflURL + "&bbo=" + bbo;
        if (notBefore != null) {
            url += "&notBefore" + notBefore;
        }
        if (notAfter != null) {
            url += "&notAfter" + notAfter;
        }
        if (page != null) {
            url += "&page=" + page;
        }
        if (limit != null) {
            url += "&itemsPerPage=" + limit;
        }

        try {
            Document xmlDocument = reader.getDocument(url);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();

            NodeList placemarkList = (NodeList) xPath.compile("//Placemark").evaluate(xmlDocument, XPathConstants.NODESET);
            NodeList nameList = (NodeList) xPath.compile("//Placemark/name").evaluate(xmlDocument, XPathConstants.NODESET);
            NodeList coordList = (NodeList) xPath.compile("//Placemark/Point/coordinates").evaluate(xmlDocument, XPathConstants.NODESET);
            NodeList thumbnailList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectThumbnailSrc']").evaluate(xmlDocument, XPathConstants.NODESET);
            totalResults = (String) xPath.evaluate("//totalResults", xmlDocument);
            itemsPerPage = (String) xPath.evaluate("//itemsPerPage", xmlDocument);
            startIndex = (String) xPath.evaluate("//startIndex", xmlDocument);

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

        return Response.status(200).
                entity(pictureList).
                header("Pagination-Count", totalResults).
                header("Pagination-Page", startIndex).
                header("Pagination-Limit", itemsPerPage).build();
    }
}