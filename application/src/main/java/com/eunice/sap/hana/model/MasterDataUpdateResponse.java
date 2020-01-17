package com.eunice.sap.hana.model;
/**
 * Created by roychoud on 18 Dec 2019.
 */

import com.google.gson.Gson;
import com.sap.cloud.sdk.datamodel.odata.helper.*;
import com.sap.cloud.sdk.datamodel.odata.helper.batch.BatchResponse;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.productmaster.Product;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * History:
 * <ul>
 * <li> 18 Dec 2019 : roychoud - Created
 * </ul>
 *
 * @authors roychoud : Arunava Roy Choudhury
 */
public class MasterDataUpdateResponse
{
    private static final Logger LOG = LoggerFactory.getLogger(MasterDataUpdateResponse.class);
    private List<Map<String, Object>>  successes= new ArrayList<>();
    private List<Map<String, Object>>  failures = new ArrayList<>();
    public List<Map<String, Object>> getSuccesses()
    {
        return successes;
    }
    public List<Map<String, Object>> getFailures()
    {
        return failures;
    }

    public MasterDataUpdateResponse(final List<Product> products, final BatchResponse batchResponse) {

        for (int i = 0; i < products.size(); i++) {
            Map<String, Object> responseMap = new HashMap<>();
            if (batchResponse.get(i).isSuccess()) {
                List<Product> createdEntities = batchResponse.get(i).get().getCreatedEntities().stream()
                        .map(entity -> (Product) entity).collect(Collectors.toList());
                responseMap.put("product", createdEntities.get(0).getProduct());
                successes.add(responseMap);
            } else {
                responseMap.put("product", products.get(i).getProduct());
                String errorMessage = extractSAPResponse(batchResponse.get(i).getCause().getMessage());
                responseMap.put("errorMessage", errorMessage);
                failures.add(responseMap);
            }
        }
    }
    public MasterDataUpdateResponse(Exception ex){
        LOG.error("UNCAUGHT Exception :" + ExceptionUtils.getStackTrace(ex));
        createFailedMessage(ex);
    }

    public String extractSAPResponse(String str){

        String responseBody = str.substring(str.indexOf("<?xml")).trim();
        LOG.info("Start extracting the error message. Exception:"+ responseBody);
        StringBuilder stringBuilder = new StringBuilder();
        try{
            DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = newDocumentBuilder.parse(new ByteArrayInputStream(responseBody.getBytes()));
            NodeList errorDetails = document.getElementsByTagName("errordetail");
            for (int i=0;i<errorDetails.getLength();i++){
                Node messageNode = getNodeByName(errorDetails.item(i),"message");
                if(messageNode != null) {
                    stringBuilder.append(messageNode.getTextContent()).append(". ### ");
                }
            }
            LOG.info("Error message extracted. " + stringBuilder.toString());
        }
        catch (Exception ex){
            LOG.error("Exception occured in error message extraction: " + ExceptionUtils.getStackTrace(ex));
            createFailedMessage(ex);
        }
        return stringBuilder.toString();
    }

    private void createFailedMessage(Exception ex)
    {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("errorMessage", ex instanceof ODataException ? ex.getMessage() : ExceptionUtils.getStackTrace(ex));
        failures.add(responseMap);
    }
    private Node getNodeByName(Node root,String nodeName){
        NodeList nodeList = root.getChildNodes();
        for (int i=0;i<nodeList.getLength();i++){
            if (nodeList.item(i).getNodeName().equalsIgnoreCase(nodeName)){
                return nodeList.item(i);
            }
        }
        return null;
    }
}
