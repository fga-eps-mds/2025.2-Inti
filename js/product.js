// Importa a URL base da API
import { API_BASE_URL } from './config.js';

/**
 * Função para criar um novo anúncio de produto/serviço.
 * @param {object} productData - Dados do produto (title, description, price, imgLink, contactInfo).
 * @returns {Promise<object>} - O objeto de resposta da API.
 */
export async function createProduct(productData) {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        throw new Error("Usuário não autenticado. Faça login para criar um anúncio.");
    }

    try {
        const response = await fetch(`${API_BASE_URL}/products`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(productData)
        });

        const data = await response.json();

        if (!response.ok) {
            // Lança um erro com a mensagem de erro da API
            throw new Error(data.message || `Erro ao criar anúncio: ${response.statusText}`);
        }

        return data;
    } catch (error) {
        console.error('Erro na requisição de criação de produto:', error);
        throw error;
    }
}

/**
 * Função para listar todos os anúncios de produtos/serviços.
 * @returns {Promise<Array<object>>} - Lista de anúncios.
 */
export async function listProducts() {
    try {
        const response = await fetch(`${API_BASE_URL}/products`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
                // Não precisa de token para listagem pública
            }
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || `Erro ao listar anúncios: ${response.statusText}`);
        }

        return data;
    } catch (error) {
        console.error('Erro na requisição de listagem de produtos:', error);
        throw error;
    }
}
