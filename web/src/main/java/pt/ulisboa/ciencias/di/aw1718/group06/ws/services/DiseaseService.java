package pt.ulisboa.ciencias.di.aw1718.group06.ws.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;

@Path("/disease")
public class DiseaseService {

    private static final Logger LOG = LoggerFactory.getLogger(DiseaseService.class);
    private static final String CONFIG_FILE_NAME = "config.properties";
    private DiseaseCatalog diseaseCatalog;


    public DiseaseService() {
        try {
            diseaseCatalog = new DiseaseCatalog(CONFIG_FILE_NAME);
        } catch (SQLException e) {
            LOG.error("Error while connecting to database: " + e.getErrorCode(), e);
        }
    }

    @GET
    @Path("/get_all/{limit}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Disease> getAllDiseases(@PathParam("limit") int limit) throws SQLException {
        return diseaseCatalog.getDiseases(limit);
    }

    @GET
    @Path("/get_by_name_fragment/{fragment}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_XML})
    public List<Disease> getDiseasesByNameFragment(@PathParam("fragment") String fragment) throws SQLException {
        return diseaseCatalog.getFragmentDiseases(fragment);
    }

    @GET
    @Path("/get_disease/{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object getDiseaseDetail(@PathParam("name") String name) {
        return null;
    }

}
