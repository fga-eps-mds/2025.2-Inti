const API_CONFIG = {
    token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJub3ZvdCIsImV4cCI6MTc2NjA2MDc4MH0.VNz3bRuSpHtehwAIPPVVC3cJgxozc96qa7n82I7NiLs',
    baseURL: 'https://20252-inti-production.up.railway.app/'
};

fetchUserData();

async function fetchUserData() {
    
    try {
        const response = await fetch(`${API_CONFIG.baseURL}profile/me?size=5&page=5`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${API_CONFIG.token}`,
                'Content-Type': 'application/json'
            }
        });

        console.log(response);
        if (!response.ok) {
            throw new Error(`Erro HTTP:  ${response.status}`);
        }

        const userData = await response.json();
        populateForms(userData);
        console.log(`${userData.email}`);
        return userData;
    }


    catch (error) {
        console.log('Erro na requisição:', error.message);
        throw error;
    }
}

function populateForms(userData) {
    document.getElementById('nome-input').value = userData.name || '';
    //document.getElementById('email-input').value = userData.email || '';
    document.getElementById('bio-input').value = userData.bio || '';
    //document.getElementById('telefone-input').value = userData.telefone || '';
}


async function editProfile() {
    try {
        const response = await fetch('http://localhost:8080/profile/')
    } catch (error) {
        
    }
}
