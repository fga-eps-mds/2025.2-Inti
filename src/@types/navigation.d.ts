/*
    
    Aqui se e escrito a tipagem do codigo.
    Se vc por exemplo, instala um modulo com 

        npm install modulo

    E voce utiliza ele por exemplo
    
        import * as unbCIS from 'modulo';

        const a = unbCIS('abc, 'def');

    e a logica do modulo esta toda em index.js:
        
        modulo.exports = function(um, dois) {
            // codigo
            return resultado;
        }

    voce pode declarar essa logica nesse arquivo *.d.ts para para ter a 
    tipagem do typescript:

        declare module 'modulo' {
            export default function nome(arg1: string, arg2: string): meuTipo;
        }

        interface meuTipo {
            algo: number;
            coisa: string;
        }
    
        


    
*/

import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import type { BottomTabScreenProps } from '@react-navigation/bottom-tabs';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import type { BottomTabScreenProps } from '@react-navigation/bottom-tabs';
import { NavigatorScreenParams } from '@react-navigation/native';

// Define as telas que fazem parte da pilha de autenticacao
export type AuthStackParamList = {
  Login: undefined; // A tela de Login nao recebe parâmetros
  Register: undefined; // A tela de Register também nao
};

export type AppStackParamList = {
  MainTabs: NavigatorScreenParams<AppTabParamList>; // Aninhando o Tab Navigator
  Notifications: undefined;

  // PesquisarDetail: { itemId: string };
};

// Define as telas da Tab Navigator (após o login)
export type AppTabParamList = {
  Home: undefined;
  Pesquisar: undefined;
  NewPost: undefined;
  Eventos: undefined;
  Perfil: undefined;
};

// Tipos para as props de cada tela de autenticacao
export type LoginScreenProps = NativeStackScreenProps<AuthStackParamList, 'Login'>;
export type RegisterScreenProps = NativeStackScreenProps<AuthStackParamList, 'Register'>;
export type HomeScreenProps = BottomTabScreenProps<AppTabParamList, 'Home'>;
export type AuthScreenProps<T extends keyof AuthStackParamList> =
  NativeStackScreenProps<AuthStackParamList, T>;

export type AppStackScreenProps<T extends keyof AppStackParamList> =
  NativeStackScreenProps<AppStackParamList, T>;


export type AppTabScreenProps<T extends keyof AppTabParamList> =
  BottomTabScreenProps<AppTabParamList, T>;
