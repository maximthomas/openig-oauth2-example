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

def sjwt = new JwtBuilderFactory().reconstruct(session.userInfo, SignedJwt.class);

if ((sjwt.getClaimsSet().getExpirationTime() !=null && sjwt.getClaimsSet().getExpirationTime().before(new Date()))) {
    logger.warn("jwt expired " + jwt.getClaimsSet().getExpirationTime());
    session.userInfo = null
    return getUnatuhorizedResponse() 
}

if(allowedEmails != null) {
    def allowedEmailSet = allowedEmails.split(',').toList().toSet()
    if(!allowedEmailSet.contains(sjwt.getClaimsSet().getClaim("email"))) {
        logger.warn("email " + sjwt.getClaimsSet().getClaim("email") + " is not in allowed email list: " + allowedEmailSet)
        return getUnatuhorizedResponse() 
    }
}

return next.handle(context, request)

