@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardService {

    private static final Logger logger = LogManager.getLogger(DashboardService.class);

    @jakarta.inject.Inject
    private DashboardBean dashboardBean;

    @GET
    @Path("/summary")
    public Response getDashboardSummary() {
        UserEntity currentUser = RequestContext.getCurrentUser();

        logger.info("User: {} | IP: {} - Requested dashboard summary.", 
            currentUser.getEmail(), RequestContext.getIp());

        DashboardSummaryDto summary = dashboardBean.getSummaryForUser(currentUser);

        return Response.ok(summary).build();
    }
}
