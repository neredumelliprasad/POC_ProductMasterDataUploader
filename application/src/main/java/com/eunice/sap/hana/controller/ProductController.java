package com.eunice.sap.hana.controller;
/**
 * Created by roychoud on 08 Jan 2020.
 */

import com.eunice.sap.hana.model.DataConstructionContext;
import com.eunice.sap.hana.model.MasterDataUpdateResponse;
import com.eunice.sap.hana.model.RawProductData;
import com.eunice.sap.hana.model.ValidationResult;
import com.eunice.sap.hana.service.ProductMasterDataService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.productmaster.Product;
import org.slf4j.*;
import org.springframework.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * History:
 * <ul>
 * <li> 08 Jan 2020 : roychoud - Created
 * </ul>
 *
 * @authors roychoud : Arunava Roy Choudhury
 */

@Path("/masterdata/product")
public class ProductController
{

    private ProductMasterDataService productMasterDataService = new ProductMasterDataService();
    private static final Gson gson = new GsonBuilder().create();
    private static final int CREATED = 201;
    private static final int MULTI_STATUS = 207;
    private static final String mediaType = "application/json; charset=UTF-8";
    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

    @GET
    @Path("/")
    @Produces(mediaType)
    public void  getCSRF(){}


    @PUT
    @Path("/")
    @Produces(mediaType)
    @Consumes(mediaType)
    public Response addProducts(String reqDataString) throws IOException
    {
        List<RawProductData> rawProductData = gson.fromJson(reqDataString,new TypeToken<ArrayList<RawProductData>>(){}.getType());
        if(!CollectionUtils.isEmpty(rawProductData))
        {
            List<ValidationResult> validationResults = productMasterDataService.validateOdataObject(rawProductData);
            if (validationResults.isEmpty())
            {
                String datePattern = "YYYY/MM/DD";
                DataConstructionContext constructionContext = new DataConstructionContext(datePattern);
                LOG.info("-------- Input RawProductData is ---------: " + rawProductData);
                List<Product> products = productMasterDataService.createEntities(rawProductData, constructionContext);
                LOG.info("-------- Persisting the product ---------: " + products);
                MasterDataUpdateResponse response = productMasterDataService.persistProducts(products);
                int status = response.getFailures().isEmpty()? CREATED: MULTI_STATUS;
                LOG.info("Status code:" + status);
                return Response.status(status).entity(gson.toJson(response)).type(MediaType.APPLICATION_JSON).build();
            }
            else{
                return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(validationResults)).type(MediaType.APPLICATION_JSON).build();
            }
        }else{
            return Response.status(Response.Status.BAD_REQUEST).entity("Did you forgot to send request data?").build();
        }
    }

    @GET
    @Path("/getProducts")
    @Produces(mediaType)
    public Response getProducts(@QueryParam("limit") Integer limit){
         List<Product> products =productMasterDataService.getProducts(limit);
         return Response.ok(gson.toJson(products)).build();
    }

    @GET
    @Path("/getByKey")
    @Produces(mediaType)
    @Consumes(mediaType)
    public Response getProducts(@QueryParam("keys") String productKey){
        List<String> keys = Arrays.asList(productKey.split(","));
        List<Product> products =productMasterDataService.getProductsByKey(keys);
        if (products.isEmpty()){
            return Response.noContent().build();
        }
        else{
            return Response.ok(gson.toJson(products.get(0))).build();
        }
    }
}
