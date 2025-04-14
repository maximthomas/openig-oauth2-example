import org.forgerock.json.jose.builders.JwtBuilderFactory
import org.forgerock.json.jose.jws.SignedJwt

def getForbiddenResponse() {
    def response = new Response(Status.FORBIDDEN)
	response.headers.add("Content-Type", "text/html; charset=UTF-8")
    response.entity="<h1>Forbidden</h1>"
    return response;
}

if(contexts['oauth2'] == null) {
    logger.warn("there is no access token in the request")
    return getForbiddenResponse()
}

def accessTokenInfo = contexts['oauth2'].accessToken.info
logger.info("" + accessTokenInfo)

if(allowedEmails != null) {
    def allowedEmailSet = allowedEmails.split(',').toList().toSet()
    if(!allowedEmailSet.contains(accessTokenInfo['email'])) {
        logger.warn("email " + accessTokenInfo['email'] + " is not in allowed email list: " + allowedEmailSet)
        return getForbiddenResponse() 
    }
}

return next.handle(context, request)

