// Mapeamento dos tipos de componentes de endereço da API para os IDs do seu formulário
const addressMap = {
    'street_number': ['street_address', 'long_name'],
    'route': ['street_address', 'long_name'],
    'locality': ['city', 'long_name'],
    'administrative_area_level_1': ['state', 'short_name']
};

function initAutocomplete() {
    const input = document.getElementById('localizacao_autocomplete');
    
    // Inicializa o serviço Autocomplete no input
    const autocomplete = new google.maps.places.Autocomplete(input, {
        types: ['geocode'], 
        componentRestrictions: {'country': ['br']} 
    });

    // Adiciona o listener para quando o usuário SELECIONA um local
    autocomplete.addListener('place_changed', function() {
        const place = autocomplete.getPlace();

        if (!place.geometry) {
            console.error("Localização não encontrada.");
            alert("Localização não encontrada. Por favor, selecione uma sugestão da lista.");
            return;
        }

        // --- 1. COLETA E LIMPEZA INICIAL ---
        
        let street = '';
        let streetNumber = '';
        const collectedData = {}; // Objeto para armazenar os dados e enviar para o console

        // 2. Preenche os campos de Endereço (Rua, Cidade, Estado)
        for (let component of place.address_components) {
            for (let type of component.types) {
                if (addressMap[type]) {
                    const [inputId, typeName] = addressMap[type];
                    const value = component[typeName];

                    if (type === 'route') {
                        street = value;
                    } else if (type === 'street_number') {
                        streetNumber = value;
                    } else if (inputId) {
                         // Salva Cidade e Estado no objeto de dados
                        collectedData[inputId] = value;
                        document.getElementById(inputId).value = value;
                    }
                }
            }
        }
        
        // Combina Rua e Número
        const fullStreetAddress = street + (streetNumber ? ', ' + streetNumber : '');
        collectedData['street_address'] = fullStreetAddress;
        document.getElementById('street_address').value = fullStreetAddress;

        // 3. Preenche as Coordenadas
        const lat = place.geometry.location.lat();
        const lng = place.geometry.location.lng();
        
        collectedData['latitude'] = lat;
        collectedData['longitude'] = lng;
        
        document.getElementById('latitude').value = lat;
        document.getElementById('longitude').value = lng;
        
        // --- 4. EXIBIÇÃO NO CONSOLE (O QUE VOCÊ PEDIU) ---
        console.groupCollapsed("📍 Dados da Localização Coletados");
        console.log("Endereço Completo (Display):", place.formatted_address);
        console.table(collectedData); // Exibe os dados de forma legível
        console.groupEnd();
        
        // --- 5. OS DADOS ESTÃO SALVOS NOS CAMPOS HIDDEN E PRONTOS PARA O ENVIO DO FORMULÁRIO ---
    });
}