package dk.kb.simplecopapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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


    private String copURL = "http://www.kb.dk/cop/syndication";
    private String dsflURL = "http://www.kb.dk/cop/syndication/images/luftfo/2011/maj/luftfoto/subject203?format=kml";
    private String contentURL = "http://www.kb.dk/cop/content";
    private String navigationURL = "http://www.kb.dk/cop/navigation";

    @GET
    @ApiOperation(value = "Get the list of editions and retrieve the identifier of the edition, it is needed for any other call ",
            notes = "Only published editions",
            response = API.class,
            responseContainer = "List<Edition>")
    @Path("editions")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Edition> getEditions() {
        List<Edition> editions = new ArrayList<Edition>();

        try {
            URLReader reader = new URLReader();
            Document doc = reader.getDocument("http://cop.kb.dk/cop/editions/editions/any/2009/jul/editions/da");
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
    @ApiOperation(value = "Get a list of items from a specific edition ",
            response = API.class)
    @Path("cop/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getItemsInsideEditions(
            @ApiParam(value = "Edition id", name = "id", required = true) @QueryParam("id") String id,
            @ApiParam(value = "Search query", name = "query") @QueryParam("query") String query,
            @ApiParam(value = "Pagination-Page", name = "page") @QueryParam("page") String page,
            @ApiParam(value = "Pagination-Limit", name = "itemsPerPage") @QueryParam("itemsPerPage") String itemsPerPage,
            @ApiParam(value = "default is 1920-01-01, Do not return pictures before this date YYYY-MM-DD", name = "before") @QueryParam("before") String notBefore,
            @ApiParam(value = "default is 1970-12-31, Do not return pictures before this date YYYY-MM-DD", name = "after") @QueryParam("after") String notAfter)
            throws Exception {

        List<Edition> editions = new ArrayList<Edition>();

        String totalResults = "unknown";
        String startIndex = "unknown";


        URLReader reader = new URLReader();

        String url = copURL + id + "?format=kml";

        if (page != null) {
            url += "&page=" + page;
        }
        if (itemsPerPage != null) {
            url += "&itemsPerPage=" + itemsPerPage;
        }
        if (query != null) {
            url += "&query=" + query;
        }
        if (notBefore != null) {
            url += "&notBefore=" + notBefore;
        }
        if (notAfter != null) {
            url += "&notAfter=" + notAfter;
        }

        Document xmlDocument = reader.getDocument(url);
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        NodeList placemarkList = (NodeList) xPath.compile("//Placemark").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList nameList = (NodeList) xPath.compile("//Placemark/name").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList thumbnailList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectThumbnailSrc']").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList linkList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectLink']").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList imageURIList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectImageSrc']").evaluate(xmlDocument, XPathConstants.NODESET);


        totalResults = (String) xPath.evaluate("//totalResults", xmlDocument);
        itemsPerPage = (String) xPath.evaluate("//itemsPerPage", xmlDocument);
        startIndex = (String) xPath.evaluate("//startIndex", xmlDocument);

        for (int i = 0; i < placemarkList.getLength(); i++) {

            Edition edition = new Edition();
            edition.setThumbnailURI(thumbnailList.item(i).getTextContent());
            edition.setLink(linkList.item(i).getTextContent());
            edition.setTitle(nameList.item(i).getTextContent());
            edition.setImageURI(imageURIList.item(i).getTextContent());
            editions.add(edition);
        }

        int total = Integer.parseInt(totalResults)/Integer.parseInt(itemsPerPage);
        return Response.status(200).
                entity(editions).
                header("Total", totalResults).
                header("Pagination-Count", total).
                header("Pagination-Page", startIndex).
                header("Pagination-Limit", itemsPerPage).build();
    }


    @GET
    @ApiOperation(value = "Get the content of an object ",
            response = API.class)
    @Path("content/")
    @Produces(MediaType.APPLICATION_XML)
    public Response getContent(
            @ApiParam(value = "Edition id", name = "identifier", required = true) @QueryParam("identifier") String identifier,
            @ApiParam(value = "Object id", name = "objectId", required = true) @QueryParam("objectId") String objectId)
            throws Exception {

        URLReader reader = new URLReader();
        String url = contentURL  + identifier +  objectId;
        Document xmlDocument = reader.getDocument(url);
        return Response.status(200).entity(xmlDocument).build();
    }

    @GET
    @ApiOperation(value = "The subject hierarchy needed for filtering and building the browsing service",
            response = API.class)
    @Path("navigation/")
    @Produces(MediaType.APPLICATION_XML)
    public Response getSubjectHierarchy(
            @ApiParam(value = "Edition id", name = "identifier", required = true) @QueryParam("identifier") String identifier,
            @ApiParam(value = "Edition id", name = "subjectId", required = true) @QueryParam("subjectId") String subjectId)
            throws Exception {

        URLReader reader = new URLReader();
        String url = navigationURL + identifier + subjectId;

        System.out.println(url);
        Document xmlDocument = reader.getDocument(url);
        System.out.println(url);
        return Response.status(200).entity(xmlDocument).build();
    }

    @GET
    @Path("dsfl/")
    @ApiOperation(value = "Get a list of pictures (url, coordinates and descriptions) inside a bounding box")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDSFLPhotos(
            @ApiParam(value = "The bounding box", name = "bbo", required = true) @QueryParam("bbo") String bbo,
            @ApiParam(value = "Pagination-Page", name = "page") @QueryParam("page") String page,
            @ApiParam(value = "Pagination-Limit", name = "itemsPerPage") @QueryParam("itemsPerPage") String itemsPerPage,
            @ApiParam(value = "default is 1920-01-01, Do not return pictures before this date YYYY-MM-DD", name = "notBefore") @QueryParam("notBefore") String notBefore,
            @ApiParam(value = "default is 1970-12-31, Do not return pictures before this date YYYY-MM-DD", name = "notAfter") @QueryParam("notAfter") String notAfter)
            throws Exception {

        Pictures pictures = new Pictures();
        pictures.setType("FeatureCollection");
        String totalResults = "unknown";
        String startIndex = "unknown";
        List<Picture> pictureList = new ArrayList<Picture>();
        URLReader reader = new URLReader();

        if (bbo == null) throw new NotAuthorizedException("BBO is missing");
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
        if (itemsPerPage != null) {
            url += "&itemsPerPage=" + itemsPerPage;
        }

        Document xmlDocument = reader.getDocument(url);
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        NodeList placemarkList = (NodeList) xPath.compile("//Placemark").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList nameList = (NodeList) xPath.compile("//Placemark/name").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList coordList = (NodeList) xPath.compile("//Placemark/Point/coordinates").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList thumbnailList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectThumbnailSrc']").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList srcList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectImageSrc']").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList genreList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectGenre']").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList subjectCreationDateList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectCreationDate']").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList geographicList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='subjectGeographic']").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList recordCreationDateList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='recordCreationDate']").evaluate(xmlDocument, XPathConstants.NODESET);
        NodeList recordChangeDateList = (NodeList) xPath.compile("//Placemark/ExtendedData/Data[@name='recordCreationDate']").evaluate(xmlDocument, XPathConstants.NODESET);

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
            properties.setSrc(srcList.item(i).getTextContent());
            properties.setGenre(genreList.item(i).getTextContent());
            properties.setGeographic(geographicList.item(i).getTextContent());
            properties.setSubjectCreationDate(subjectCreationDateList.item(i).getTextContent());
            properties.setRecordCreationDate(recordCreationDateList.item(i).getTextContent());
            properties.setRecordChangeDate(recordChangeDateList.item(i).getTextContent());

            picture.setProperties(properties);

            picture.setType("Feature");

            pictureList.add(picture);
        }

        pictures.setFeatures(pictureList);

        return Response.status(200).
                entity(pictureList).
                header("Pagination-Count", totalResults).
                header("Pagination-Page", startIndex).
                header("Pagination-Limit", itemsPerPage).build();
    }
}