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

	public DwClient() throws Exception {
		String filename = System.getProperty("user.home") + File.separator + ".ipa" + File.separator + "dss-dw.properties";
		File propsFile = new File(filename);

		try {
			InputStream input;

			input = new FileInputStream(propsFile);

			Properties prop = new Properties();

			prop.load(input);
			input.close();

			if (prop.getProperty("URL") == null) {
				throw new Exception(filename + " is missing required URL property.");
			}
			if (prop.getProperty("TOKEN") == null) {
				throw new Exception(filename + " is missing required TOKEN property.");
			}
			if (prop.getProperty("PORT") == null) {
				throw new Exception(filename + " is missing required PORT property.");
			}
			
			ApiUrl = prop.getProperty("URL");
			ApiToken = prop.getProperty("TOKEN");
			ApiPort = Integer.parseInt(prop.getProperty("PORT"));
		} catch (FileNotFoundException e) {
			throw new Exception("Could not find " + filename + ".");
		} catch (IOException e) {
			throw new Exception("Unhandled IOException in DwClient: " + e);
		}
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

}
