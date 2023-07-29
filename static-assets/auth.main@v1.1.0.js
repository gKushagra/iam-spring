function isValidEmail(email) {
    return email.match(
        /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    );
}

function validateEmail(event, $result, $disableBtns) {
    let email = event.target.value;
    if (isValidEmail(email)) {
        $result.innerText = '';
        $disableBtns.forEach(b => b.disabled = false);
    } else {
        $result.innerText = 'Please enter a valid email!';
        $disableBtns.forEach(b => b.disabled = true);
    }
}

function emailValidationListener($emailInput, $errorMsgEl, $disableBtns = []) {
    $emailInput.addEventListener('input', function (event) {
        validateEmail(event, $errorMsgEl, $disableBtns);
    });
}

function isValidPassword(pw) {
    return /[A-Z]/.test(pw) &&
        /[a-z]/.test(pw) &&
        /[0-9]/.test(pw) &&
        /[^A-Za-z0-9]/.test(pw) &&
        pw.length > 6;
}

function validatePassword(event, $error, $progress, $disableBtns) {
    let pw = event.target.value;
    if (isValidPassword(pw)) {
        $error.innerText = '';
        $disableBtns.forEach(b => b.disabled = false);
        l = pw.length;
        if (l >= 6 && l < 11) {
            $progress.style.width = '25%';
            $progress.style.backgroundColor = '#FFD600';
        }
        if (l >= 11 && l < 16) {
            $progress.style.width = '75%';
            $progress.style.backgroundColor = '#64DD17';
        }
        if (l >= 16) {
            $progress.style.width = '100%';
            $progress.style.backgroundColor = '#00C853';
        }
    } else {
        $error.innerText =
            'Password should be atleast 6 characters and contain combination of upper & lower case alphabets, digits and special characters';
        $progress.style.width = '25%';
        $progress.style.backgroundColor = '#DD2C00';
        $disableBtns.forEach(b => b.disabled = true);
    }
}

function passwordValidationListener($passInput, $errorMsgEl, $strengthBarEl, $disableBtns = []) {
    $passInput.addEventListener('input', function (event) {
        validatePassword(event, $errorMsgEl, $strengthBarEl, $disableBtns);
    });
}

function matchPasswords($passwordInput, $retypePasswordInput, $passwordMatchErrorMsgEl, $disableBtns = []) {
    $retypePasswordInput.addEventListener('input', function (event) {
        if (event.target.value && event.target.value !== '' && event.target.value !== $passwordInput.value) {
            $passwordMatchErrorMsgEl.innerText = 'Oops, the passwords entered do not match! Please check!';
            $disableBtns.forEach(b => b.disabled = true);
        } else {
            $passwordMatchErrorMsgEl.innerText = '';
            $disableBtns.forEach(b => b.disabled = false);
        }
    });
}

function togglePassword($showPasswordBtn, $passwordInput) {
    $showPasswordBtn.addEventListener('click', function (event) {
        if ($passwordInput && $passwordInput.value) {
            let currType = $passwordInput.getAttribute('type');
            if (currType === 'password') {
                $passwordInput.setAttribute('type', 'text');
                event.target.innerText = 'hide password';
            } else {
                $passwordInput.setAttribute('type', 'password');
                event.target.innerText = 'show password';
            }
        }
    });
}

function footerStatement($el) {
    $el.innerHTML = `&#xA9; opensourcedit.com ${new Date().getFullYear()}`;
}

function redirectTo(id, attribute = 'data-value') {
    const redirectUri = document.getElementById(id)
        .getAttribute(attribute);
    const redirectLink = document.createElement('a');
    redirectLink.href = redirectUri;
    redirectLink.click();
}

function createSession(pageDataEl, redirectPageDataEl, redirectLoginDataEl, errorDataEl, redirectErrorEl) {
    const error = document.getElementById(errorDataEl).getAttribute('data-value');
    if (error === '404') {
        redirectTo(redirectErrorEl);
    } else {
        const token = new URLSearchParams(window.location.search).get('token');
        const page = document.getElementById(pageDataEl).getAttribute('data-value');
        if (!token || token === '' || !page || page === '') {
            localStorage.clear();
            redirectTo(redirectLoginDataEl);
        }
        localStorage.setItem('_page', page);
        localStorage.setItem('sf-token', token);
        redirectTo(redirectPageDataEl);
    }
}
