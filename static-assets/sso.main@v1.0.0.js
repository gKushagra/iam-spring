function redirectToLogin(redirectUri) {
    localStorage.clear();
    const link = document.createElement('a');
    link.href = `/auth/login?redirectUri=${redirectUri}`;
    link.click();
}

function verifyToken() {
    const redirectUri = `/auth/${localStorage.getItem('_page')}/callback`;
    const token = localStorage.getItem('sf-token');
    if (token && token !== '') {
        fetch(
            '/auth/verify?redirectUri=',
            { headers: { 'Authorization': `Bearer ${token}` } }
        )
            .then(function (response) {
                if (response.status === 401) {
                    redirectToLogin(redirectUri);
                } else {
                    return response.text();
                }
            })
            .then(function (data) {
                if (data !== 'Authorized') {
                    redirectToLogin(redirectUri);
                }
                fetch('/user/', { headers: { 'Authorization': `Bearer ${token}` } })
                    .then(function (userResponse) { return userResponse.json(); })
                    .then(function (data) { localStorage.setItem('_uid', data['userId']); })
                    .catch(function (error) { throw new Error(error); });
            })
            .catch(function (error) {
                console.error(error);
                redirectToLogin(redirectUri);
            });
    } else {
        console.error("Unauthorized");
        redirectToLogin(redirectUri);
    }
}
