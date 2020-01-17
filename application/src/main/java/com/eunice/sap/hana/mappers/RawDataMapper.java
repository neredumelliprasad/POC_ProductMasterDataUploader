package com.eunice.sap.hana.mappers;
/**
 * Created by roychoud on 18 Dec 2019.
 */

import com.eunice.sap.hana.model.DataConstructionContext;
import com.eunice.sap.hana.model.RawProductData;
import java.util.List;

/**
 * History:
 * <ul>
 * <li> 18 Dec 2019 : roychoud - Created
 * </ul>
 *
 * @authors roychoud : Arunava Roy Choudhury
 */
public interface RawDataMapper<T extends Object>
{

    List<T> createEntities(List<RawProductData> rawData, DataConstructionContext constructionContext);
}
