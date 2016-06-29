package edu.ucdavis.dss.ipa.api.components.auth;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.List;

public class SecurityDTODeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        SecurityDTO securityDTO = new SecurityDTO();

        if (node.has("redirect")) {
            securityDTO.redirect = node.get("redirect").textValue();
        }

        if (node.has("token")) {
            securityDTO.token = node.get("token").textValue();
        }

        if (node.has("displayName")) {
            securityDTO.displayName = node.get("displayName").textValue();
        }

        if (node.has("userRoles")) {

            CollectionType collectionType =
                    TypeFactory
                            .defaultInstance()
                            .constructCollectionType(List.class, UserRoleDTO.class);

            // Convert the tree model to the collection (of UserRole-objects)
            ObjectMapper mapper = new ObjectMapper();

            List<UserRoleDTO> userRoleDTOs = mapper.reader(collectionType).readValue(node.get("userRoles"));
            securityDTO.userRoles = userRoleDTOs;
        }

        return securityDTO;
    }

}
