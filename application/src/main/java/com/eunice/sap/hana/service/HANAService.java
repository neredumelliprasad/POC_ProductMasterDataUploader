package com.eunice.sap.hana.service;
/**
 * Created by roychoud on 08 Jan 2020.
 */

import com.eunice.sap.hana.model.ValidationResult;
import com.eunice.sap.hana.model.validation.Validable;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.s4hana.connectivity.DefaultErpHttpDestination;
import com.sap.cloud.sdk.s4hana.connectivity.ErpHttpDestination;
import java.util.ArrayList;
import java.util.List;

/**
 * History:
 * <ul>
 * <li> 08 Jan 2020 : roychoud - Created
 * </ul>
 *
 * @authors roychoud : Arunava Roy Choudhury
 */
public interface HANAService
{
    String getDestinationName();

     default ErpHttpDestination getErpContext(){
        return DestinationAccessor.getDestination(getDestinationName()).asHttp()
            .decorate(DefaultErpHttpDestination::new);
    }

    default List<ValidationResult> validateOdataObject(List<? extends Validable> validables){
        List<ValidationResult> validationResults =new ArrayList<>();
        for (int i=0;i<validables.size();i++){
            Validable validable = validables.get(i);
            Integer index = validable.getIndex();
            validationResults.addAll(validable.validate(index));
        }
        return validationResults;
    }
}
