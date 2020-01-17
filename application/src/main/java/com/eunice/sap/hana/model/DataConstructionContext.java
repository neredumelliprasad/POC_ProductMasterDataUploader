package com.eunice.sap.hana.model;
/**
 * Created by roychoud on 18 Dec 2019.
 */

import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.productmaster.Product;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.productmaster.ProductPlant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * History:
 * <ul>
 * <li> 18 Dec 2019 : roychoud - Created
 * </ul>
 *
 * @authors roychoud : Arunava Roy Choudhury
 */
public class DataConstructionContext
{
    private Map<String, Product> productMap;
    private Map<String, ProductPlant> productPlantMap;
    private String localDateFormat;

    public DataConstructionContext(String localDateFormat){
        this.localDateFormat = localDateFormat;
        productMap = new ConcurrentHashMap<>();
        productPlantMap = new ConcurrentHashMap<>();
    }


    public Map<String,ProductPlant> getProductPlantMap(){return productPlantMap;}
    public Map<String, Product> getProductMap()
    {
        return productMap;
    }
    public String getLocalDateFormat()
    {
        return localDateFormat;
    }
}
