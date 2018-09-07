package hoohoot.synapse.adapter.conf;

public class Routes {

    private Routes() { }

    public static final String MXISD_LOGIN = "/_mxisd/backend/api/v1/auth/login";
    public static final String MXISD_USER_SEARCH = "/_mxisd/backend/api/v1/directory/user/search";
    public static final String MXISD_SINGLEPID_SEARCH = "/_mxisd/backend/api/v1/identity/single";
    public static final String MXISD_BULKPID_SEARCH = "/_mxisd/backend/api/v1/identity/bulk";
}