/* globals Java, logger*/
(function () {
  "use strict";

  /**
  *  Fusion 5 only!
  *  library used for making a service call to Fusion 5 service.
  *  In Fusion 4, use one of the helpers in HttpLib.
  */
  var libName = 'FusionServiceLib'
  var lib = {};

  var isDebug = false;
  var rethrowErrors = false;

  /**
  * @param rte_b if true, this library will rethrow errors instead of `catch`, `log`, and returning null or emptystring
  */
  lib.setRethrowErrors = function(rte_b){ rethrowErrors = (true && rte_b)}

  /**
  * @args - debug_b if true, this library will log messages
  */
  lib.setDebugLogging = function(debug_b){
    isDebug = (true && debug_b);
  }
  function logIfDebug(message){
    if( isDebug){
      var args = Array.prototype.slice.call(arguments)
      if(args.length > 0){
        logger.info(args[0],args.slice(1));
      }
    }
  }
  lib.getLibName = function(){return libName}


  var WebApplicationContextUtils = Java.type("org.springframework.web.context.support.WebApplicationContextUtils");
  var RequestContextHolder = Java.type("org.springframework.web.context.request.RequestContextHolder");
  var JavaString = Java.type("java.lang.String");
  var HttpHeaders = Java.type("org.springframework.http.HttpHeaders");
  var HttpMethod = Java.type("org.springframework.http.HttpMethod")
  var HttpEntity = Java.type("org.springframework.http.HttpEntity")
  var ServiceAccountJwtSupport = Java.type("com.lucidworks.cloud.security.ServiceAccountJwtSupport")


  var _springContext = WebApplicationContextUtils.getWebApplicationContext(RequestContextHolder.getRequestAttributes().getRequest().getServletContext());
  var _serviceAccountJwt = _springContext.getBean(ServiceAccountJwtSupport.class);
  var _rest = _springContext.getBean("restTemplate");
  var _feign = _springContext.getBean(com.lucidworks.client.api.impl.FusionFeignClient.class)

    /**
      * uri (required).  The Fusion service uri i.e. /api/v1/jobs
      * server - Fusion API server (default localhost)
      * port - Fusion API port (default 8765)
      * protocol - HTTP protocol (default http)
      * @return JSON object from response
    */
    lib.queryFusionService = function(service,uri,httpHeaders,protocol){
      service = service || 'admin';
      protocol = protocol || 'http';
      var url = protocol + "://" + service + uri;
      logIfDebug('SUBMITTING "' + service + '" request for url: ' + url);

      return lib.query2Json(url,httpHeaders);
    }

    /**
    * perform Spring RestTemplate GET
    * @param url
    * @param optional java class type T (java.lang.String.class default)
    * @param HttpHeaders (optional) defaults to just the JWT Header
    * @return ResponseEntity<T> or null if a problem is encountered.  @see setRethrowErrors
    */
    lib.doGET = function(url,typ,httpHeaders){
      typ = typ || JavaString.class
      httpHeaders = httpHeaders || lib.getJWTHeaders()
      var resp = null;
      try{
          logIfDebug("Querying via RestTemplate for url: " + url + " and type: " + typ.toString())
          resp = _rest.exchange(url,HttpMethod.GET,new HttpEntity(httpHeaders),typ)// ::ResponseEntity<T>
          if(resp && resp.getStatusCode().is2xxSuccessful()){
            logIfDebug("Got response with status: " + resp.getStatusCode() + " and type: " + resp.getClass().getName())

          }else{
            logger.error("FusionServiceLib:: GET to {} was unsuccessful.  Status: ", url, resp.getStatusCode())
          }
      }catch (error){
         logger.error("FusionServiceLib::Error querying Fusion for '" + url + "' Err: " + error);
         if(rethrowErrors){
           throw error
         }
      }
      return resp;

    }
    lib.getFeignClient = function(){
      return _feign
    }
    /**
    @param url
    @return response body as a String or null if a problem was encountered. @see setRethrowErrors
    **/
    lib.query2String = function(url, httpHeaders){
      var resp = lib.doGET(url,JavaString.class,httpHeaders)
      return resp ? resp.getBody() : null;
    }
    /**
    @param url
    @param httpHeaders (optional)
    @return Json object or null if a problem was encountered. @see setRethrowErrors
    **/
    lib.query2Json = function(url, httpHeaders){
      var body = lib.query2String(url,httpHeaders);
      try{
        return JSON.parse(body)
      }catch(error){
        logger.error("unable to parse Json from text '{}',\nerror: {}",body,error)
        if(rethrowErrors){
         throw error
        }
      }
      return null
    }

    lib.getJWTHeaders = function(){
      var headers = new HttpHeaders();
      var jwt = _serviceAccountJwt.getJwt()
      if(jwt){
        headers.setBearerAuth(jwt.getTokenValue())
      }
      return headers;
    }
    /**
    * fetch datasource history
    */
    lib.getDSHistory = function(dataSourceName, limitCount,  protocol){
         return lib.getHistory('datasource',dataSourceName,limitCount,protocol)
    }

    /**
    * fetch spark job run history
    */
    lib.getJobHistory = function(jobId, limitCount, protocol){
         return lib.getHistory('spark',jobId,limitCount,protocol)
    }

    /**
    *  fetch latest job run details for a Spark job.  This is much like what is in the driver log
    */
//    lib.getSparkLogForJob = function(jobId, rowLimit, protocol){
//        rowLimit = rowLimit || 10
//         var uri = '/api/v2/spark/driver/log/' + jobId+ '?rows=' + rowLimit
//
//         logIfDebug("getSparkJobLog calling admin service for: " +uri)
//
//         return lib.queryFusionService('job-rest-server',uri, null, protocol)
//
//    }

    /**
    * fetch job history from spark, datasource, or type jobs
    * @param objType: one of spark, datasource or type
    * @param jobid: the jobid of the job in question
    * @param limitCount: optional number specifying how many history elements to request
    * @return Json object or empty string
    */
    lib.getHistory = function(objType,jobid, limitCount, httpHeaders, protocol){
       limitCount = limitCount || 10
       var uri = '/api/v1/jobs/' + objType + ':' + jobid + '/history?limit=' + limitCount

       logIfDebug("getHistory calling admin service for: " +uri)

       return lib.queryFusionService('admin',uri, httpHeaders, protocol)
    }

    lib.getBlobAsStream = function(blobId){
      var blob = null
      try{
        logIfDebug("getting Feign Client and loading '" + blobId + "'")

        blob = lib.getFeignClient().getBlob(blobId)
        logIfDebug("got '" + blobId + "'")
      }catch(e){
        logger.error("Encountered exception while fetching blob '{}'.  Message: {}", blobId, e)
      }
      return blob;

    }
    /**
    * fetch a text blob from the BlobStore and parse it as JSON.
    */
    lib.getJsonBlob = function(blobId, httpHeaders,protocol){

         return lib.queryFusionService('admin','/api/v1/blobs/' + blobId, httpHeaders, protocol)
     }
    /**
     * fetch fusion collection definition
     */
     lib.getFusionCollection = function(indexCollection,  httpHeaders, protocol){
        return lib.queryFusionService('admin','/api/v1/collections/' + indexCollection,httpHeaders,protocol);
      }

     /**
     * fetch a text blob as a String
     */
     lib.getStringBlob = function(blobId, httpHeaders, protocol){
        protocol = protocol || 'http';
        var url = protocol + "://admin/api/v1/blobs/" + blobId

       var s =  lib.query2String(url,httpHeaders);
       return s;
     }


    /**
    * query via a Query Profile
    * @param profileId - the name of the profile to call
    * @param queryString - the `q=*:*` query string parameters for the query
    * @param asJson - true to parse the response as JSON which is then returned
    * @return an org.springframework.http.ResponseEntity.  Call getBody() for the xml or json result string
    */
    lib.queryByProfile = function(profileId,queryString,asJson){

        var url = "http://query/query/" + profileId + "?" + queryString
        var resp = lib.doGET(url,JavaString.class)
        if(asJson && resp){
            try{
                resp =  JSON.parse(resp.getBody())
            }catch(error){
                logger.error("unable to parse Json from text '{}',\nerror: {}",body,error)
                if(rethrowErrors){
                   throw error
                 }
            }
        }

        return resp

    }


    /**
      Lazy load this `lib` into the ctx object.
      If called as a Managed Index Pipeline, the arguments will be doc, ctx, collection, solrClient, solrFactory
      If called as a Managed Query Pipeline, the arguments will be request, response, ctx...

      Check for argument types to figure out which one is the ctx.

    */

    return function () {
      var args = Array.prototype.slice.call(arguments);
      var rtnObj = null
      //var Context = Java.type("com.lucidworks.apollo.pipeline.impl.DefaultContext")
      var Context = Java.type("com.lucidworks.apollo.pipeline.Context")


      //var name1 = args[1].getClass().getName()
      //var name2 = args[2].getClass().getName()
      var _ctx = null;
      if( args[1] instanceof Context){ //name1 == "com.lucidworks.apollo.pipeline.impl.DefaultContext"  ||  name1 == "com.lucidworks.apollo.pipeline.Context"){
         _ctx = args[1]; //Index Pipeline
         rtnObj = args[0]
      }else if( args[2] instanceof Context){//name2 == "com.lucidworks.apollo.pipeline.impl.DefaultContext" || name2 == "com.lucidworks.apollo.pipeline.Context"){
        _ctx = args[2]; // Query Pipeline
      }else{
         throw "Unable to find a " + Context + " in the arguments list, can not register library " + libName;
      }
      //register libName in context object
      if (! _ctx.containsKey(libName)){
          _ctx.put(libName,lib);
      }
      if(rtnObj){
        return args[0];
      }

    }

})();