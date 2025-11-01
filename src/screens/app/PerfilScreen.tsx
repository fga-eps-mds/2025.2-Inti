import React from 'react';
import { Image, ScrollView, StyleSheet, Text, View } from 'react-native';
import { scale } from 'react-native-size-matters';

export default function PerfilScreen() {
  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.contentContainer}
    >
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
      </View>
      <View style={styles.feedSection}>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/736x/25/7d/1d/257d1d2e68d3ba8fe97668124d31ccbb.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/736x/25/7d/1d/257d1d2e68d3ba8fe97668124d31ccbb.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
        <View style={styles.containerLine}>
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
          <Image
            style={styles.image}
            source={{
              uri: 'https://i.pinimg.com/1200x/f6/c6/1d/f6c61dbd0154d004d46895ab014ed338.jpg',
            }}
          />
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  contentContainer: {
    paddingTop: 49,
    paddingHorizontal: 20,
    paddingBottom: 29,
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
  feedSection: {
    flex: 1,
    gap: scale(20),
  },
  containerLine: {
    gap: scale(20),
    flexDirection: 'row',
  },
  image: {
    width: scale(148),
    height: scale(148),
    borderRadius: scale(20),
  },
});
