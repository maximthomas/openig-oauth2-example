import org.forgerock.json.jose.builders.JwtBuilderFactory
import org.forgerock.json.jose.jws.SignedJwt

def redirectToAuthResponse() {
    session.successLoginRedirect = request.getUri().toString()
    logger.info("redirect to OAuth2 authentication: " + session)
    def response = new Response(Status.FOUND)
	response.headers.add("Location", "/oauth")
    response.headers.add("Content-Type", "application/json; charset=UTF-8")
    response.entity=new org.forgerock.json.JsonValue([
        "location":  "/oauth",				
    ])    
    return response;
}


if(session.userInfo != null) {
    return next.handle(context, request)
}

//test if access token present and valid
if(session.access_token == null) {
    logger.info("access token is emtpy")
    return redirectToAuthResponse()    
} 
if(session.access_token.expires_at == null) {
    logger.info("access token expires_at not set")
    return redirectToAuthResponse()
} 
if(session.access_token.expires_at < System.currentTimeMillis()) {
     logger.info("access token expired: " + session.access_token.expires_at)
     return redirectToAuthResponse()
}   

logger.info("getting JWT with user info...")
def httpRequest = new Request()
httpRequest.method = "GET"
httpRequest.uri = userInfoUri
httpRequest.headers['Authorization'] = "OAuth " + session.access_token.access_token
def response = http.send(httpRequest).get(5, java.util.concurrent.TimeUnit.SECONDS)
try {
    def sjwt = new JwtBuilderFactory().reconstruct(response.entity.string, SignedJwt.class)
    logger.info("sjwt: " + sjwt.getClaimsSet())
    session.userInfo = response.entity.string
} catch(Exception ex) {
    logger.warn("exception occurred: " + ex + ", response " + response.entity)
    throw ex
} finally {
    response.close();
}

return next.handle(context, request)
