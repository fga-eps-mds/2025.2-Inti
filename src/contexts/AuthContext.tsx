import React, { createContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

// definir o formato dos dados
interface AuthContextData {
  token: string | null;
  isLoading: boolean;
  signIn(token: string): Promise<void>;
  signOut(): Promise<void>;
}


interface AuthProviderProps {
  children: ReactNode;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);


export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadStorageData() {
      const storedToken = await AsyncStorage.getItem('@App:token');
      if (storedToken) {
        setToken(storedToken);
      }
      setIsLoading(false);
    }
    loadStorageData();
  }, []);

  async function signIn(apiToken: string) {
    await AsyncStorage.setItem('@App:token', apiToken);
    setToken(apiToken);
  }

  async function signOut() {
    await AsyncStorage.removeItem('@App:token');
    setToken(null);
  }

  return (
    <AuthContext.Provider value={{ token, isLoading, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
