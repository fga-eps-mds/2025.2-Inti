const API_CONFIG = {
  token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJuYXRhbjg2NDMiLCJleHAiOjE3NjYzMzE1NzJ9.5RCNVk9mQBj2buXY-uMDXKQ34_nZ4loD3oE6flLSPos",
  baseURL: "https://20252-inti-production.up.railway.app",
  urlLocal: "http://localhost:8080",
};

let selectedImageFile = null;

document.addEventListener('DOMContentLoaded', function () {
  fetchUserData();
  document.querySelector('.botao-edit').addEventListener('click', handleProfileUpdate);
  document.getElementById('avatar-upload').addEventListener('change', function (event) {
    selectedImageFile = event.target.files[0];
    if (selectedImageFile) {
      const reader = new FileReader();
      reader.onload = (e) => document.querySelector('.icon-user').src = e.target.result;
      reader.readAsDataURL(selectedImageFile);
    }
  });
});


async function handleProfileUpdate(e) {
  e.preventDefault();

  const originalData = {
    name: document.getElementById('nome-input').getAttribute('data-original') || '',
    email: document.getElementById('email-input').getAttribute('data-original') || '',
    telefone: document.getElementById('telefone-input').getAttribute('data-original') || '',
    bio: document.getElementById('bio-input').getAttribute('data-original') || '',
    username: document.getElementById('username-input').getAttribute('data-original') || ''
  };

  const currentData = {
    name: document.getElementById('nome-input').value,
    email: document.getElementById('email-input').value,
    telefone: document.getElementById('telefone-input').value,
    bio: document.getElementById('bio-input').value,
    username: document.getElementById('username-input').value
  };

  const userData = {};
  
  if (currentData.name !== originalData.name) userData.name = currentData.name;
  if (currentData.email !== originalData.email) userData.email = currentData.email;
  if (currentData.telefone !== originalData.telefone) userData.telefone = currentData.telefone;
  if (currentData.bio !== originalData.bio) userData.bio = currentData.bio;
  if (currentData.username !== originalData.username) userData.username = currentData.username;
  
  if (selectedImageFile) {
    userData.picture = selectedImageFile;
  }

  if (Object.keys(userData).length === 0 && !selectedImageFile) {
    alert('Nenhuma alteração foi feita!');
    return;
  }

  try {
    const success = await editProfile(userData);

    if (success) {
      alert('Perfil atualizado com sucesso!');
      updateOriginalData(currentData);
    }

    else {
      alert('Erro ao atualizar o perfil. Tente novamente');
    }

  } catch (error) {
    console.log('Erro: ', error);
    alert('Erro ao atualizar perfil');
  }
}


async function fetchUserData() {
  try {
    const response = await fetch(
      `${API_CONFIG.baseURL}/profile/me?size=5&page=5`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${API_CONFIG.token}`,
          "Content-Type": "application/json",
        },
      }
    );

    console.log(response);
    if (!response.ok) {
      throw new Error(`Erro HTTP:  ${response.status}`);
    }

    const userData = await response.json();
    populateForms(userData);
    console.log(`${userData.email}`);
    console.log('profilePicture existe?:', userData.profile_picture_url);
    if (userData.profile_picture_url) {
      const imageUrl = userData.profile_picture_url.startsWith('http')
        ? userData.profile_picture_url
        : `${API_CONFIG.baseURL}${userData.profile_picture_url}`;

      console.log('URL da imagem:', imageUrl);
      loadProfileImage(imageUrl);
    }

    return userData;
  } catch (error) {
    console.log("Erro na requisição:", error.message);
    throw error;
  }
}

function populateForms(userData) {
  document.getElementById("username-input").value = userData.username || "";
  document.getElementById("username-input").setAttribute('data-original', userData.username || "");
  
  document.getElementById("nome-input").value = userData.name || "";
  document.getElementById("nome-input").setAttribute('data-original', userData.name || "");
  
  document.getElementById("email-input").value = userData.email || "";
  document.getElementById("email-input").setAttribute('data-original', userData.email || "");
  
  document.getElementById("bio-input").value = userData.bio || "";
  document.getElementById("bio-input").setAttribute('data-original', userData.bio || "");
  
  document.getElementById("telefone-input").value = userData.telefone || "";
  document.getElementById("telefone-input").setAttribute('data-original', userData.telefone || "");
}

async function editProfile(userData) {
  try {
    const formData = new FormData();

    if (userData.name) formData.append("name", userData.name);
    if (userData.email) formData.append("publicEmail", userData.email);
    if (userData.bio) formData.append("userBio", userData.bio);
    if (userData.telefone) formData.append("phone", userData.telefone);
    if (userData.username) formData.append("username", userData.username);
    if (userData.picture) formData.append("profilePicture", userData.picture);

    const response = await fetch(`${API_CONFIG.baseURL}/profile/update`, {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${API_CONFIG.token}`,
      },
      body: formData,
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.log('Mensagem de erro do servidor:', errorText);
      throw new Error(`Erro HTTP ${response.status}: ${errorText}`);
    }

    if (response.status === 201) {
      console.log("Perfil atualizado com sucesso!");
      return true;
    } else {
      throw new Error(`Erro HTTP : ${response.status}`);
    }
  } catch (error) {
    console.log("Erro ao atualizar perfil: ", error);
    return false;
  }
}

function loadProfileImage(imageUrl) {
  const iconUser = document.querySelector('.icon-user');

  if (imageUrl) {
    iconUser.src = imageUrl;
    iconUser.alt = "Foto de perfil";
  }

  else {
    iconUser.src = "../assets/img_user.svg";
    iconUser.alt = "Selecionar foto";
  }
}

function updateOriginalData(currentData) {
  document.getElementById("username-input").setAttribute('data-original', currentData.username || "");
  document.getElementById("nome-input").setAttribute('data-original', currentData.name || "");
  document.getElementById("email-input").setAttribute('data-original', currentData.email || "");
  document.getElementById("bio-input").setAttribute('data-original', currentData.bio || "");
  document.getElementById("telefone-input").setAttribute('data-original', currentData.telefone || "");
}
