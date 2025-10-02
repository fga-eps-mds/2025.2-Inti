FROM node:20

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install

COPY . .

# Expõe a porta do Metro Bundler
EXPOSE 8081

CMD ["npx", "react-native", "start", "--host", "0.0.0.0"]
