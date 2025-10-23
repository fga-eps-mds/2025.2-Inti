import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { View, Text, TouchableOpacity, Image, StyleSheet } from 'react-native';
import Ionicons from 'react-native-vector-icons/Ionicons';
import { useNavigation } from '@react-navigation/native';

// @types
import { AppStackParamList, AppTabParamList } from '../@types/navigation';

import NotificationScreen from '../screens/app/NotificationScreen';
import HomeScreen from '../screens/app/EventosScreen';
import EventosScreen from '../screens/app/EventosScreen';
import PerfilScreen from '../screens/app/PerfilScreen';
import PesquisarScreen from '../screens/app/PesquisarScreen';


function NewPostScreen() {
  return (
    <View style={styles.dummyScreen}>
      <Text>Tela de Novo Post (Em desenvolvimento)</Text>
    </View>
  );
}

function HeaderLogo() {
  return (
    <Image
      style={styles.headerLogo}
      source={require('../assets/MUSA-LOGO.png')}
      resizeMode="contain"
    />
  );
}

function NotificationsButton() {
  const navigation = useNavigation();
  return (
    <TouchableOpacity onPress={() => navigation.navigate('Notifications')}>
      <Ionicons name="notifications-outline" size={26} color="#000" />
    </TouchableOpacity>
  );
}


const Stack = createNativeStackNavigator<AppStackParamList>();
const Tab = createBottomTabNavigator<AppTabParamList>();

function MainTabsNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName = 'home'; 
          if (route.name === 'Home') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'Pesquisar') {
            iconName = focused ? 'search' : 'search-outline';
          } else if (route.name === 'NewPost') {
            iconName = focused ? 'add-circle' : 'add-circle-outline';
            return <Ionicons name={iconName} size={32} color={color} />; 
          } else if (route.name === 'Eventos') {
            iconName = focused ? 'calendar' : 'calendar-outline';
          } else if (route.name === 'Perfil') {
            iconName = focused ? 'person' : 'person-outline';
          }
          return <Ionicons name={iconName as string} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#6200EE', 
        tabBarInactiveTintColor: 'gray',
        tabBarShowLabel: false, // esconde os nomes (Home, Perfil, etc)
      })}
    >
      <Tab.Screen
        name="Home"
        component={HomeScreen}
        options={{
          headerTitle: 'Feed', // Título 
          headerTitleAlign: 'center',
          headerLeft: () => <HeaderLogo />, // Logo "M"
          headerRight: () => <NotificationsButton />, // Botão de Notificações
        }}
      />


      <Tab.Screen name="Pesquisar" component={PesquisarScreen} />
      <Tab.Screen name="NewPost" component={NewPostScreen} options={{ title: 'Novo Post' }} />
      <Tab.Screen name="Eventos" component={EventosScreen} />
      <Tab.Screen name="Perfil" component={PerfilScreen} />
    </Tab.Navigator>
  );
}


export default function AppNavigator() {
  return (
    <Stack.Navigator>
      <Stack.Screen
        name="MainTabs" // contém as abas
        component={MainTabsNavigator}
        options={{ headerShown: false }} // Esconde o header duplicado do Stack
      />
      <Stack.Screen
        name="Notifications" 
        component={NotificationScreen}
        options={{ title: 'Notificações' }}
      />
    </Stack.Navigator>
  );
}

const styles = StyleSheet.create({
  dummyScreen: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  headerLogo: {
    width: 30,
    height: 30,
    marginLeft: 10,
  },
});

