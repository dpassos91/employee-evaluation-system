package aor.projetofinal.service;


import aor.projetofinal.bean.EvaluationBean;
import aor.projetofinal.dto.EvaluationOptionsDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


@Path("/evaluations")
public class EvaluationService {

    private static final Logger logger = LogManager.getLogger(ProfileService.class);

    @Inject
    private EvaluationBean evaluationBean;

    @GET
    @Path("/list-evaluation-options")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listEvaluationOptions() {




        List <EvaluationOptionsDto> options= evaluationBean.listEvaluationOptions();
        return Response.ok(options).build();
    }

}
