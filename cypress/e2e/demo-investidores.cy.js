describe('🎬 MUSA Demo - Apresentação para Investidores', () => {

    // Login apenas uma vez no início
    before(() => {
        cy.log('🔐 Fazendo login inicial...');
        cy.login();
        cy.url().should('include', '/pages/home.html');
        cy.wait(3000); // Aguarda 3 segundos após login
    });

    it('1️⃣ Visualizar Feed de Posts', () => {
        cy.log('📱 DEMO: Explorando o feed principal');

        // Já estamos logados e na home
        cy.get('.events-grid').should('exist');
        cy.wait(4000); // 4 segundos para ver o feed carregar

        // Verifica se há posts
        cy.get('.post').should('have.length.greaterThan', 0);
        cy.wait(3000); // 3 segundos para visualizar posts

        // Scroll suave para ver mais posts
        cy.window().then((win) => {
            win.scrollTo({ top: 300, behavior: 'smooth' });
        });
        cy.wait(2000);
        cy.window().then((win) => {
            win.scrollTo({ top: 600, behavior: 'smooth' });
        });
        cy.wait(3000);
    });

    it('2️⃣ Curtir um Post', () => {
        cy.log('❤️ DEMO: Curtindo um post');

        // Volta ao topo
        cy.window().then((win) => {
            win.scrollTo({ top: 0, behavior: 'smooth' });
        });
        cy.wait(2000);

        // Encontra o primeiro botão de like e curte
        cy.get('.like-button').first().as('firstLike');
        cy.wait(2000); // Pausa antes de curtir

        cy.get('@firstLike').click();
        cy.wait(4000); // 4 segundos para ver o like

        // Descurte
        cy.log('💔 DEMO: Removendo like');
        cy.get('@firstLike').click();
        cy.wait(3000); // 3 segundos para ver o unlike
    });

    it('3️⃣ Abrir Detalhes de um Post', () => {
        cy.log('🔍 DEMO: Visualizando detalhes de um post');

        // Volta ao topo se necessário
        cy.window().then((win) => {
            win.scrollTo({ top: 0, behavior: 'smooth' });
        });
        cy.wait(2000);

        // Clica no primeiro post (na imagem, não no botão de like)
        cy.get('.post').first().find('.image-post-placeholder').click();
        cy.wait(4000); // 4 segundos na página de detalhes

        // Volta para o feed clicando no botão Home
        cy.log('⬅️ Voltando ao feed');
        cy.get('.navbar .nav-btn').eq(0).click();
        cy.wait(3000);
    });

    it('4️⃣ Navegar para Lista de Eventos', () => {
        cy.log('📅 DEMO: Explorando eventos disponíveis');

        cy.wait(2000);

        // Clica no dropdown de eventos
        cy.get('#dropdownToggle').click();
        cy.wait(1000);
        cy.get('#eventosBtn').click();
        cy.wait(4000); // 4 segundos para ver a lista de eventos

        // Verifica se está na página de eventos
        cy.url().should('include', 'eventList.html');
        cy.wait(3000);
    });

    it('5️⃣ Visualizar Detalhes de um Evento', () => {
        cy.log('🎉 DEMO: Abrindo detalhes do evento');

        cy.wait(2000);

        // Clica no primeiro evento da lista
        cy.get('.event').first().click();
        cy.wait(5000); // 5 segundos para ver os detalhes do evento

        // Verifica se está na página de detalhes
        cy.url().should('include', '/pages/event-detail.html');
        cy.wait(2000);
    });

    it('6️⃣ Ver Participantes do Evento', () => {
        cy.log('👥 DEMO: Visualizando quem confirmou presença');

        // Scroll suave até a seção de participantes
        cy.window().then((win) => {
            win.scrollTo({ top: win.document.body.scrollHeight, behavior: 'smooth' });
        });
        cy.wait(4000); // 4 segundos para ver os participantes

        // Verifica que estamos na página
        cy.get('body').should('exist');
        cy.wait(3000);
    });

    it('7️⃣ Confirmar Presença no Evento', () => {
        cy.log('✅ DEMO: Confirmando presença');

        // Scroll de volta para o botão
        cy.window().then((win) => {
            win.scrollTo({ top: 0, behavior: 'smooth' });
        });
        cy.wait(2000);

        // Clica no botão de confirmar presença
        cy.get('#btn-confirm').click();
        cy.wait(5000); // 5 segundos para ver a confirmação

        // Cancela a presença
        cy.log('❌ DEMO: Cancelando presença');
        cy.get('#btn-confirm').click();
        cy.wait(4000);
    });

    it('8️⃣ Navegar para Perfil', () => {
        cy.log('👤 DEMO: Acessando perfil do usuário');

        cy.wait(2000);

        // Clica no botão de perfil (5º botão da navbar)
        cy.get('.navbar .nav-btn').eq(4).click();
        cy.wait(5000); // 5 segundos para ver o perfil

        // Verifica se está na página de perfil
        cy.url().should('include', '/pages/profile.html');
        cy.wait(3000);
    });

    it('9️⃣ Visualizar Produtos no Perfil', () => {
        cy.log('🛍️ DEMO: Explorando produtos cadastrados');

        cy.wait(2000);

        // Clica na aba de produtos
        cy.get('.product-btn').click();
        cy.wait(5000); // 5 segundos para ver os produtos

        // Scroll para ver mais produtos
        cy.window().then((win) => {
            win.scrollTo({ top: win.document.body.scrollHeight, behavior: 'smooth' });
        });
        cy.wait(3000);
    });

    it('🔟 Buscar Usuários', () => {
        cy.log('🔍 DEMO: Buscando outros usuários');

        cy.wait(2000);

        // Clica no botão de busca (2º botão da navbar)
        cy.get('.navbar .nav-btn').eq(1).click();
        cy.wait(4000); // 4 segundos na página de busca

        // Verifica se está na página de busca
        cy.url().should('include', '/pages/search.html');
        cy.wait(3000);

        cy.log('✨ DEMO: Apresentação concluída!');
    });

});
