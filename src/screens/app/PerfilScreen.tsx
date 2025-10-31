import React from 'react';
import { View, Text, StyleSheet, Button } from 'react-native';
import { useAuth } from '../../hooks/useAuth';


export default function PerfilScreen() {
  return (
    <ScrollView style={styles.container}>
      {/* Foto de perfil */}
      <Image
        source={{
          uri: 'https://images.unsplash.com/photo-1566843972705-1aad0ee32f88?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        }}
        style={styles.profileImage}
      />
      {/* Nome */}
      <Text style={styles.name}>BRUNNO GABRYEL</Text>
      {/* Username */}
      <Text style={styles.username}>@BRUNNINHO</Text>
      {/* Informações de contato */}
      <View style={styles.contactInfo}>
        <Text style={styles.contactText}>(61) 99999-9999</Text>
        <Text style={styles.contactText}>MUSA@ALUNO.UNB.BR</Text>
      </View>
      {/* Bio/Descrição */}rr
      <Text style={styles.bio}>ESTUDANTE DE DIREITO</Text>
      {/* Aqui viriam os posts */}
      <View style={styles.postsSection}>
        <Text style={styles.postsTitle}>POSTS</Text>
        {/* Grid de posts viria aqui */}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 20,
  },
  profileImage: {
    width: 100,
    height: 100,
    borderRadius: 50,
    alignSelf: 'stretch',
    marginTop: 20,
    marginBottom: 16,
    // Adicione border se tiver no design
    // borderWidth: 2,
    // borderColor: '#E0E0E0',
  },
  name: {
    fontSize: 24,
    fontWeight: 'light',
    textAlign: 'left',
    color: '#000000',
    marginBottom: 4,
    fontFamily: 'JuliusSansOne-Regular',
  },
  username: {
    fontSize: 16,
    textAlign: 'left',
    color: '#666666',
    marginBottom: 16,
  },
  contactInfo: {
    marginBottom: 16,
    alignItems: 'flex-start', // ou 'flex-start' se for alinhado à esquerda
  },
  contactText: {
    fontSize: 14,
    color: '#333333',
    marginBottom: 4,
    alignItems: 'flex-start',
  },
  bio: {
    fontSize: 16,
    textAlign: 'center',
    color: '#333333',
    fontStyle: 'italic', // se for o caso
    marginBottom: 24,
  },
  postsSection: {
    borderTopWidth: 1,
    borderTopColor: '#E0E0E0',
    paddingTop: 16,
  },
  postsTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#000000',
    marginBottom: 16,
  },
});

