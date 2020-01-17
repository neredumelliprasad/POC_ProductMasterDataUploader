package com.eunice.sap.hana.configuration;
/**
 * Created by roychoud on 08 Jan 2020.
 */

import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultProductMasterService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.ProductMasterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * History:
 * <ul>
 * <li> 08 Jan 2020 : roychoud - Created
 * </ul>
 *
 * @authors roychoud : Arunava Roy Choudhury
 */
@Configuration
public class ServiceConfiguration
{

    @Bean
    public ProductMasterService getProductMasterService(){
        return new DefaultProductMasterService();
    }
}
