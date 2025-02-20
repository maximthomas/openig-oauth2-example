logger.info("access_token: " + attributes.access_token)

session.access_token = attributes.access_token
session.access_token.expires_at = System.currentTimeMillis() + (attributes.access_token.expires_in * 1000)

logger.info("session: " + session)

if(session.successLoginRedirect != null) {
    def response = new Response(Status.FOUND)
	response.headers.put("Location",  session.successLoginRedirect)
    response.headers.add("Content-Type", "application/json; charset=UTF-8")
    response.entity=new org.forgerock.json.JsonValue([
        "location":  "/oauth",				
    ])    
    session.successLoginRedirect = null
    return response;
}

return next.handle(context, request)