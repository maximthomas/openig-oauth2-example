// Usage example
document.getElementById('loginButton')?.addEventListener('click', startPKCEFlow);
// document.getElementById('loginButton')?.addEventListener('click', function() {
//     successCallback("test_token");
// });

async function successCallback(token) {
    document.getElementById('login').style.display = 'none';
    
    document.getElementById('profile').style.display = '';   

    const profileData = await fetchProfileData(token);

    if(!profileData) {
        const alert = document.getElementById('alert');
        alert.style.display = '';
        alert.textContent = "Error fetching profile data";
        return;
    }

    document.getElementById('email').textContent = profileData.email;
}

// Check if this is the callback
if (window.location.search.includes('code=')) {
    handleCallback(successCallback);
}