  import React from 'react';
  import { View, Text, StyleSheet, TextInput } from 'react-native';

  export default function PesquisarScreen() {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>Pesquisar</Text>
        <TextInput
          style={styles.input}
          placeholder="O que você procura?"
        />
      </View>
    );
  }

  const styles = StyleSheet.create({
    container: {
      flex: 1,
      paddingTop: 50,
      alignItems: 'center',
      paddingHorizontal: 20,
    },
    title: {
      fontSize: 22,
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
    },
  });
