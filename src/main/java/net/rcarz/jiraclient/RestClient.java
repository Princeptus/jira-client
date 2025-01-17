/**
 * jira-client - a simple JIRA REST client
 * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.rcarz.jiraclient;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

/**
 * A simple REST client that speaks JSON.
 */
public class RestClient {

    private HttpClient httpClient = null;
    private ICredentials creds = null;
    private URI uri = null;

    /**
     * Creates a REST client instance with a URI.
     *
     * @param httpclient Underlying HTTP client to use
     * @param uri Base URI of the remote REST service
     */
    public RestClient(HttpClient httpclient, URI uri) {
        this(httpclient, null, uri);
    }

    /**
     * Creates an authenticated REST client instance with a URI.
     *
     * @param httpclient Underlying HTTP client to use
     * @param creds Credentials to send with each request
     * @param uri Base URI of the remote REST service
     */
    public RestClient(HttpClient httpclient, ICredentials creds, URI uri) {
        this.httpClient = httpclient;
        this.creds = creds;
        this.uri = uri;
    }

    /**
     * Build a URI from a path.
     *
     * @param path Path to append to the base URI
     *
     * @return the full URI
     *
     * @throws URISyntaxException when the path is invalid
     */
    public URI buildURI(String path) throws URISyntaxException {
        return buildURI(path, null);
    }

    /**
     * Build a URI from a path and query parmeters.
     *
     * @param path Path to append to the base URI
     * @param params Map of key value pairs
     *
     * @return the full URI
     *
     * @throws URISyntaxException when the path is invalid
     */
    public URI buildURI(String path, Map<String, String> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder(uri);
        ub.setPath(ub.getPath() + path);

        if (params != null) {
            for (Map.Entry<String, String> ent : params.entrySet())
                ub.addParameter(ent.getKey(), ent.getValue());
        }

        return ub.build();
    }
    
    /**
     * Executes a HTTP-request.
     * 
     * @param req
     * Request to be executed.
     * @return Result string or null, if no result was provided.
     * @throws RestException In case an HTTP status code >= 300 was provided.
     * @throws IOException If the request failed.
     */
    private String httpRequest(HttpRequestBase req) throws RestException, IOException 
    {
    	req.addHeader("Accept", "application/json");

        if (creds != null)
            creds.authenticate(req);

        HttpResponse resp = httpClient.execute(req);
        HttpEntity ent = resp.getEntity();
        StringBuilder result = new StringBuilder();

        if (ent != null) {
            String encoding = null;
            if (ent.getContentEncoding() != null) {
            	encoding = ent.getContentEncoding().getValue();
            }
            
            if (encoding == null) {
    	        Header contentTypeHeader = resp.getFirstHeader("Content-Type");
    	        HeaderElement[] contentTypeElements = contentTypeHeader.getElements();
    	        for (HeaderElement he : contentTypeElements) {
    	        	NameValuePair nvp = he.getParameterByName("charset");
    	        	if (nvp != null) {
    	        		encoding = nvp.getValue();
    	        	}
    	        }
            }
            
            InputStreamReader isr =  encoding != null ?
                new InputStreamReader(ent.getContent(), encoding) :
                new InputStreamReader(ent.getContent());
            BufferedReader br = new BufferedReader(isr);
            String line = "";

            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            isr.close();
            br.close();
            isr=null;
            br=null;
        }
        EntityUtils.consumeQuietly(ent);

        StatusLine sl = resp.getStatusLine();

        if (sl.getStatusCode() >= 300)
            throw new RestException(sl.getReasonPhrase(), sl.getStatusCode(), result.toString(), resp.getAllHeaders());
        
        return result.length() == 0 ? null : result.toString();
    }

    private JSONObject requestMap(HttpRequestBase req) throws RestException, IOException {
        String result = httpRequest(req);

        return result != null ? new JSONObject(result) : null;
    }
    
    private JSONArray requestArray(HttpRequestBase req) throws RestException, IOException {
        String result = httpRequest(req);

        return result != null ? new JSONArray(result) : null;
    }
    
   

    private JSONObject request(HttpEntityEnclosingRequestBase req, String payload)
        throws RestException, IOException {

        if (payload != null) {
            StringEntity ent = null;

            try {
                ent = new StringEntity(payload, "UTF-8");
                ent.setContentType("application/json");
            } catch (UnsupportedCharsetException ex) {
                /* utf-8 should always be supported... */
            }

            req.addHeader("Content-Type", "application/json");
            req.setEntity(ent);
        }

        return requestMap(req);
    }
    
    private JSONObject request(HttpEntityEnclosingRequestBase req, File file)
        throws RestException, IOException {
        if (file != null) {
            File fileUpload = file;
            req.setHeader("X-Atlassian-Token", "nocheck");
            MultipartEntity ent = new MultipartEntity();
            ent.addPart("file", new FileBody(fileUpload));
            req.setEntity(ent);
        }
        return requestMap(req);
    }

    private JSONObject request(HttpEntityEnclosingRequestBase req, Issue.NewAttachment... attachments)
        throws RestException, IOException {
        if (attachments != null) {
            req.setHeader("X-Atlassian-Token", "nocheck");
            MultipartEntity ent = new MultipartEntity();
            for(Issue.NewAttachment attachment : attachments) {
                String filename = attachment.getFilename();
                Object content = attachment.getContent();
                if (content instanceof byte[]) {
                    ent.addPart("file", new ByteArrayBody((byte[]) content, filename));
                } else if (content instanceof InputStream) {
                    ent.addPart("file", new InputStreamBody((InputStream) content, filename));
                } else if (content instanceof File) {
                    ent.addPart("file", new FileBody((File) content, filename));
                } else if (content == null) {
                    throw new IllegalArgumentException("Missing content for the file " + filename);
                } else {
                    throw new IllegalArgumentException(
                        "Expected file type byte[], java.io.InputStream or java.io.File but provided " +
                            content.getClass().getName() + " for the file " + filename);
                }
            }
            req.setEntity(ent);
        }
        return requestMap(req);
    }

    private JSONObject request(HttpEntityEnclosingRequestBase req, JSONObject payload)
        throws RestException, IOException {

        return request(req, payload != null ? payload.toString() : null);
    }

    /**
     * Executes an HTTP DELETE with the given URI.
     *
     * @param uri Full URI of the remote endpoint
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JSONObject delete(URI uri) throws RestException, IOException {
        return requestMap(new HttpDelete(uri));
    }

    /**
     * Executes an HTTP DELETE with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JSONObject delete(String path) throws RestException, IOException, URISyntaxException {
        return delete(buildURI(path));
    }

    /**
     * Executes an HTTP GET with the given URI.
     *
     * @param uri Full URI of the remote endpoint
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JSONObject getMap(URI uri) throws RestException, IOException {
        return requestMap(new HttpGet(uri));
    }

    /**
     * Executes an HTTP GET with the given URI.
     *
     * @param uri Full URI of the remote endpoint
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JSONArray getArray(URI uri) throws RestException, IOException {
        return requestArray(new HttpGet(uri));
    }

    /**
     * Executes an HTTP GET with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param params Map of key value pairs
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JSONObject getMap(String path, Map<String, String> params) throws RestException, IOException, URISyntaxException {
        return getMap(buildURI(path, params));
    }
    
    /**
     * Executes an HTTP GET with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param params Map of key value pairs
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JSONArray getArray(String path, Map<String, String> params) throws RestException, IOException, URISyntaxException {
        return getArray(buildURI(path, params));
    }

    /**
     * Executes an HTTP GET with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JSONObject getMap(String path) throws RestException, IOException, URISyntaxException {
        return getMap(path, null);
    }


    /**
     * Executes an HTTP GET with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JSONArray getArray(String path) throws RestException, IOException, URISyntaxException {
        return getArray(path, null);
    }

    /**
     * Executes an HTTP POST with the given URI and payload.
     *
     * @param uri Full URI of the remote endpoint
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JSONObject post(URI uri, JSONObject payload) throws RestException, IOException {
        return request(new HttpPost(uri), payload);
    }

    /**
     * Executes an HTTP POST with the given URI and payload.
     *
     * At least one JIRA REST endpoint expects malformed JSON. The payload
     * argument is quoted and sent to the server with the application/json
     * Content-Type header. You should not use this function when proper JSON
     * is expected.
     *
     * @see https://jira.atlassian.com/browse/JRA-29304
     *
     * @param uri Full URI of the remote endpoint
     * @param payload Raw string to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JSONObject post(URI uri, String payload) throws RestException, IOException {
    	String quoted = null;
    	if(payload != null && !payload.equals(new JSONObject())){
    		quoted = String.format("\"%s\"", payload);
    	}
        return request(new HttpPost(uri), quoted);
    }

    /**
     * Executes an HTTP POST with the given path and payload.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JSONObject post(String path, JSONObject payload)
        throws RestException, IOException, URISyntaxException {

        return post(buildURI(path), payload);
    }
    
    /**
     * Executes an HTTP POST with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JSONObject post(String path)
        throws RestException, IOException, URISyntaxException {
    	
        return post(buildURI(path), new JSONObject());
    }
    
    /**
     * Executes an HTTP POST with the given path and file payload.
     * 
     * @param path Full URI of the remote endpoint
     * @param file java.io.File
     * 
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws RestException 
     */
    public JSONObject post(String path, File file) throws RestException, IOException, URISyntaxException{
        return request(new HttpPost(buildURI(path)), file);
    }

    /**
     * Executes an HTTP POST with the given path and file payloads.
     *
     * @param path    Full URI of the remote endpoint
     * @param attachments   the name of the attachment
     *
     * @throws URISyntaxException
     * @throws IOException
     * @throws RestException
     */
    public JSONObject post(String path, Issue.NewAttachment... attachments)
        throws RestException, IOException, URISyntaxException
    {
        return request(new HttpPost(buildURI(path)), attachments);
    }

    /**
     * Executes an HTTP PUT with the given URI and payload.
     *
     * @param uri Full URI of the remote endpoint
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JSONObject put(URI uri, JSONObject payload) throws RestException, IOException {
        return request(new HttpPut(uri), payload);
    }

    /**
     * Executes an HTTP PUT with the given path and payload.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JSONObject put(String path, JSONObject payload)
        throws RestException, IOException, URISyntaxException {

        return put(buildURI(path), payload);
    }
    
    /**
     * Exposes the http client.
     *
     * @return the httpClient property
     */
    public HttpClient getHttpClient(){
        return this.httpClient;
    }
}

