import {
  StyleSheet,
  Alert,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  Button,
  ActivityIndicator,
  Image,
} from 'react-native';
import { useAuth } from '../../hooks/useAuth';
import {
  Asset,
  ImagePickerResponse,
  launchImageLibrary,
  launchCamera,
} from 'react-native-image-picker';
import { AppTabScreenProps } from '../../@types/navigation';
import { useState } from 'react';
import { Platform } from 'react-native';
import { check, request, PERMISSIONS, RESULTS } from 'react-native-permissions';

export default function PostScreen({
  navigation,
}: AppTabScreenProps<'NewPost'>) {
  const [description, setDescription] = useState('');
  const [image, setImage] = useState<Asset | null>(null);
  const [loading, setLoading] = useState(false);
  const { token } = useAuth();

  const requestGalleryPermission = async () => {
    try {
      if (Platform.OS === 'android') {
        const permission =
          Platform.Version >= 33
            ? PERMISSIONS.ANDROID.READ_MEDIA_IMAGES
            : PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE;

        const result = await request(permission);
        return result === RESULTS.GRANTED;
      }

      if (Platform.OS === 'ios') {
        // para iOS 14+ pode retornar 'limited' quando o usuário permite fotos selecionadas
        const result = await request(PERMISSIONS.IOS.PHOTO_LIBRARY);
        return result === RESULTS.GRANTED || result === RESULTS.LIMITED;
      }

      // outros platforms (web etc.) não suportados aqui
      return false;
    } catch (err) {
      console.log('Erro ao pedir permissão: ', err);
      return false;
    }
  };

  const handleChoosePhoto = async () => {
    const granted = await requestGalleryPermission();
    if (!granted) {
      Alert.alert(
        'Permissão necessária',
        'Precisamos de permissão para acessar a galeria para selecionar uma imagem.',
      );
      return;
    }

    launchImageLibrary(
      { mediaType: 'photo', quality: 0.7 },
      (response: ImagePickerResponse) => {
        if (response.didCancel) {
          console.log('Usuário cancelou a seleção de imagem');
        } else if (response.errorCode) {
          console.log('Erro do ImagePicker: ', response.errorMessage);
          Alert.alert('Erro ao selecionar imagem', response.errorMessage);
        } else if (response.assets && response.assets.length > 0) {
          setImage(response.assets[0]);
        }
      },
    );
  };

  const handleSubmit = async () => {
    if (!image || !image.uri || !image.type || !image.fileName) {
      Alert.alert('Erro', 'Por favor, selecione uma imagem válida.');
      return;
    }
    if (!description.trim()) {
      Alert.alert('Erro', 'Por favor, adicione uma descrição.');
      return;
    }
    if (!token) {
      Alert.alert('Erro', 'Você não está autenticado.');
      return;
    }

    setLoading(true);
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.mainTitle}>Nova publicação</Text>

      <TouchableOpacity style={styles.imagePicker} onPress={handleChoosePhoto}>
        {image ? (
          <Image source={{ uri: image.uri }} style={styles.imagePreview} />
        ) : (
          <Text style={styles.imagePickerText}>Selecionar Imagem</Text>
        )}
      </TouchableOpacity>

      <Text style={styles.description}>Descrição</Text>

      <TextInput
        style={styles.input}
        placeholder="Adicione uma descrição"
        multiline
        value={description}
        onChangeText={setDescription}
        placeholderTextColor="#888"
      />

      {loading ? (
        <ActivityIndicator size="large" color="#592E83" />
      ) : (
        <Button
          title="Publicar"
          onPress={handleSubmit}
          disabled={!image || !description.trim()}
          color="#592E83"
        />
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    padding: 20,
    backgroundColor: '#fff',
    alignItems: 'center',
  },
  mainTitle: {
    fontSize: 35,
    fontWeight: 'bold',
    marginBottom: 70,
    marginTop: 50,
    color: '#592E83',
  },
  imagePicker: {
    width: '100%',
    height: 200,
    backgroundColor: '#f0f0f0',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  imagePickerText: {
    color: '#555',
    fontSize: 16,
  },
  imagePreview: {
    width: '100%',
    height: '100%',
    borderRadius: 8,
  },
  input: {
    width: '100%',
    minHeight: 100,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 20,
    paddingHorizontal: 15,
    paddingVertical: 10,
    marginBottom: 25,
    fontSize: 16,
    textAlignVertical: 'top',
    color: '#333',
  },
  description: {
    fontSize: 25,
    marginBottom: 30,
    color: '#592E83',
  },
});
