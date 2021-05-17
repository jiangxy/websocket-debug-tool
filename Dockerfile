ARG base_image=node
FROM ${base_image}

WORKDIR /app
COPY . /app

# Install packages
RUN npm install

# Open port 4040
EXPOSE 4040

# Run the application
CMD npm run dev


