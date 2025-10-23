import React from 'react';
import { View, Image, StyleSheet, StatusBar } from 'react-native';

export default function SplashScreen() {
  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      <Image
        source={require('../assets/MUSA-LOGO.png')} 
        style={styles.logo}
        resizeMode="contain"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FFFFFF', 
  },
  logo: {
    width: 150, 
    height: 150,
  },
});
