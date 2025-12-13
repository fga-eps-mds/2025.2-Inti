const { defineConfig } = require('cypress');

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://127.0.0.1:5500',
        viewportWidth: 390,
        viewportHeight: 844,
        video: true,
        videoCompression: 32,
        screenshotOnRunFailure: true,
        defaultCommandTimeout: 10000,
        pageLoadTimeout: 30000,
        watchForFileChanges: false,
        chromeWebSecurity: false,
        env: {
            email: 'a@a.com',
            password: '123456'
        },
        setupNodeEvents(on, config) {
            // implement node event listeners here
        },
    },
});
