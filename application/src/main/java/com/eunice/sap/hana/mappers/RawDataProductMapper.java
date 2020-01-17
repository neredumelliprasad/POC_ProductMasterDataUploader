package com.eunice.sap.hana.mappers;
/**
 * Created by roychoud on 18 Dec 2019.
 */

import com.eunice.sap.hana.model.DataConstructionContext;
import com.eunice.sap.hana.model.RawProductData;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.productmaster.*;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * History:
 * <ul>
 * <li> 18 Dec 2019 : roychoud - Created
 * </ul>
 *
 * @authors roychoud : Arunava Roy Choudhury
 */
public class RawDataProductMapper implements RawDataMapper<Product>
{
    ProductUnitsOfMeasure createUnitOfMeasure(RawProductData rawProductData, DataConstructionContext constructionContext){
        ProductUnitsOfMeasure productUnitsOfMeasure = new ProductUnitsOfMeasure();
        productUnitsOfMeasure.setProduct(rawProductData.getMaterial());
        productUnitsOfMeasure.setAlternativeUnit(rawProductData.getAun());
        productUnitsOfMeasure.setWeightUnit(rawProductData.getWeightUnit());
        productUnitsOfMeasure.setQuantityDenominator(new BigDecimal(rawProductData.getQunatityDenominator()));
        productUnitsOfMeasure.setQuantityNumerator(new BigDecimal(rawProductData.getQunatityNumerator()));
        return productUnitsOfMeasure;
    }

    private ProductStorageLocation createProductStorageLocation(RawProductData rawProductData,DataConstructionContext constructionContext) {
        ProductStorageLocation productStorageLocation = new ProductStorageLocation();
        productStorageLocation.setProduct(rawProductData.getMaterial());
        productStorageLocation.setPlant(rawProductData.getPlant());
        productStorageLocation.setStorageLocation(rawProductData.getStoreLocation());
        return productStorageLocation;
    }

    ProductPlant createProductPlant(RawProductData rawProductData,DataConstructionContext constructionContext){
        String key = rawProductData.getMaterial()+"_"+rawProductData.getPlant();
        ProductPlant productPlant = constructionContext.getProductPlantMap().containsKey(key)?constructionContext.getProductPlantMap().get(key):new ProductPlant();
        constructionContext.getProductPlantMap().put(key,productPlant);
        productPlant.setProduct(rawProductData.getMaterial());
        productPlant.setPlant(rawProductData.getPlant());
        productPlant.setAvailabilityCheckType(rawProductData.getAvailabilityCheck());
        boolean isBatchManagementRequired = rawProductData.getBatchManagementPlant().equalsIgnoreCase("x")? true:false;
        productPlant.setIsBatchManagementRequired(isBatchManagementRequired);
        productPlant.setProfitCenter(rawProductData.getProfitCenter());
        productPlant.setMRPType(rawProductData.getMrpType());
        productPlant.setSerialNumberProfile(rawProductData.getSerialNoProfile());
        productPlant.setCountryOfOrigin(rawProductData.getCountryOfOrigin());
        productPlant.setPlantSales(createProductPlantSales(rawProductData,constructionContext));
        if(!StringUtils.isEmpty(rawProductData.getPlantSpecificMaterialStatus())
                && !rawProductData.getPlantSpecificMaterialStatus().equals("undefined")) {
            productPlant.setProfileCode(rawProductData.getPlantSpecificMaterialStatus());
        }
        productPlant.setMRPResponsible(rawProductData.getMrpController());
        productPlant.setGoodsReceiptDuration(new BigDecimal(rawProductData.getGoodsReceiptProcessingTime()));
        productPlant.setProcurementType(rawProductData.getProcurementType());
        ProductSupplyPlanning productSupplyPlanning = new ProductSupplyPlanning();
        productSupplyPlanning.setProduct(rawProductData.getMaterial());
        productSupplyPlanning.setTotalReplenishmentLeadTime(new BigDecimal(rawProductData.getInhouseProductionTime()));
        productSupplyPlanning.setPlanningTimeFence(rawProductData.getPlanningTimeFence());
        productSupplyPlanning.setPlanningStrategyGroup(rawProductData.getStrategyGroup());
        productSupplyPlanning.setLotSizingProcedure(rawProductData.getLotSizingProcedure());
        productSupplyPlanning.setProcurementSubType(rawProductData.getSpecialProcurement());
        productPlant.setProductSupplyPlanning(productSupplyPlanning);
        if(!StringUtils.isEmpty(rawProductData.getValidFromPlantMaterial())
                && !rawProductData.getValidFromPlantMaterial().equals("undefined")) {
            LocalDateTime now = LocalDateTime.now();
            productPlant.setProfileValidityStartDate(now);
        }
        //productPlant.setProfileValidityStartDate(LocalDateTime.parse(rawProductData.getValidFromPlantMaterial() //TODO need to check the error
        //    , DateTimeFormatter.ofPattern(constructionContext.getLocalDateFormat())));
        //addProductPlantMRPArea(productPlant, rawProductData, constructionContext);
        addStorageLocation(productPlant,rawProductData,constructionContext);  //commented to test for client

        return productPlant;
    }

    ProductStorage createProductStorage(RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductStorage productStorage = new ProductStorage();
        productStorage.setProduct(rawProductData.getMaterial());
        productStorage.setMinRemainingShelfLife(new BigDecimal(rawProductData.getMinimumRemainingShelfLife()));
        //productStorage.setExpirationDate(rawProductData.getExpirationDate()); //TODO find the correct mapping
        return productStorage;
    }

    ProductSalesDelivery createProductSalesDelivery(RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductSalesDelivery productSalesDelivery = new ProductSalesDelivery();
        productSalesDelivery.setProduct(rawProductData.getMaterial());
        productSalesDelivery.setProductSalesOrg(rawProductData.getSalesOrg());
        productSalesDelivery.setProductDistributionChnl(rawProductData.getDistributionChannel());
        productSalesDelivery.setProductHierarchy(rawProductData.getProductHierarchy());
        productSalesDelivery.setSalesMeasureUnit(rawProductData.getSalesUnit());
        if(!StringUtils.isEmpty(rawProductData.getDeliveryPlantSpecStatus())
                && !rawProductData.getDeliveryPlantSpecStatus().equals("undefined")) {
            productSalesDelivery.setProductSalesStatus(rawProductData.getDeliveryPlantSpecStatus());
        }
        productSalesDelivery.setItemCategoryGroup(rawProductData.getItemCategoryGroup());
        //LocalDateTime now = LocalDateTime.now();
        //productSalesDelivery.setProductSalesStatusValidityDate(now);
        //productSalesDelivery.setThirdSalesSpecProductGroup(rawProductData.getMaterialGroup());
        //addProductSalesTax(productSalesDelivery, rawProductData, constructionContext);
        //addProductSalesText(productSalesDelivery, rawProductData, constructionContext);
        return productSalesDelivery;
    }

    private void addProductSalesTax(Product product,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductSalesTax productSalesTax = createProductSalesTax(rawProductData,constructionContext);
        Set<ProductSalesTax> existingOnes = new HashSet<>(product.getProductSalesTaxIfPresent().getOrElse(new ArrayList<>()));
        if (!existingOnes.contains(productSalesTax)){
            product.addProductSalesTax(productSalesTax);
        }
    }

    private void addProductSalesTax(ProductSalesDelivery productSalesDelivery,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductSalesTax productSalesTax = createProductSalesTax(rawProductData,constructionContext);
        Set<ProductSalesTax> existingOnes = new HashSet<>(productSalesDelivery.getSalesTaxIfPresent().getOrElse(new ArrayList<>()));
        if (!existingOnes.contains(productSalesTax)){
            productSalesDelivery.addSalesTax(productSalesTax);
        }
    }

    private void addProductSalesText(ProductSalesDelivery productSalesDelivery,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductSalesText productSalesText = createProductSalesText(rawProductData,constructionContext);
        Set<ProductSalesText> existingOnes = new HashSet<>(productSalesDelivery.getSalesTextIfPresent().getOrElse(new ArrayList<>()));
        if (!existingOnes.contains(productSalesText)){
            productSalesDelivery.addSalesText(productSalesText);
        }
    }

    private void addProductPlantMRPArea(ProductPlant productPlant,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductPlantMRPArea productPlantMRPArea = createProductPlantMRPArea(rawProductData, constructionContext);
        Set<ProductPlantMRPArea> existingOnes = new HashSet<>(productPlant.getPlantMRPAreaIfPresent().getOrElse(new ArrayList<>()));
        if (!existingOnes.contains(productPlantMRPArea)){
            productPlant.addPlantMRPArea(productPlantMRPArea);
        }
    }

    ProductPlantMRPArea createProductPlantMRPArea(RawProductData rawProductData, DataConstructionContext dataConstructionContext){
        ProductPlantMRPArea productPlantMRPArea = new ProductPlantMRPArea();
        productPlantMRPArea.setProduct(rawProductData.getMaterial());
        productPlantMRPArea.setIsMRPDependentRqmt(rawProductData.getMrpDependentRequirements());
        productPlantMRPArea.setPlant(rawProductData.getPlant());
        productPlantMRPArea.setLotSizingProcedure(rawProductData.getLotSizingProcedure());
        productPlantMRPArea.setMRPType(rawProductData.getMrpType());
        productPlantMRPArea.setMRPGroup(rawProductData.getMrpGroup());
        productPlantMRPArea.setMRPArea(rawProductData.getPlant());
        return productPlantMRPArea;
    }

    ProductSalesTax createProductSalesTax(RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductSalesTax productSalesTax = new ProductSalesTax();
        productSalesTax.setProduct(rawProductData.getMaterial());
        productSalesTax.setCountry(rawProductData.getCountryOfOrigin());
        productSalesTax.setTaxCategory("MWST"); //Hardcoding for now as we dont know the actual values
        productSalesTax.setTaxClassification(rawProductData.getTaxClassification());
        return productSalesTax;
    }

    ProductSalesText createProductSalesText(RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductSalesText productSalesText = new ProductSalesText();
        productSalesText.setProduct(rawProductData.getMaterial());
        productSalesText.setLanguage(rawProductData.getLanguage());
        //productSalesText.setLongText(); //TODO what mapping to be used from JSON
        //productSalesText.setProductDistributionChnl(); //TODO what mapping to be used from JSON
        //productSalesText.setProductSalesOrg(); //TODO what mapping to be used from JSON
        return productSalesText;
    }

    ProductDescription createProductDescription(RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductDescription productDescription = new ProductDescription();
        productDescription.setProduct(rawProductData.getMaterial());
        productDescription.setLanguage(rawProductData.getLanguage());
        productDescription.setProductDescription(rawProductData.getMaterialDescription());
        return productDescription;
    }
    ProductValuation createProductValuation(RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductValuation productValuation = new ProductValuation();
        productValuation.setProduct(rawProductData.getMaterial());
        productValuation.setValuationClass(rawProductData.getValuationClass());
        productValuation.setPriceDeterminationControl(rawProductData.getPriceDeterminationControl());
        productValuation.setProductOriginType(rawProductData.getMaterialOrigin());
        return productValuation;
    }

    ProductSales createProductSales(RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductSales productSales = new ProductSales();
        productSales.setProduct(rawProductData.getMaterial());
        productSales.setTransportationGroup(rawProductData.getTransportationGroup());
        productSales.setTaxClassification(rawProductData.getTaxClassification()); //TODO This value is not persisted
        //LocalDateTime now = LocalDateTime.now();
        //productSales.setSalesStatusValidityDate(now);
        return productSales;
    }

    ProductPlantSales createProductPlantSales(RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductPlantSales productPlantSales = new ProductPlantSales();
        productPlantSales.setProduct(rawProductData.getMaterial());
        productPlantSales.setPlant(rawProductData.getPlant());
        productPlantSales.setLoadingGroup(rawProductData.getLoadingGroup());

        return productPlantSales;
    }

    @Override
    public List<Product> createEntities(List<RawProductData> rawProductDatas,DataConstructionContext constructionContext){
        List<Product> productsOrdered = new ArrayList<>();
        rawProductDatas.forEach(rawProductData->{
            Product product = createOrFetchProduct(productsOrdered,rawProductData,constructionContext);
            product.setProduct(rawProductData.getMaterial());
            product.setProductType(rawProductData.getMaterialType());
            product.setProductOldID(rawProductData.getOldMaterialNumber());
            product.setDivision(rawProductData.getDivision());
            if(!StringUtils.isEmpty(rawProductData.getCrossPlantMaterialStatus()) &&
                    !rawProductData.getCrossPlantMaterialStatus().equals("undefined")) {
                product.setCrossPlantStatus(rawProductData.getCrossPlantMaterialStatus());
            }
            if(!StringUtils.isEmpty(rawProductData.getValidFromCrossPlantMaterial())
                    && !rawProductData.getValidFromCrossPlantMaterial().equals("undefined")) {
                LocalDateTime now = LocalDateTime.now();
                product.setCrossPlantStatusValidityDate(now);
            }
            //product.setCrossPlantStatusValidityDate(LocalDateTime.parse(rawProductData.getValidFromCrossPlantMaterial(), //TODO some error
            //DateTimeFormatter.ofPattern(constructionContext.getLocalDateFormat())));
            product.setWeightUnit(rawProductData.getWeightUnit());
            product.setBaseUnit(rawProductData.getBaseUnitOfMeasure());
            product.setItemCategoryGroup(rawProductData.getItemCategoryGroup());
            product.setProductHierarchy(rawProductData.getProductHierarchy());
            boolean isBatchManagementRequired = rawProductData.getBatchManagement().equalsIgnoreCase("x")? true : false;
            product.setIsBatchManagementRequired(isBatchManagementRequired);
            product.setSerialNumberProfile(rawProductData.getSerialNoProfile());
            product.setCountryOfOrigin(rawProductData.getCountryOfOrigin());
            product.setProductGroup(rawProductData.getMaterialGroup());
            addUnitOfMeasure(product,rawProductData,constructionContext);
            addProductPlant(product,rawProductData,constructionContext);
            addDescription(product,rawProductData,constructionContext);
            addSalesDelivery(product,rawProductData,constructionContext);
            addProductSales(product, rawProductData, constructionContext);
            addProductStorage(product, rawProductData, constructionContext);
            addProductSalesTax(product, rawProductData, constructionContext);

        });
        return productsOrdered;
    }

    private void addProductStorage(Product product,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductStorage productStorage = createProductStorage(rawProductData,constructionContext);
        ProductStorage existingObj = product.getProductStorageIfPresent().getOrElse(new ProductStorage()); //TODO Validate this
        if (!existingObj.equals(productStorage)){
            product.setProductStorage(productStorage);
        }
    }

    private void addProductSales(Product product,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductSales productSales = createProductSales(rawProductData,constructionContext);
        product.getProductSalesIfPresent().getOrElse(new ProductSales());
        ProductSales existingObj = product.getProductSalesIfPresent().getOrElse(new ProductSales());  //TODO Validate this
        if (!existingObj.equals(productSales)){
            product.setProductSales(productSales);
        }
    }
    private void addProductPlant(Product product,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductPlant productPlant = createProductPlant(rawProductData,constructionContext);
        Set<ProductPlant> existingOnes = new HashSet<>(product.getPlantIfPresent().getOrElse(new ArrayList<>()));
        if (!existingOnes.contains(productPlant)){
            product.addPlant(productPlant);
        }
    }
    private void addDescription(Product product,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductDescription productDescription = createProductDescription(rawProductData,constructionContext);
        Set<ProductDescription> existingOnes = new HashSet<>(product.getDescriptionIfPresent().getOrElse(new ArrayList<>()));
        if (!existingOnes.contains(productDescription)){
            product.addDescription(productDescription);
        }
    }
    private void addSalesDelivery(Product product,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductSalesDelivery productSalesDelivery = createProductSalesDelivery(rawProductData,constructionContext);
        Set<ProductSalesDelivery> existingOnes = new HashSet<>(product.getSalesDeliveryIfPresent().getOrElse(new ArrayList<>()));
        if (!existingOnes.contains(productSalesDelivery)){
            product.addSalesDelivery(productSalesDelivery);
        }
    }
    private void addUnitOfMeasure(Product product,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductUnitsOfMeasure productUnitsOfMeasure = createUnitOfMeasure(rawProductData,constructionContext);
        Set<ProductUnitsOfMeasure> existingOnes = new HashSet<>(product.getProductUnitsOfMeasureIfPresent().getOrElse(new ArrayList<>()));
        if (!existingOnes.contains(productUnitsOfMeasure)){
            product.addProductUnitsOfMeasure(productUnitsOfMeasure);
        }
    }
    private void addStorageLocation(ProductPlant productPlant,RawProductData rawProductData,DataConstructionContext constructionContext){
        ProductStorageLocation productPlantStorageLocation = createProductStorageLocation(rawProductData,constructionContext);
        Set<ProductStorageLocation> existingOnes = new HashSet<>(productPlant.getStorageLocationIfPresent().getOrElse(new ArrayList<>()));
        if (!existingOnes.contains(productPlant)){
            productPlant.addStorageLocation(productPlantStorageLocation);
        }
    }
    private Product createOrFetchProduct(List<Product> productsOrdered,RawProductData rawProductData,DataConstructionContext constructionContext){
        String key = rawProductData.getMaterial();
        Product product;
        if (!constructionContext.getProductMap().containsKey(key)) {
            product = new Product();
            productsOrdered.add(product);
            constructionContext.getProductMap().put(key,product);
        }else {
            product = constructionContext.getProductMap().get(key);
        }
        return product;
    }
}
