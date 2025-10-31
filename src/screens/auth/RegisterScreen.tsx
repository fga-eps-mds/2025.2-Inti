import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, TouchableOpacity, Alert, Pressable } from 'react-native';
import { AuthScreenProps } from '../../@types/navigation';
import { useAuth } from '../../hooks/useAuth';

export default function RegisterScreen({ navigation }: AuthScreenProps<'Register'>) {
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

            {/* <View style={styles.form}> */}
                <Text>Username</Text>
                <TextInput
                    style={[
                        styles.input,
                    ]}
                    placeholder="Ex: Musa UnB"
                    value={username}
                    onChangeText={setUsername}
                    autoCapitalize="none"
                />

                <TextInput
                    style={styles.input}
                    placeholder="Email"
                    value={email}
                    onChangeText={setEmail}
                    keyboardType="email-address"
                    autoCapitalize="none"
                />
                <TextInput
                    style={styles.input}
                    placeholder="Senha"
                    value={password}
                    onChangeText={setPassword}
                    secureTextEntry={!showPassword}
                />
                <TextInput
                    style={styles.input}
                    placeholder="Confirmar Senha"
                    value={confirmPassword}
                    onChangeText={setConfirmPassword}
                    secureTextEntry={!showPassword}
                />
            {/* </View> */}

            <View style={styles.inputCheckbox}>
                <Pressable
                    style={[
                        styles.checkbox,
                        showPassword && styles.checkboxChecked
                    ]}
                    onPress={() => {
                        setShowPassword(!showPassword)
                        // console.log('showPassword:', !showPassword);

                    }}
                />

                <Text style={styles.textCheckbox}>Mostrar senha</Text>
            </View>

            <View style={styles.inputCheckbox}>
                <Pressable
                    style={[
                        styles.checkbox,
                        accountType && styles.checkboxChecked
                    ]}
                    onPress={() => setAccountType(!accountType)}
                />
                <Text style={styles.textCheckbox}>Sua conta é organizacional?</Text>
            </View>


            <Button title="Cadastrar" onPress={handleRegister} />

            <TouchableOpacity onPress={() => navigation.navigate('Login')}>
                <Text style={styles.linkText}>Já tem uma conta? Fazer Login</Text>
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
        paddingTop: 104,
    },
    title: {
        fontSize: 48,
        fontWeight: 'bold',
        marginBottom: 20,
        color: '#592E83',
        alignItems: 'flex-start'

    },
    // form: {
        
    // },
    input: {
        width: '100%',
        height: 50,
        borderColor: 'gray',
        borderRadius: 11,
        paddingHorizontal: 10,
        marginBottom: 15,
        borderWidth: undefined,
        backgroundColor: '#F2EBFB',
        paddingLeft: 20,
    },

    inputFocused: {
        borderWidth: 1,
        borderColor: '#592E83',
    },

    linkText: {
        color: '#6200EE',
        marginTop: 20,
    },

    inputCheckbox: {
        width: '100%',
        flexDirection: 'row',
        alignContent: 'center',
        alignItems: 'center',
    },

    checkbox: {
        width: 20,
        height: 20,
        borderRadius: 2,
        borderWidth: 2,
        borderColor: '#6200EE',
        alignItems: "center",
        backgroundColor: '#F2EBFB',

    },
    checkboxChecked: {
        backgroundColor: '#6200EE',
        borderColor: '#F2EBFB',

    },

    textCheckbox: {

    },
});
