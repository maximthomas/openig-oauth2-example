// Usage example
window.onload = function () {
    
    function setError(text) {
        const alert =  document.getElementById('alert');
        alert.textContent = text;
        alert.style.display = '';
    }

    document.getElementById('loginButton')?.addEventListener('click', doLogin);

    async function setUserData(userData) {
        document.getElementById('login').style.display = 'none';
        
        document.getElementById('email').textContent = userData.email;
        
        document.getElementById('profile').style.display = '';
    }

    async function getUserData(accessToken) {
        try {
            const response = await fetch("/userinfo", {
                headers: {
                    "Authorization": "Bearer " + accessToken,
                  },
            });
            if(response.ok) {
                const userData = await response.json();
                console.log('User data:', userData);
                setUserData(userData);
            } else if (response.status == 403) {
                setError("got http status 403 Forbidden");
            } else if (response.status == 401) {
                setError("got http status 401 Not Authenticated");
            } else {
                setError("got http status " + response.status + " " + response.statusText);
            }
            
        } catch (error) {
            console.error('get user data failed:', error);
            setError("get user data failed: " + error);
        }

    }

    async function doLogin() {
        try {

            const response = await fetch("/oauth?goto=/app", {
                redirect: 'manual'
            });
            if (response.type === "opaqueredirect") {
                window.sessionStorage.setItem("autoLogin", true);
                window.location.href = response.url;
                return;
            }
            const tokenData = await response.json();
            const accessToken = tokenData.access_token;
            console.log('Access Token:', accessToken);
            getUserData(accessToken)

        } catch (error) {
            console.error('Token exchange failed:', error);
            setError('Token exchange failed:' + error);
        }
    }

    if(window.sessionStorage.getItem("autoLogin")) {
        window.sessionStorage.removeItem("autoLogin");
        doLogin();
    }

}
