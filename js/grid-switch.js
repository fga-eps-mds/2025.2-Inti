// grid-switch.js
// Handle view buttons in .grid-products: toggle active, animate, and fetch posts/products
(function(){
  const backendBase = 'https://20252-inti-production.up.railway.app';

  function isSameView(btn){
    // consider current active view
    const active = document.querySelector('.grid-products .view-btn.active');
    return active === btn;
  }

  async function fetchAndPopulate(url, viewType){
      
      console.log(`Fetching ${viewType} from:`, backendBase + url);
      console.log('Headers:', headers);
      
      const res = await fetch(backendBase + url);
      
      if (!res.ok) {
        const errorText = await res.text();
        console.error('Erro na resposta:', errorText);
        throw new Error(`fetch error ${res.status}: ${errorText}`);
      }
      
      const json = await res.json();
      console.log(`Dados recebidos (${viewType}):`, json);
      
      // Determinar qual função de renderização usar baseado no viewType
      if (viewType === 'posts') {
        // Buscar posts no JSON (pode vir como array direto ou dentro de propriedade)
        const posts = json.posts || json.data || json || [];
        
        if (typeof window.populateUserPosts === 'function'){
          window.populateUserPosts(posts);
        } else {
          console.warn('populateUserPosts não está disponível');
        }
      } else if (viewType === 'products') {
        // Buscar produtos no JSON
        const products = json.products || json.data || json || [];
        
        if (typeof window.populateUserProducts === 'function'){
          window.populateUserProducts(products);
        } else {
          console.warn('populateUserProducts não está disponível - usando renderização padrão');
          // Fallback: usar a mesma função de posts
          if (typeof window.populateUserPosts === 'function'){
            window.populateUserPosts(products);
          }
        }
      }
  }

  function showFetchError(message) {
    const postsGrid = document.querySelector('.user-posts-grid');
    if (postsGrid) {
      postsGrid.innerHTML = `<p class="no-posts">Erro ao carregar dados: ${message}</p>`;
    }
  }

  function addHandlers(){
    const container = document.querySelector('.grid-products');
    if (!container) return;
    
    const buttons = Array.from(container.querySelectorAll('.view-btn'));
    
    buttons.forEach(btn => {
      btn.addEventListener('click', async (e) => {
        e.preventDefault();
        if (isSameView(btn)) return; // já ativo => nada a fazer

        const current = container.querySelector('.view-btn.active');
        if (current) current.classList.remove('active');

        // add active + animating for quick effect
        btn.classList.add('active','animating');
        // small visual feedback
        setTimeout(() => btn.classList.remove('animating'), 300);

        // fetch data for this view if data-url provided
        const url = btn.dataset.url;
        const viewType = btn.dataset.view; // 'posts' ou 'products'
        
        if (url && viewType){
          await fetchAndPopulate(url, viewType);
        }
      });
    });
  }

  // init on DOM ready
  document.addEventListener('DOMContentLoaded', addHandlers);
})();