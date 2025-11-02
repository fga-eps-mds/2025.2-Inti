import React, { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Image,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { scale } from 'react-native-size-matters';
import axios from 'axios';

interface IPostsContract {
  id: number;
  src: {
    portrait: string;
  };
  alt: string;
}

export default function PerfilScreen() {
  const [loading, setLoading] = useState(true);
  const [posts, setPosts] = useState<Array<IPostsContract>>([]);

  useEffect(() => {
    const handleGetPosts = async () => {
      const response = await axios.get(
        'https://api.pexels.com/v1/search?query=people',
        {
          headers: {
            Authorization:
              'ggmVotobVGiKdiJD4GkkfOkkz17VWQaylzQvUYHIhrkwW0Az0FCzDtnK',
          },
        },
      );

      console.log(response.data.photos);

      setPosts(() => response.data.photos);
      setLoading(() => false);
    };

    handleGetPosts();

    console.log(posts);
  }, []);

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.contentContainer}
    >
      <View style={styles.header}>
        <View style={styles.containerProfile}>
          <Image
            source={require('../../assets/imageProfile.png')}
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

      {loading ? (
        <ActivityIndicator color="#000" size={50} />
      ) : (
        <View style={styles.feedSection}>
          {posts.map(postItem => (
            <Image
              key={postItem.id}
              style={styles.image}
              source={{
                uri: postItem.src.portrait,
              }}
              alt={postItem.alt}
            />
          ))}
        </View>
      )}
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
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  containerLine: {
    gap: scale(20),
    flexDirection: 'row',
  },
  image: {
    width: scale(145),
    height: scale(145),
    borderRadius: scale(20),
  },
});
