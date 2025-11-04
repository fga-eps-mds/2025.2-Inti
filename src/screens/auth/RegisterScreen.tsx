import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, TouchableOpacity, Alert, Pressable } from 'react-native';
import { AuthScreenProps } from '../../@types/navigation';
import { useAuth } from '../../hooks/useAuth';
import Ionicons from 'react-native-vector-icons/Ionicons';
import FontAwesome from 'react-native-vector-icons/FontAwesome';

export default function RegisterScreen({ navigation }: AuthScreenProps<'Register'>) {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const { signIn } = useAuth();
    const [accountType, setAccountType] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const hasNumber = /\d/;
    const hasUpper = /[A-Z]/;




    const handleRegister = async () => {
        if (!email || !password || !confirmPassword || !username) {
            Alert.alert('Erro', 'Preencha todos os campos.');
            return;
        }

        if (!emailRegex.test(email.trim())) {
            Alert.alert('Erro', 'Por favor insira um email válido.');
            return;
        }

        if (!hasNumber.test(password)) {
            Alert.alert('Erro', 'A senha deve conter um número.');
            return;
        }

        if (!hasUpper.test(password)) {
            Alert.alert('Erro', 'A senha deve conter uma letra maiúscula.');
            return;
        }

        if (password !== confirmPassword) {
            Alert.alert('Erro', 'As senhas não coincidem.');
            return;
        }

        try {
            Alert.alert('Sucesso', 'Conta criada!');
            await signIn('token-falso-de-cadastro');
        } catch (error) {
            Alert.alert('Erro no Cadastro', 'Não foi possível criar a conta.');
        }
    };

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Cadastro</Text>

            <View style={styles.form}>
                <Text style={styles.titileInput}>Nome</Text>
                <TextInput
                    style={[
                        styles.input,
                    ]}
                    placeholder="Ex: Musa UnB"
                    value={name}
                    placeholderTextColor={'#592E838C'}
                    onChangeText={setName}
                    autoCapitalize="none"
                />
                <Text style={styles.titileInput}>Username</Text>
                <TextInput
                    style={[
                        styles.input,
                    ]}
                    placeholder="Ex: musa123"
                    value={username}
                    placeholderTextColor={'#592E838C'}
                    onChangeText={setUsername}
                    autoCapitalize="none"
                />
                <Text style={styles.titileInput}>Email</Text>
                <TextInput
                    style={styles.input}
                    placeholder="Ex: musa@aluno.unb.br"
                    value={email}
                    onChangeText={setEmail}
                    placeholderTextColor={'#592E838C'}
                    keyboardType="email-address"
                    autoCapitalize="none"
                />
                <Text style={styles.titileInput}>Senha</Text>
                <TextInput
                    style={styles.input}
                    placeholder="Senha"
                    value={password}
                    placeholderTextColor={'#592E838C'}
                    onChangeText={setPassword}
                    secureTextEntry={!showPassword}
                />
                <Text style={styles.titileInput}>Confirmar senha</Text>
                <TextInput
                    style={styles.input}
                    placeholder="Confirmar Senha"
                    value={confirmPassword}
                    placeholderTextColor={'#592E838C'}
                    onChangeText={setConfirmPassword}
                    secureTextEntry={!showPassword}
                />
            </View>

            <View style={styles.containerCheckbox}>
                <View style={styles.inputCheckbox}>
                    <Pressable
                        style={[
                            styles.checkbox,
                            showPassword && styles.checkboxChecked
                        ]}
                        onPress={() => setShowPassword(prev => !prev)}
                    >
                        {showPassword && (
                            <FontAwesome name="check" size={16} color="#fff" />
                        )}
                    </Pressable>

                    <Text style={styles.textCheckbox}>Mostrar senha</Text>
                </View>

                <View style={styles.inputCheckbox}>
                    <Pressable
                        style={[
                            styles.checkbox,
                            accountType && styles.checkboxChecked
                        ]}
                        onPress={() => setAccountType(!accountType)}
                    >
                        {accountType && (
                            <FontAwesome name='check' size={16} color="#fff"/>
                        )}
                    </Pressable>

                    <Text style={styles.textCheckbox}>Sua conta é organizacional?</Text>
                </View>
            </View>

            <TouchableOpacity style={styles.buttom} onPress={handleRegister}>
                <Text style={styles.textButtom}>Criar Conta</Text>
            </TouchableOpacity>


            <TouchableOpacity onPress={() => navigation.navigate('Login')}>
                <Text style={styles.linkText}>Fazer Login</Text>
            </TouchableOpacity>


        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'center',
        padding: 30,
        backgroundColor: '#fff',
        paddingTop: 80,
    },
    title: {
        fontSize: 52,
        fontWeight: 'bold',
        marginBottom: 20,
        color: '#592E83',
        alignItems: 'flex-start'

    },
    form: {
        width: '100%'
    },
    input: {
        width: '100%',
        height: 50,
        borderColor: 'gray',
        borderRadius: 11,
        paddingHorizontal: 20,
        marginBottom: 16,
        borderWidth: undefined,
        backgroundColor: '#F2EBFB',
        color: '#592E83'

    },
    titileInput: {
        color: '#592E83',
        fontSize: 18,
        marginLeft: 10,
    },

    inputFocused: {
        borderWidth: 1,
        borderColor: '#592E83',
    },

    linkText: {
        color: '#592E83',
        marginTop: 20,
        fontWeight: 'bold',
        fontSize: 24,
    },

    inputCheckbox: {
        width: '100%',
        flexDirection: 'row',
        alignContent: 'center',
        alignItems: 'center',
    },

    containerCheckbox: {
        width: '100%',
        gap: 12,
        marginTop: 20
    },

    checkbox: {
        width: 24,
        height: 24,
        borderRadius: 4,
        alignItems: "center",
        backgroundColor: '#F2EBFB',

    },
    checkboxChecked: {
        backgroundColor: '#592E83',
        borderColor: '#F2EBFB',
        alignItems: 'center',
        justifyContent: 'center',
    },

    textCheckbox: {
        fontSize: 18,
        color: '#592E83',
        marginLeft: 10,

    },

    buttom: {
        width: 270,
        height: 50,
        backgroundColor: '#F2EBFB',
        alignItems: 'center',
        justifyContent: 'center',
        marginTop: 36,
        borderRadius: 11,
    },

    textButtom: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#592E83'
    },
});
