// Usage example
window.onload = function() {
    
    document.getElementById('loginButton')?.addEventListener('click', doLogin);

    function getUserData(accessToken) {

    }

    async function doLogin(params) {
        try {
            // const response = await axios.get("/oauth");
            // console.log(response);

            const response = await fetch("/oauth", {
                redirect: 'manual'
            });
            if (response.type === "opaqueredirect") {
                window.location.href = response.url;
                return;
            }
            const tokenData = await response.json();
            console.log('Access Token:', tokenData.access_token);
            getUserData(tokenData.accessToken)
                        
        } catch (error) {
            console.error('Token exchange failed:', error);
        }
    }
}
