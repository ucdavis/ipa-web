package edu.ucdavis.dss.dw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.ucdavis.dss.dw.dto.DwTerm;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

@Service("DwClientService")
public class DwClient {
	private static final Logger log = LogManager.getLogger();

	private CloseableHttpClient httpclient;
	private HttpHost targetHost;
	private HttpClientContext context;
	private String ApiUrl, ApiToken;
	private int ApiPort;

	public DwClient(String url, String token, String port) throws Exception {
		ApiUrl = url;
		ApiToken = token;
		ApiPort = Integer.parseInt(port);
	}

	private boolean connect() {
		// Set the default timeout to be 90 seconds.
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(90 * 1000).build();

		httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		
		targetHost = new HttpHost(ApiUrl, ApiPort, "https");

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials("nobody", "nothing"));

		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		
		authCache.put(targetHost, basicAuth);

		// Add AuthCache to the execution context
		context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);

		return true;
	}

	/**
	 * Searches all people based on 'query'. May match against multiple fields.
	 * 
	 * @param query
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<DwPerson> searchPeople(String query) throws ClientProtocolException, IOException {
		List<DwPerson> dwPeople = null;
		
		if (connect() && query != null) {
			HttpGet httpget = new HttpGet("/people/search?q=" + URLEncoder.encode(query, "UTF-8") + "&token=" + ApiToken);

			try {
				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);
				
				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}

				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();
				JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
				if (arrNode != null) {
					dwPeople = mapper.readValue(
							arrNode.toString(),
							mapper.getTypeFactory().constructCollectionType(
									List.class, DwPerson.class));
				} else {
					log.warn("searchUsers Response from DW returned null, for criterion = " + query);
				}

				response.close();
			} catch (JsonParseException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
		} else if (query == null) {
			log.warn("No query given.");
		}
		
		return dwPeople;
	}

	public List<DwTerm> getTerms() throws IOException {
		List<DwTerm> dwTerms = null;

		if(connect()) {
			// https://beta.dw.dss.ucdavis.edu/terms?token=dssit
			HttpGet httpGet = new HttpGet("/terms?token=" + ApiToken);

			try {
				CloseableHttpResponse response = httpclient.execute(targetHost, httpGet, context);
				StatusLine line = response.getStatusLine();

				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}

				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();
				JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));

				if (arrNode != null) {
					dwTerms = mapper.readValue(
							arrNode.toString(),
							mapper.getTypeFactory().constructCollectionType(List.class, DwTerm.class));
				} else {
					log.warn("getTerms Reponse from DW returned null");
				}

				response.close();
			} catch (JsonParseException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
		} else {
			log.warn("Could not connect to DW");
		}

		return dwTerms;
	}

}
