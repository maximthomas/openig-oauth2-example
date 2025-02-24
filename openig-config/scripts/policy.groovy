import org.forgerock.json.jose.builders.JwtBuilderFactory
import org.forgerock.json.jose.jws.SignedJwt

def getUnatuhorizedResponse() {
    def response = new Response(Status.UNAUTHORIZED)
	response.headers.add("Content-Type", "application/json; charset=UTF-8")
    response.entity=new org.forgerock.json.JsonValue([
        "error": "unauthorized",				
    ])    
    return response;
}
def accessTokenInfo = contexts['oauth2'].accessToken.info
logger.info("" + accessTokenInfo)

if(allowedEmails != null) {
    def allowedEmailSet = allowedEmails.split(',').toList().toSet()
    if(!allowedEmailSet.contains(accessTokenInfo['email'])) {
        logger.warn("email " + accessTokenInfo['email'] + " is not in allowed email list: " + allowedEmailSet)
        return getUnatuhorizedResponse() 
    }
}

return next.handle(context, request)

