// ***********************************************
// Custom commands for MUSA demo
// ***********************************************

// Comando para fazer login
Cypress.Commands.add('login', (email, password) => {
    cy.visit('/index.html');
    cy.get('input[type="email"]').clear().type(email || Cypress.env('email'));
    cy.get('#loginPassword').clear().type(password || Cypress.env('password'));
    cy.get('button[type="submit"]').click();
    cy.wait(2000); // Aguarda redirecionamento
});
