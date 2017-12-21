package es.ihm.ismadani.calendar.model;


import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbAccessException;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.android.http.AndroidHttpClient;


/**
 * This class encapsulates the access to the CouchDB server and manages a single
 * connection to the database (singleton pattern). 
 */

public class CouchDBAndroidHelper {

    private static final String DATABASE_NAME = "calendario";    


    /**
     * CouchDB is represented by two main interfaces in Ektorp:
     * - org.ektorp.CouchDbInstance is the handle for the actual CouchDB instance 
     *   you are connecting to.
     * - org.ektorp.CouchDbConnector is a connection to a specific database residing
     *   on the instance above.
     *  So, in order to connect to a database in a CouchDB instance, you will need 
     *  a CouchDbConnector, which needs a CouchDbInstance, which in turn needs a HttpClient.
     * These classes are thread-safe so they can be shared across threads.
     */

    private static CouchDBAndroidHelper singleInstance = null;
    private static CouchDbConnector dbConnector = null;
    
    
    public static CouchDBAndroidHelper getInstance() {
        if (singleInstance == null) {
            singleInstance = new CouchDBAndroidHelper();            
        }
        return singleInstance;
    }
    
    private CouchDBAndroidHelper() {
        try {
        	HttpClient httpClient = new AndroidHttpClient.Builder()
            .host("filosente.cloudant.com")
            .port(443)
            .username("filosente")
            .password("Daniesgayperoseleaprecia")
            .enableSSL(true)
            .relaxedSSLSettings(true)
            .build();

            CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
            dbConnector = new StdCouchDbConnector(DATABASE_NAME, dbInstance);       
        } catch (Exception e) {
            throw new DbAccessException(e);
        }
    }
    
    public CouchDbConnector getDbConnector() {
        return dbConnector;
    }
    
}

/**/
