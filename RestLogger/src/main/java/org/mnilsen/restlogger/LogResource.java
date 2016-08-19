package org.mnilsen.restlogger;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import java.io.StringReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.Consumes;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.bson.Document;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.json.JSONObject;
import org.json.JSONString;

import org.mnilsen.restlogger.jaxb.LogMessage;
import org.mnilsen.restlogger.jaxb.MessageList;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("log")
public class LogResource {

    private MongoClient client = null;
    private MongoDatabase database = null;
    private MongoCollection collection = null;
    JAXBContext ctx = null;

    @PostConstruct
    public void postConstruct() {
        //System.setProperty("javax.xml.bind.context.factory","org.eclipse.persistence.jaxb.JAXBContextFactory");
        this.client = new MongoClient("10.0.0.39");
        this.database = this.client.getDatabase("rest_logger");
        this.collection = this.database.getCollection("messages");
        Class[] classes = new Class[]{LogMessage.class, MessageList.class};
        try {
            ctx = JAXBContextFactory.createContext(classes, new HashMap());
        } catch (JAXBException ex) {
            Logger.getLogger(LogResource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @GET
    @Path("/tail/{count}")
    @Produces("application/json")
    public String tail(@PathParam("count") int count) {
        StringBuilder buff = new StringBuilder();
        buff.append("[");
        MongoCursor<Document> mc = this.collection.find().sort(new Document("timestamp", -1)).limit(count).iterator();
        int limit = 0;
        while (mc.hasNext()) {
            limit++;
            if (limit > count) {
                break;
            }
            Document d = mc.next();
            buff.append(d.toJson());
            buff.append(",");

        }
        buff.append("]");
        return buff.toString();
    }

    private LogMessage convert(Document d) {
        LogMessage lm = null;
        if (this.ctx == null) {
            return null;
        }
        try {
            // Create the Marshaller Object using the JaxB Context
            Unmarshaller um = ctx.createUnmarshaller();

            // Set the Unmarshaller media type to JSON or XML
            um.setProperty(UnmarshallerProperties.MEDIA_TYPE,
                    "application/json");
            um.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);

            System.out.println(String.format("Generated JSON: %s", d.toJson()));
            StreamSource json = new StreamSource(
                    new StringReader(d.toJson()));
            lm = um.unmarshal(json, LogMessage.class).getValue();
        } catch (JAXBException ex) {
            Logger.getLogger(LogResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lm;
    }

    @PUT
    @Path("/write")
    @Consumes("application/json")
    public Response writeMessage(String json) {
        try {
            Logger.getLogger(LogResource.class.getName()).info(String.format("Inserting '%s'", json));
            BasicDBObject bobj = (BasicDBObject) JSON.parse(json);
            Document d = new Document(bobj);
            this.collection.insertOne(d);
        } catch (Exception e) {
            Logger.getLogger(LogResource.class.getName()).log(Level.SEVERE, "Mongo insert failed", e);
            return Response.serverError().status(500).build();
        }
        return Response.ok().build();
    }

}
