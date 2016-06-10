package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

public class TeachingCallDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		TeachingCall teachingCall = new TeachingCall();

		if (node.has("id")) {
			teachingCall.setId(node.get("id").longValue());
		}

		if (node.has("sentToFederation")) {
			teachingCall.setSentToFederation(node.get("sentToFederation").booleanValue());
		}

		if (node.has("sentToSenate")) {
			teachingCall.setSentToSenate(node.get("sentToSenate").booleanValue());
		}

		if (node.has("message")) {
			teachingCall.setMessage(node.get("message").textValue());
		}

		if (node.has("emailInstructors")) {
			teachingCall.setEmailInstructors(node.get("emailInstructors").booleanValue());
		}

		if (node.has("termsBlob")) {
			teachingCall.setTermsBlob(node.get("termsBlob").textValue());
		}

		if (node.has("showUnavailabilities")) {
			teachingCall.setShowUnavailabilities(node.get("showUnavailabilities").booleanValue());
		}

		if (node.has("dueDate")) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String textDate = node.get("dueDate").textValue();
			Date date = null;
			try {
				date = new java.sql.Date(format.parse(textDate).getTime());
			} catch (ParseException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
			teachingCall.setDueDate((java.sql.Date) date);
		}

		return teachingCall;
	}

}
