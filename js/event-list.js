document.addEventListener('DOMContentLoaded', loadEvents);

const BASE_API = 'http://localhost:8080';
const BEARER_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJuYXRhbjg2NDMiLCJleHAiOjE3NjYzMzE1NzJ9.5RCNVk9mQBj2buXY-uMDXKQ34_nZ4loD3oE6flLSPos';

async function loadEvents() {
  const container = document.querySelector('.event-list');
  container.innerHTML = '<p>Carregando eventos...</p>';
  try {
    const res = await fetch(`${BASE_API}/event/lists`, {
      headers: {
        'Authorization': 'Bearer ' + BEARER_TOKEN,
        'Accept': 'application/json'
      }
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const data = await res.json();
    if (!Array.isArray(data)) {
      container.innerHTML = '<pre>Resposta inesperada: ' + JSON.stringify(data, null, 2) + '</pre>';
      return;
    }
    if (data.length === 0) {
      container.innerHTML = '<p>Nenhum evento encontrado.</p>';
      return;
    }
    container.innerHTML = '';
    data.forEach(event => {
      // Ajuste os campos conforme o seu backend
      const title = event.title || 'Evento';
      const imageUrl = event.imageUrl || event.image || `/images/${event.blobName}` || '';
      const date = event.eventTime || event.data || event.date || '';
      let formattedDate = '';
      if (date) {
        const d = new Date(date);
        if (!isNaN(d)) {
          // Formata como DD/MM (sem ano)
          formattedDate = d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
        } else {
          formattedDate = date;
        }
      }

      // Cria o card no mesmo modelo do seu HTML
      const eventDiv = document.createElement('div');
      eventDiv.className = 'event';

      const eventText = document.createElement('div');
      eventText.className = 'event-text';

      const h2Title = document.createElement('h2');
      h2Title.textContent = title;
      eventText.appendChild(h2Title);

      const h2Date = document.createElement('h2');
      h2Date.textContent = formattedDate;
      eventText.appendChild(h2Date);

      eventDiv.appendChild(eventText);

      const img = document.createElement('img');
      img.src = imageUrl.startsWith('http') ? imageUrl : `${BASE_API}${imageUrl}`;
      img.alt = title;
      eventDiv.appendChild(img);

      container.appendChild(eventDiv);
    });
  } catch (err) {
    container.innerHTML = '<pre>Erro ao carregar: ' + err.message + '</pre>';
    console.error(err);
  }
}