function generateRandomString(length) {
    const array = new Uint8Array(length);
    crypto.getRandomValues(array);
    return Array.from(array, byte =>
        ('0' + byte.toString(16)).slice(-2)
    ).join('');
}

function base64UrlEncode(arrayBuffer) {
    return btoa(String.fromCharCode.apply(null, new Uint8Array(arrayBuffer)))
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=+$/, '');
}

function generateCodeChallenge(codeVerifier) {
    const hash = CryptoJS.SHA256(codeVerifier);
    const base64Hash = CryptoJS.enc.Base64.stringify(hash);
    return base64UrlEncode(base64Hash);
}
async function getOAuthSettings() {
    let settings = sessionStorage.getItem("oauth2.settings")
    if(!settings) {
        const response = await fetch('settings.json');
        settings = await response.json();        
    } 
    console.log('OAuth2 Settings:', settings);
    return settings;
}

async function startPKCEFlow() {

    const settings = await getOAuthSettings();
    
    const codeVerifier = generateRandomString(64);
    const codeChallenge = generateCodeChallenge(codeVerifier);

    sessionStorage.setItem('code_verifier', codeVerifier);

    const authParams = new URLSearchParams({
        client_id: settings.client_id,
        response_type: 'code',
        redirect_uri: 'http://openig.example.org:8080/oauth/callback',
        code_challenge: codeChallenge,
        code_challenge_method: 'S256',
        scope: settings.scope,
        state: generateRandomString(16)
    });

    // 4. Redirect to authorization endpoint
    const authUrl = settings.auth_url + `?${authParams.toString()}`;
    window.location.href = authUrl;
}

// Handle callback (after redirect)
async function handleCallback() {
    const settings = await getOAuthSettings();

    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const state = urlParams.get('state');

    if (code) {
        // 5. Retrieve stored code verifier
        const codeVerifier = sessionStorage.getItem('code_verifier');

        // 6. Exchange code for tokens
        const tokenParams = new URLSearchParams({
            client_id: settings.client_id,
            grant_type: 'authorization_code',
            code: code,
            redirect_uri: 'http://openig.example.org:8080/oauth/callback',
            code_verifier: codeVerifier
        });

        try {
            const response = await fetch(settings.toktoken_url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: tokenParams
            });

            const tokenData = await response.json();
            console.log('Access Token:', tokenData.access_token);
            // Handle tokens (store securely, etc.)
        } catch (error) {
            console.error('Token exchange failed:', error);
        }
    }
}

// Usage example
document.getElementById('loginButton')?.addEventListener('click', startPKCEFlow);

// Check if this is the callback
if (window.location.search.includes('code=')) {
    handleCallback();
}