import React from 'react';
import { Image, ScrollView, StyleSheet, Text, View } from 'react-native';
import { scale } from 'react-native-size-matters';

export default function PerfilScreen() {
  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.header}>
        <View style={styles.containerProfile}>
          <Image
            source={{
              uri: 'https://images.unsplash.com/photo-1566843972705-1aad0ee32f88?q=80&w=1470&auto=format&fit=crop',
            }}
            style={styles.profileImage}
          />

          <View style={styles.nameContainer}>
            <Text style={styles.name}>BRUNNO Gabryel</Text>
            <Text style={styles.username}>@BRUNNINHO</Text>
          </View>
        </View>
        <View style={styles.contactInfo}>
          <Text style={styles.contactText}>(61) 99999-9999</Text>
          <Text style={styles.contactText}>MUSA@ALUNO.UNB.BR</Text>
          <Text style={styles.contactText}>Estudante de Direito</Text>
        </View>
        {/* 

      <Text style={styles.bio}>ESTUDANTE DE DIREITO</Text>

      <View style={styles.postsSection}>
        <Text style={styles.postsTitle}>POSTS</Text>
      </View> */}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    paddingTop: 49,
    paddingHorizontal: 20,
  },
  header: {
    width: '100%',
    marginBottom: 20,
    gap: 12,
  },
  containerProfile: {
    alignItems: 'center',
    flexDirection: 'row',
  },

  profileImage: {
    width: scale(100),
    height: scale(100),
    borderRadius: scale(100),
    marginRight: 20, // espaço entre imagem e textos
  },
  nameContainer: {
    flex: 1,
    alignContent: 'center',
  },
  name: {
    textAlign: 'center',
    fontSize: 22,
    fontWeight: '600',
    color: '#000',
    fontFamily: 'JuliusSansOne-Regular',
  },
  username: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
  },
  contactInfo: {
    marginBottom: 16,
  },
  contactText: {
    fontSize: 14,
    color: '#333',
    marginBottom: 4,
  },
  bio: {
    fontSize: 16,
    textAlign: 'center',
    color: '#333',
    fontStyle: 'italic',
    marginBottom: 24,
  },
  postsSection: {
    borderTopWidth: 1,
    borderTopColor: '#E0E0E0',
    width: '90%',
    paddingTop: 16,
    alignItems: 'center',
  },
  postsTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#000',
    marginBottom: 16,
  },
});
