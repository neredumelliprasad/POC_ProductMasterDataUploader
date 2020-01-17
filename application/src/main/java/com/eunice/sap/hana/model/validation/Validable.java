package com.eunice.sap.hana.model.validation;
/**
 * Created by roychoud on 30 Dec 2019.
 */

import com.eunice.sap.hana.controller.ProductController;
import com.eunice.sap.hana.model.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * History:
 * <ul>
 * <li> 30 Dec 2019 : roychoud - Created
 * </ul>
 *
 * @authors roychoud : Arunava Roy Choudhury
 */
public abstract class Validable
{
    private static final Logger LOG = LoggerFactory.getLogger(Validable.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Set<Field> rawDataFields = new HashSet<>();

    public Validable(){
        Stream.of(this.getClass().getDeclaredFields()).forEach(each->{
            rawDataFields.add(each);
        });
    }

    protected Optional<ValidationResult> extendValidations(Field eachField,int position){
        return Optional.empty();
    }

    public final Set<ValidationResult> validate(int pos){
        Set<ValidationResult> validationResults = new HashSet<>();
        rawDataFields.stream().forEach(each->{
            try
            {
                Optional<ValidationResult> mandatoryFieldValidation = validateMandatoryField(each, pos);
                Optional<ValidationResult> numberValidation = validateNumber(each, pos);
                Optional<ValidationResult> dateValidation = validateDate(each, pos);
                Optional<ValidationResult> extraValidations = extendValidations(each,pos);
                if (mandatoryFieldValidation.isPresent())
                    validationResults.add(mandatoryFieldValidation.get());
                if (numberValidation.isPresent())
                {
                    validationResults.add(numberValidation.get());
                }
                if (dateValidation.isPresent())
                {
                    validationResults.add(dateValidation.get());
                }
                if (extraValidations.isPresent())
                {
                    validationResults.add(extraValidations.get());
                }
            }
            catch (IllegalAccessException e){
                throw new RuntimeException("Access not available to class fields",e);
            }
        });
        return validationResults;
    }

    private Optional<ValidationResult> validateNumber(Field eachField,int position) throws IllegalAccessException
    {
        boolean isValid = true;
        NumberValidator numberValidator = eachField.getAnnotation(NumberValidator.class);
        Object input = null;
        if (numberValidator!=null){
            input = this.getData(eachField);
            if (!StringUtils.isEmpty(input) && !input.toString().equals(" ")) //Added as per client requirement
            {
                Precision precision = numberValidator.precision();
                if (precision == Precision.INTEGER)
                {
                    try{
                        Integer.parseInt(input.toString());
                    }
                    catch (NumberFormatException e){
                        isValid = false;
                    }
                }
            }
        }
        return isValid? Optional.empty(): Optional.of(new ValidationResult(eachField.getName(), ErrorType.INCORRECT_NUMBER_FORMAT,input,position));
    }

    private Optional<ValidationResult> validateMandatoryField(Field eachField, int position) throws IllegalAccessException
    {
        boolean isValid = true;
        MandatoryField mandatoryField = eachField.getAnnotation(MandatoryField.class);
        String input = null;
        if (mandatoryField!=null){
            input = (String) this.getData(eachField);
            if (StringUtils.isEmpty(input))
            {
                isValid = false;
            }
        }
        return isValid? Optional.empty(): Optional.of(new ValidationResult(eachField.getName(), ErrorType.MANDATORY_FIELD_MISSING,input,position));
    }

    private Optional<ValidationResult> validateDate(Field eachField,int position) throws IllegalAccessException
    {
        boolean isValid = true;
        DateValidator dateValidator = eachField.getAnnotation(DateValidator.class);
        String input = null;
        if (dateValidator!=null){
            input = (String) this.getData(eachField);
            if (!StringUtils.isEmpty(input) && !input.equals(" "))
            {
                String dateFormat = dateValidator.dateFormat();
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                try{
                    sdf.setLenient(false);
                    sdf.parse(input);
                    LOG.info("Date validation successfull. Date format: ", dateFormat, ". Value: ", input);
                }
                catch (ParseException e){
                    isValid = false;
                    LOG.info("Exception parsing the date", e);
                }

            }
        }
        return isValid? Optional.empty(): Optional.of(new ValidationResult(eachField.getName(), ErrorType.DATE_FORMAT_ERROR,input,position));
    }

    @Override
    public String toString() {
        try{
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }
        catch (Exception e){
            return "No String representation could be created!";
        }
    }

    public abstract Object getData(Field field);

    public abstract Integer getIndex();
}
