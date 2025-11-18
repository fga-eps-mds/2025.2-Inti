async function fetchUserData() {

    try {

        const response = await fetch('https://20252-inti-production.up.railway.app/', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            throw new Error(`Erro HTTP:  ${response.status}`);
        }

        const userData = await response.json();
        return userData;
    }


    catch (error) {
        console.log('Erro na requisição:', error.message);
        throw error;
    }
}

function populateForms(userData) {
    document.getElementById('nome-input').value = userData.name || '';
    document.getElementById('email-input').value = userData.email || '';
    document.getElementById('bio-input').value = userData.bio || '';
    //document.getElementById('telefone-input').value = userData.telefone || '';
}

