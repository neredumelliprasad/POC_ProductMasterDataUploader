package com.eunice.sap.hana.service;

import com.eunice.sap.hana.controller.*;
import com.eunice.sap.hana.mappers.RawDataProductMapper;
import com.eunice.sap.hana.model.MasterDataUpdateResponse;
import com.google.gson.*;
import com.sap.cloud.sdk.datamodel.odata.helper.ExpressionFluentHelper;
import com.sap.cloud.sdk.datamodel.odata.helper.batch.BatchResponse;
import com.sap.cloud.sdk.odatav2.connectivity.*;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.productmaster.*;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.productmaster.batch.ProductMasterServiceBatch;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultProductMasterService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.ProductMasterService;
import org.apache.commons.lang3.exception.*;
import org.slf4j.*;

import java.util.*;
import javax.transaction.Transactional;

public class ProductMasterDataService extends RawDataProductMapper implements HANAService
{
    private static final Logger LOG = LoggerFactory.getLogger(ProductMasterDataService.class);
    private ProductMasterService productMasterService = new DefaultProductMasterService();

    @Transactional
    public MasterDataUpdateResponse persistProducts(List<Product> products)
    {
        ProductMasterServiceBatch productMasterServiceBatch = productMasterService.batch();
        try
        {
            for (Product product : products)
            {
                productMasterServiceBatch = productMasterServiceBatch.beginChangeSet().createProduct(product).endChangeSet();
            }
            BatchResponse response = productMasterServiceBatch.execute(getErpContext());
            return new MasterDataUpdateResponse(products, response);
        }
        catch (ODataException e){
            LOG.error("Odata exception in executing batch:" + ExceptionUtils.getStackTrace(e));
            return new MasterDataUpdateResponse(e);
        }
        catch (Exception e){
            LOG.error("general exception in executing batch:" + ExceptionUtils.getStackTrace(e));
            return new MasterDataUpdateResponse(e);
        }
    }

    @Transactional
    public List<Product> getProducts(int limit)
    {
        try
        {

            ProductFluentHelper productFluentHelper = deepSelectProduct(productMasterService.getAllProduct()).top(limit);
            return productFluentHelper.execute(getErpContext());
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private ProductFluentHelper deepSelectProduct(ProductFluentHelper productFluentHelper){
       return productFluentHelper.select
           (Product.ALL_FIELDS,
               Product.TO_DESCRIPTION.select(ProductDescription.ALL_FIELDS),

               Product.TO_PLANT.select(ProductPlant.ALL_FIELDS,
                   ProductPlant.TO_STORAGE_LOCATION.select(ProductStorageLocation.ALL_FIELDS),
                   ProductPlant.TO_PLANT_SALES.select(ProductPlantSales.ALL_FIELDS),
                       ProductPlant.TO_PLANT_MRP_AREA.select(ProductPlantMRPArea.ALL_FIELDS),
                       ProductPlant.TO_PLANT_QUALITY_MGMT.select(ProductPlantQualityMgmt.ALL_FIELDS),
                       ProductPlant.TO_PLANT_STORAGE.select(ProductPlantStorage.ALL_FIELDS),
                       ProductPlant.TO_PLANT_TEXT.select(ProductPlantText.ALL_FIELDS),
                       ProductPlant.TO_PROD_PLANT_INTERNATIONAL_TRADE.select(ProductPlantIntlTrd.ALL_FIELDS),
                       ProductPlant.TO_PRODUCT_PLANT_COSTING.select(ProductPlantCosting.ALL_FIELDS),
                       ProductPlant.TO_PRODUCT_PLANT_FORECAST.select(ProductPlantForecasting.ALL_FIELDS),
                       ProductPlant.TO_PRODUCT_PLANT_PROCUREMENT.select(ProductPlantProcurement.ALL_FIELDS),
                       ProductPlant.TO_PRODUCT_SUPPLY_PLANNING.select(ProductSupplyPlanning.ALL_FIELDS),
                       ProductPlant.TO_PRODUCT_WORK_SCHEDULING.select(ProductWorkScheduling.ALL_FIELDS)
               ),

               Product.TO_PRODUCT_SALES.select(ProductSales.ALL_FIELDS),

               Product.TO_SALES_DELIVERY.select(ProductSalesDelivery.ALL_FIELDS,
                       //ProductSalesDelivery.TO_SALES_TAX.select(ProductSalesTax.ALL_FIELDS)
                       ProductSalesDelivery.TO_SALES_TEXT.select(ProductSalesText.ALL_FIELDS)
               ),

               Product.TO_PRODUCT_UNITS_OF_MEASURE.select(ProductUnitsOfMeasure.ALL_FIELDS,
                       ProductUnitsOfMeasure.TO_INTERNATIONAL_ARTICLE_NUMBER.select(ProductUnitsOfMeasureEAN.ALL_FIELDS)
               ),

               Product.TO_PRODUCT_STORAGE.select(ProductStorage.ALL_FIELDS),
                   Product.TO_PRODUCT_BASIC_TEXT.select(ProductBasicText.ALL_FIELDS),
                   Product.TO_PRODUCT_INSPECTION_TEXT.select(ProductInspectionText.ALL_FIELDS),
                   Product.TO_PRODUCT_PROCUREMENT.select(ProductProcurement.ALL_FIELDS),
                   Product.TO_PRODUCT_PURCHASE_TEXT.select(ProductPurchaseText.ALL_FIELDS),
                   Product.TO_PRODUCT_QUALITY_MGMT.select(ProductQualityMgmt.ALL_FIELDS),
                   Product.TO_PRODUCT_SALES_TAX.select(ProductSalesTax.ALL_FIELDS)
                   //Product.TO_VALUATION.select(ProductValuation.ALL_FIELDS)
           );
    }

    @Transactional
    public List<Product> getProductsByKey(List<String> productKeys)
    {
        try
        {
            return deepSelectProduct(productMasterService.
                getAllProduct()).
                filter(getFilterQuery(productKeys)).
                execute(getErpContext());
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private ExpressionFluentHelper getFilterQuery(List<String> productKeys){
        ExpressionFluentHelper<Product> filter = null;
        if (!productKeys.isEmpty()) {
            for (String eachKey:productKeys){
                filter = filter == null? Product.PRODUCT.eq(eachKey):filter.or(Product.PRODUCT.eq(eachKey));
            }
        }
        return filter;
    }


    @Override
    public String getDestinationName()
    {
        return "DFJ300_HTTPS_CLONING";
    }
}
