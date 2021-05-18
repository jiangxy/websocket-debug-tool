ARG base_image=node
FROM ${base_image}

WORKDIR /app
COPY . /app

# Install packages
RUN npm install

# Run the application
CMD npm run dev


