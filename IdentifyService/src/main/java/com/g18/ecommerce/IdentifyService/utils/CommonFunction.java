package com.g18.ecommerce.IdentifyService.utils;

import com.g18.ecommerce.IdentifyService.exception.ErrorCode;
import com.g18.ecommerce.IdentifyService.exception.ValidateException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@NoArgsConstructor
public class CommonFunction {

    @SneakyThrows
    public static void jsonValidate(InputStream inputStream, String json) {
        Map<String, String> fieldDescriptions = new HashMap<>();
        fieldDescriptions.put("username", "Username must be 6-10 characters and cannot contain special characters");
        fieldDescriptions.put("password", "Password must be required");
        fieldDescriptions.put("email", "Must be a valid email format");
        fieldDescriptions.put("fullName", "Full name must not exceed 20 characters");
        fieldDescriptions.put("phoneNumber", "Phone number must be 10 digits starting with 0");

        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(inputStream);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> errors = schema.validate(jsonNode);
        Map<String, String> stringSetMap = new HashMap<>();

        for (ValidationMessage error : errors) {
            String pathFormatted = formatStringValidate(error.getMessage());
            String description = fieldDescriptions.getOrDefault(pathFormatted, error.getMessage()
                    .replace("$." + pathFormatted + ":", ""));

            if (stringSetMap.containsKey(pathFormatted)) {
                stringSetMap.compute(pathFormatted, (k, message) -> message + ", " + description);
            } else {
                stringSetMap.put(pathFormatted, description);
            }
        }

        if (!CollectionUtils.isEmpty(errors)) {
            throw new ValidateException(ErrorCode.VALIDATE_ERROR, stringSetMap);
        }
    }

    private static String formatStringValidate(String str) {
        String regex = "\\$\\.([a-zA-Z0-9_]+):";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(1); // Trả về tên trường
        }
        return null;
    }

}
