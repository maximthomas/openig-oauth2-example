import org.forgerock.http.oauth2.AccessTokenInfo
import org.forgerock.http.protocol.Request
import org.forgerock.json.JsonValue

import org.forgerock.json.jose.builders.JwtBuilderFactory
import org.forgerock.json.jose.jws.SignedJwt


logger.info("getting JWT with user info...")
def httpRequest = new Request()
httpRequest.method = "GET"
httpRequest.uri = userInfoUri
httpRequest.headers['Authorization'] = "OAuth " + token
def response = http.send(httpRequest).get(5, java.util.concurrent.TimeUnit.SECONDS)
try {
    def sjwt = new JwtBuilderFactory().reconstruct(response.entity.string, SignedJwt.class)
    logger.info("sjwt: " + sjwt.getClaimsSet())
    return new AccessTokenInfo(new JsonValue(sjwt.getClaimsSet().getProperties().all), token, new HashSet<>(), sjwt.getClaimsSet().getExpirationTime().getTime() * 1000)
} catch(Exception ex) {
    logger.warn("exception occurred: " + ex + ", response " + response.entity)
    throw ex
} finally {
    response.close()
}
