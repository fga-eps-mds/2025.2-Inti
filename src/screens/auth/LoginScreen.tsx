import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, TouchableOpacity, Alert, Pressable } from 'react-native';
import { useAuth } from '../../hooks/useAuth';
import { AuthScreenProps } from '../../@types/navigation';

export default function LoginScreen({ navigation }: AuthScreenProps<'Login'>) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { signIn } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const hasNumber = /\d/;
  const hasUpper = /[A-Z]/;




  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert('Erro', 'Preencha todos os campos.');
      return;
    }
    
    if (!emailRegex.test(email.trim())){ 
      Alert.alert('Erro', 'Por favor insira um email válido.');
      return;
    }

    if (!hasNumber.test(password)){
          Alert.alert('Erro', 'A senha deve conter um número.');
          return;
        }

    if (!hasUpper.test(password)){
          Alert.alert('Erro', 'A senha deve conter uma letra maiúscula.');
          return;
    }

    try {
      await signIn('token-falso-de-login');
    } catch (error) {
      Alert.alert('Erro no Login', 'Email ou senha inválidos.');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Fazer Login</Text>
      
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
        secureTextEntry
      />
      
    <View style={styles.inputCheckbox}>
        <Pressable
            style={[
                styles.checkbox,
                showPassword && styles.checkboxChecked
            ]}
            onPress={() => {setShowPassword(!showPassword)
            console.log('showPassword:', !showPassword);

            }}
        />
        <Text>  Mostrar senha</Text>
      </View>

      <Button title="Entrar" onPress={handleLogin} />

      <TouchableOpacity onPress={() => navigation.navigate('Register')}>
        <Text style={styles.linkText}>Ainda não possui uma conta? Fazer Cadastro</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  input: {
    width: '100%',
    height: 50,
    borderColor: 'gray',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 10,
    marginBottom: 15,
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
});
