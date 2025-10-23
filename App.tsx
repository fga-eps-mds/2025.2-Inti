import React, { useEffect, useState } from 'react';
import { NavigationContainer, LinkingOptions } from '@react-navigation/native';
import { AuthProvider } from './src/contexts/AuthContext';
import { useAuth } from './src/hooks/useAuth';
import SplashScreen from './src/screens/SplashScreen';
import AuthNavigator from './src/navigation/AuthNavigator';
import AppNavigator from './src/navigation/AppNavigator';
import { View, ActivityIndicator, StyleSheet } from 'react-native';
// @types
import { AuthStackParamList, AppStackParamList } from './src/@types/navigation';
type RootStackParamList = AuthStackParamList & AppStackParamList;



const linkingConfig: LinkingOptions<RootStackParamList> = {
  prefixes: ['musa://'],
  config: {
    // mapeando os links das telas
    screens: {
      // AuthNavigator (AuthStackParamList)
      Register: 'register',
      Login: 'login',
      
      // AppNavigator (AppStackParamList)
      MainTabs: {
        screens: {
          Home: 'home',
          Pesquisar: 'pesquisar',
          Eventos: 'eventos',
          Perfil: 'perfil',
          NewPost: 'new-post', // consistência com @types/navigation.d.ts
        },
      },
      Notifications: 'notifications',
    },
  },
};

function RootNavigator() {
  const { token, isLoading: isAuthLoading } = useAuth();
  const [isAppLoading, setIsAppLoading] = useState(true);


  useEffect(() => {
    setTimeout(() => {
      setIsAppLoading(false);
      // react-native-splash-screen (nativo) o .hide() aqui
    }, 1500); 
  }, []);


  if (isAppLoading || isAuthLoading) {
    return <SplashScreen />;
  }

  // <></> (Fragmento) necessário
  return (
    <>
      {token == null ? (
        <AuthNavigator />
      ) : (
        <AppNavigator />
      )}
    </>
  );
}


export default function App() {
  return (
    <AuthProvider> // o AuthProvider envolve tudo pois passa diretamente o estado do usuário para todos os componentes dentro
      <NavigationContainer<RootStackParamList> // após isso vem o de navegação
        linking={linkingConfig}
        fallback={
          <View style={styles.loading}>
            <ActivityIndicator size="large" />
          </View>
        }
      >
        <RootNavigator />
      </NavigationContainer>
    </AuthProvider>
  );
}

const styles = StyleSheet.create({
  loading: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

